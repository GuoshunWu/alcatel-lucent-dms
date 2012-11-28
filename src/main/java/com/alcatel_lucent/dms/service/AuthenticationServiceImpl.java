package com.alcatel_lucent.dms.service;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alcatel_lucent.dms.model.User;

@Service("authenticationService")
public class AuthenticationServiceImpl extends BaseServiceImpl implements AuthenticationService {
	
	private static Logger log = Logger.getLogger(AuthenticationServiceImpl.class);

	@Autowired
	private LDAPService ldapService;
	
	private HashMap<String, User> tokenMap = new HashMap<String, User>();
	
	public User login(String username, String password) {
		if (ldapService.login(username, password)) {	// login successfully
			log.info("User " + username + " logged in.");
			User user = (User) dao.retrieve(User.class, username);
			if (user == null) {	// create a new user entry
				user = ldapService.findUserByCSL(username);
				if (user == null) {
					return null;
				}
				user.setRole(User.ROLE_GUEST);
				user.setStatus(User.ENABLED);
				user = (User) dao.create(user);
				log.info("Created new user " + user.getName() + "(" + user.getLoginName() + ")");
			}
			user.setLastLoginTime(new Timestamp(System.currentTimeMillis()));
			return user;
		} else {
			return null;
		}
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
}
