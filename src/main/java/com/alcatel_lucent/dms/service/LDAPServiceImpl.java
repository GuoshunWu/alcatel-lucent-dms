package com.alcatel_lucent.dms.service;

import com.alcatel_lucent.dms.model.User;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.PredicateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.OrFilter;
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
        return ldapTemplate.authenticate(DistinguishedName.EMPTY_PATH, "(&(objectclass=person)(uid=" + username + "))", password);
    }

    public List<User> findUsers(String filter) {
        @SuppressWarnings("unchecked")
        List<User> users = ldapTemplate.search(DistinguishedName.EMPTY_PATH, filter, UserAttributesMapper.getInstance());
        CollectionUtils.filter(users, PredicateUtils.notNullPredicate());
        return users;
    }

    private User findUser(String filter) {
        log.info("String filter={}", filter);
        List<User> users = findUsers(filter);
        if (users.isEmpty()) return null;
        return users.get(0);
    }


    public User findUserByCSL(String csl) {
        return findUser(String.format("(&(objectclass=person)(uid=%s))", csl));
    }

    public User findUserByCIL(String cil) {
        return findUser(String.format("(&(objectclass=person)(cil=%s))", cil));
    }

    public User findUserByCSLOrCIL(String cslOrCil) {
        AndFilter andFilter = new AndFilter();
        andFilter.and(new EqualsFilter("objectclass", "person")).and(
                new OrFilter().or(new EqualsFilter("uid", cslOrCil)
                ).or(new EqualsFilter("cil", cslOrCil)).or(new EqualsFilter("cn", cslOrCil))
        );
        return findUser(andFilter.toString());
    }

}
