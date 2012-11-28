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
	
	/**
	 * Perform login action and generate a temporary secure token if succeeded.
	 * @param username CSL
	 * @param password CIP
	 * @return secure token, null if login failed
	 */
	public String secureLogin1(String username, String password);
	
	/**
	 * Validate secure token and return corresponding user info.
	 * The secure token will be invalidated once used.
	 * @param token secure token
	 * @return User object if token is validated, null if the token is not valid
	 */
	public User secureLogin2(String token);

}
