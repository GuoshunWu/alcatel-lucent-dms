package com.alcatel_lucent.dms.service;

import com.alcatel_lucent.dms.model.User;

public interface AuthenticationService {
	
	/**
	 * Check user credential on LDAP, and create a new user if not exists after successful login.
	 * @param username CSL
	 * @param password CIP
	 * @return user object, null if login failed
	 */
	User login(String username, String password);

}
