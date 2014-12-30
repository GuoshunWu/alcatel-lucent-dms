package com.alcatel_lucent.dms.action;

import java.util.HashMap;
import java.util.HashSet;

import com.alcatel_lucent.dms.UserContext;
import com.alcatel_lucent.dms.action.admin.*;
import com.alcatel_lucent.dms.action.app.*;
import com.alcatel_lucent.dms.action.task.*;
import com.alcatel_lucent.dms.action.trans.*;
import com.alcatel_lucent.dms.model.User;

/**
 * Privilege definitions
 * @author allany
 *
 */
public class Privileges {
	
	private static Privileges instance = null;
	
	private HashMap<String, Integer> privilegeDef = new HashMap<String, Integer>();
	
	/**
	 * Declare all action names for which privilege check is required.
	 * No need to declare public actions which is open for all users.
	 * ROLE_ADMINISTRATOR can be ignored if there is any other role specified.
	 */
	private void init() {
		// administration
		addPrivilege(CharsetAction.class, User.ROLE_ADMINISTRATOR);
		addPrivilege(LanguageAction.class, User.ROLE_ADMINISTRATOR);
		
		// dictionary management
		addPrivilege(AddApplicationAction.class, User.ROLE_APPLICATION_OWNER | User.ROLE_TRANSLATION_MANAGER);
		addPrivilege(AddDictLanguageAction.class, User.ROLE_APPLICATION_OWNER | User.ROLE_TRANSLATION_MANAGER);
		addPrivilege(AddLabelAction.class, User.ROLE_APPLICATION_OWNER);
		addPrivilege(ChangeApplicationVersionAction.class, User.ROLE_APPLICATION_OWNER | User.ROLE_TRANSLATION_MANAGER);
		addPrivilege(ChangeDictVersionAction.class, User.ROLE_APPLICATION_OWNER | User.ROLE_TRANSLATION_MANAGER);
		addPrivilege(CreateApplicationAction.class, User.ROLE_APPLICATION_OWNER | User.ROLE_TRANSLATION_MANAGER);
		addPrivilege(CreateApplicationBaseAction.class, User.ROLE_APPLICATION_OWNER | User.ROLE_TRANSLATION_MANAGER);
		addPrivilege(CreateOrAddApplicationAction.class, User.ROLE_APPLICATION_OWNER | User.ROLE_TRANSLATION_MANAGER);
		addPrivilege(CreateProductAction.class, User.ROLE_APPLICATION_OWNER | User.ROLE_TRANSLATION_MANAGER);
		addPrivilege(CreateProductReleaseAction.class, User.ROLE_APPLICATION_OWNER | User.ROLE_TRANSLATION_MANAGER);
		addPrivilege(DeleteLabelAction.class, User.ROLE_APPLICATION_OWNER);
		addPrivilege(DeliverAppDictAction.class, User.ROLE_APPLICATION_OWNER);
		addPrivilege(DeliverDictAction.class, User.ROLE_APPLICATION_OWNER);
		addPrivilege(DeliverUpdateDictAction.class, User.ROLE_APPLICATION_OWNER);
		addPrivilege(DeliverUpdateDictLanguageAction.class, User.ROLE_APPLICATION_OWNER);
		addPrivilege(DeliverUpdateLabelAction.class, User.ROLE_APPLICATION_OWNER);
		addPrivilege(RemoveApplicationAction.class, User.ROLE_APPLICATION_OWNER | User.ROLE_TRANSLATION_MANAGER);
		addPrivilege(RemoveApplicationBaseAction.class, User.ROLE_APPLICATION_OWNER | User.ROLE_TRANSLATION_MANAGER);
		addPrivilege(RemoveDictAction.class, User.ROLE_APPLICATION_OWNER);
		addPrivilege(RemoveDictLanguageAction.class, User.ROLE_APPLICATION_OWNER);
		addPrivilege(RemoveProductAction.class, User.ROLE_APPLICATION_OWNER | User.ROLE_TRANSLATION_MANAGER);
		addPrivilege(RemoveProductBaseAction.class, User.ROLE_APPLICATION_OWNER | User.ROLE_TRANSLATION_MANAGER);
		addPrivilege(UpdateDictAction.class, User.ROLE_APPLICATION_OWNER | User.ROLE_TRANSLATION_MANAGER);
		addPrivilege(UpdateDictLanguageAction.class, User.ROLE_APPLICATION_OWNER | User.ROLE_TRANSLATION_MANAGER);
		addPrivilege(UpdateLabelAction.class, User.ROLE_APPLICATION_OWNER | User.ROLE_TRANSLATION_MANAGER);
		addPrivilege(UpdateLabelStatusAction.class, User.ROLE_APPLICATION_OWNER | User.ROLE_TRANSLATION_MANAGER);
		addPrivilege(ImportTranslationDetailsAction.class, User.ROLE_APPLICATION_OWNER | User.ROLE_TRANSLATION_MANAGER);

		addPrivilege(UpdateLabelRefAndTranslationsAction.class, User.ROLE_APPLICATION_OWNER | User.ROLE_TRANSLATION_MANAGER);
		addPrivilege(GlossaryAction.class, User.ROLE_ADMINISTRATOR);


		// translation management
		addPrivilege(UpdateStatusAction.class, User.ROLE_APPLICATION_OWNER | User.ROLE_TRANSLATION_MANAGER);
		addPrivilege(UpdateTranslationAction.class, User.ROLE_APPLICATION_OWNER | User.ROLE_TRANSLATION_MANAGER);
		
		// task management
		addPrivilege(ApplyTaskAction.class, User.ROLE_TRANSLATION_MANAGER);
		addPrivilege(CloseTaskAction.class, User.ROLE_TRANSLATION_MANAGER);
		addPrivilege(CreateTaskAction.class, User.ROLE_TRANSLATION_MANAGER);
		addPrivilege(ReceiveTaskFilesAction.class, User.ROLE_TRANSLATION_MANAGER);
	}
	
	/**
	 * Get set of privilege names which is forbidden to current user.
	 * @return
	 */
	public HashSet<String> getForbiddenPrivileges() {
		HashSet<String> result = new HashSet<String>();
		int role = getCurrentRole();
		if ((role & User.ROLE_ADMINISTRATOR) != 0) {
			return result;
		}
		for (String privilegeName : privilegeDef.keySet()) {
			int requiredRoles = privilegeDef.get(privilegeName);
			if ((requiredRoles & role) == 0) {
				result.add(privilegeName);
			}
		}
		return result;
	}
	
	/**
	 * Check if current user is allowed to execute an action.
	 * @param actionClass
	 * @return
	 */
	public boolean isAllowed(BaseAction actionClass) {
		int role = getCurrentRole();
		if ((role & User.ROLE_ADMINISTRATOR) != 0) {
			return true;
		}
		Integer requiredRoles = privilegeDef.get(actionClass.getClass().getSimpleName());
		return requiredRoles == null || (requiredRoles & role) != 0;
	}
	
	private int getCurrentRole() {
		if (UserContext.getInstance() != null && UserContext.getInstance().getUser() != null) {
			return UserContext.getInstance().getUser().getRole();
		} else {
			return User.ROLE_GUEST;
		}
	}
	
	synchronized public static Privileges getInstance() {
		if (instance == null) {
			instance = new Privileges();
		}
		return instance;
	}
	
	private Privileges() {
		init();
	}
	
	private void addPrivilege(Class<?> actionClass, int role) {
		String privilegeName = actionClass.getSimpleName();
		Integer currentRole = privilegeDef.get(privilegeName);
		if (currentRole == null) {
			currentRole = 0;
		}
		privilegeDef.put(privilegeName, currentRole | role);
	}
	

}
