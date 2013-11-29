package com.alcatel_lucent.dms.service;

import com.alcatel_lucent.dms.model.User;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.PredicateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.stereotype.Service;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import java.util.List;

@Service("ldapService")
public class LDAPServiceImpl implements LDAPService {
    private static Logger log = LoggerFactory.getLogger(LDAPServiceImpl.class);
    
    @Value("${ldap.url}")
	private String ldapUrl;

    @Autowired
    private LdapTemplate ldapTemplate;

    private static class UserAttributesMapper implements AttributesMapper {
        private static UserAttributesMapper me = new UserAttributesMapper();
        public static UserAttributesMapper getInstance(){
            return  me;
        }
        @Override
        public Object mapFromAttributes(Attributes attributes) throws NamingException {
            Attribute csl = attributes.get("cslx500");
            Attribute email = attributes.get("mail");
            if (null == csl || email == null) return null;
            String cil = (String) attributes.get("cn").get();

            if (cil == null || email == null) return null;
            return new User(csl.get().toString(), email.get().toString(), cil);
        }
    }

    public boolean login(String username, String password) {
        return ldapTemplate.authenticate("", "(&(objectclass=person)(cn=" + username + "))", password);
    }

    public List<User> findUsers(String filter) {
        List<User> users = ldapTemplate.search(DistinguishedName.EMPTY_PATH, filter, UserAttributesMapper.getInstance());
        CollectionUtils.filter(users, PredicateUtils.notNullPredicate());
        return users;
    }


    /**
     * @deprecated use findUserByCIL instead, which also accept CSL as input and have better performance
     */
    @Deprecated
    public User findUserByCSL(String csl) {
        String strFilter = "(&(objectclass=person)(cslx500=" + csl + "))";
        log.info("String filter={}", strFilter);
        List<User> users = findUsers(strFilter);
        if (users.isEmpty()) return null;
        return users.get(0);
    }

    public User findUserByCIL(String cil) {
        String strFilter = "(&(objectclass=person)(cn=" + cil + "))";
        log.info("String filter={}", strFilter);
        List<User> users = findUsers(strFilter);
        if (users.isEmpty()) return null;
        return users.get(0);
    }

}
