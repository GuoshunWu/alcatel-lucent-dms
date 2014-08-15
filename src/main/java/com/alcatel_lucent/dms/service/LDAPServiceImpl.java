package com.alcatel_lucent.dms.service;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.model.User;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.*;
import org.springframework.stereotype.Service;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import java.util.List;

@Service("ldapService")
public class LDAPServiceImpl implements LDAPService {
    private static final Logger log = LoggerFactory.getLogger(LDAPServiceImpl.class);
    @Autowired
    private LdapTemplate ldapTemplate;

    private static final int TRY_COUNT = 3;

    private AndFilter buildUserFilter(Filter filter) {
        AndFilter andFilter = new AndFilter().and(new EqualsFilter("objectclass", "person")).
                and(new PresentFilter("mail")).
                and(new PresentFilter("uid")).
                and(new PresentFilter("cil"));
        if (null != filter) andFilter.and(filter);
        return andFilter;
    }

    private AndFilter buildUserFilter(String filter) {
        Filter filter1 = null;
        if (StringUtils.isNotEmpty(filter)) filter1 = new HardcodedFilter(filter);
        return buildUserFilter(filter1);
    }

    private static class UserAttributesMapper implements AttributesMapper {
        private static final UserAttributesMapper me = new UserAttributesMapper();

        public static UserAttributesMapper getInstance() {
            return me;
        }

        @Override
        public Object mapFromAttributes(Attributes attributes) throws NamingException {
            Attribute csl = attributes.get("uid");
            Attribute email = attributes.get("mail");
            String cil = (String) attributes.get("cil").get();
            if (cil == null || email == null || csl == null) {
                log.warn("CSL, CIL or email can not be found in attributes \n{}", attributes);
                return null;
            }
            return new User(csl.get().toString(), email.get().toString(), cil);
        }
    }

    public boolean login(String username, String password) {
        String filter = buildUserFilter(
                new OrFilter().
                        or(new EqualsFilter("uid", username)).
                        or(new EqualsFilter("cn", username)).
                        or(new EqualsFilter("cil", username))
        ).encode();
        int tryCount = TRY_COUNT;

        BusinessException businessException = null;
        while (tryCount-- > 0) {
            try {
                return ldapTemplate.authenticate(DistinguishedName.EMPTY_PATH, filter, password);
            } catch (org.springframework.ldap.ServiceUnavailableException e) {
                log.error("Ldap exception: {}", e);
                businessException = new BusinessException(BusinessException.LDAP_CONNECTION_ERROR, e.getMessage());
            }
        }
        throw businessException;
    }

    public List<User> findUsers(String filter) {
        @SuppressWarnings("unchecked")
        List<User> users = ldapTemplate.search(DistinguishedName.EMPTY_PATH, filter, UserAttributesMapper.getInstance());
        return users;
    }

    private User findUser(String filter) {
        log.info("String filter={}", filter);
        List<User> users = findUsers(filter);
        if (users.isEmpty()) return null;
        return users.get(0);
    }


    public User findUserByCSL(String csl) {
        return findUser(buildUserFilter(new EqualsFilter("uid", csl)).encode());
    }

    public User findUserByCIL(String cil) {
        return findUser(buildUserFilter(new EqualsFilter("cil", cil)).encode());
    }

    public User findUserByCSLOrCIL(String cslOrCil) {
        String filter = buildUserFilter(
                new OrFilter().
                        or(new EqualsFilter("uid", cslOrCil)).
                        or(new EqualsFilter("cn", cslOrCil)).
                        or(new EqualsFilter("cil", cslOrCil))
        ).encode();
        return findUser(filter);
    }

}
