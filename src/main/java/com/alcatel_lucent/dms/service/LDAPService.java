package com.alcatel_lucent.dms.service;
import com.alcatel_lucent.dms.model.User;

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
    @Deprecated
    User findUserByCSL(String csl);
    
    /**
     * Get user information by CIL.
     * @param cil
     * @return null if CIL doesn't exist
     */
    User findUserByCIL(String cil);
}
