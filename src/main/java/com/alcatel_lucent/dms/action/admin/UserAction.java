package com.alcatel_lucent.dms.action.admin;

import com.alcatel_lucent.dms.SystemError;
import com.alcatel_lucent.dms.action.JSONAction;
import com.alcatel_lucent.dms.model.User;
import com.alcatel_lucent.dms.service.UserService;

@SuppressWarnings("serial")
public class UserAction extends JSONAction {

	private UserService userService;
	
	private String oper;
	private String loginName;
	private String name;
	private String email;
	private Integer role;
	private Integer userStatus;
    private Boolean isShowTip;
	
	@Override
	protected String performAction() throws Exception {
		log.info("[UserAction] oper=" + oper + ",loginName=" + loginName + ",name=" + name + ",email=" + email + ",role=" + role + ",userStatus=" + userStatus);
		if (oper.equals("add")) {
			User user = new User();
			user.setLoginName(loginName);
			user.setName(name);
			user.setEmail(email);
			user.setRole(role);
			user.setStatus(userStatus);
			userService.createUser(user);
		} else if (oper.equals("edit")) {
			userService.updateUser(loginName, name, email, role, userStatus, isShowTip);
		} else {
			throw new SystemError("Unknown oper: " + oper);
		}
		setMessage(getText("message.success"));
		return SUCCESS;
	}

    public Boolean getIsShowTip() {
        return isShowTip;
    }

    public void setIsShowTip(Boolean isShowTip) {
        this.isShowTip = isShowTip;
    }

    public String getOper() {
		return oper;
	}

	public void setOper(String oper) {
		this.oper = oper;
	}

	public String getLoginName() {
		return loginName;
	}

	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public int getRole() {
		return role;
	}

	public void setRole(int role) {
		this.role = role;
	}

	public Integer getUserStatus() {
		return userStatus;
	}

	public void setUserStatus(Integer userStatus) {
		this.userStatus = userStatus;
	}

	public UserService getUserService() {
		return userService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

}
