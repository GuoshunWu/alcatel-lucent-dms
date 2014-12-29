package com.alcatel_lucent.dms.service;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.model.User;

public interface UserService {
	/**
	 * Create a user.
	 * @param user
	 * @return
	 */
	User createUser(User user) throws BusinessException;
	
	/**
	 * Update a user.
	 *
     * @param loginName login name of the user
     * @param name user name, null if no change
     * @param email email, null if no change
     * @param role user role, null if no change
     * @param status user status, null if no change
     * @param isShowTip if show tip , null if no change
     * @return
	 */
	User updateUser(String loginName, String name, String email, Integer role, Integer status, Boolean isShowTip) throws BusinessException;

    /**
     * Copy the state of the given object onto the persistent object with the same csl
     * */
    User mergeUser(User user);


}
