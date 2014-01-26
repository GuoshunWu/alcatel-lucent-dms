package com.alcatel_lucent.dms.service;
import com.alcatel_lucent.dms.model.User;

import java.util.List;

public interface LDAPService {
	
	/**
	 * Validate username/password
	 * @param username
	 * @param password
	 * @return
	 */
    boolean login(String username, String password);
    
    /**
     * Get user information by CSL.
     * @param csl
     * @return null if CSL doesn't exist
     */
    User findUserByCSL(String csl);
    List<User> findUsers(String filter);
    
    /**
     * Get user information by CIL.
     * @param cil
     * @return null if CIL doesn't exist
     */
    User findUserByCIL(String cil);
    User findUserByCSLOrCIL(String cslOrCil);
}
