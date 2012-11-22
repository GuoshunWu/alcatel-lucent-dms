package com.alcatel_lucent.dms.action.login;

import com.alcatel_lucent.dms.UserContext;
import com.alcatel_lucent.dms.action.BaseAction;
import com.alcatel_lucent.dms.action.JSONAction;
import com.alcatel_lucent.dms.model.User;
import com.alcatel_lucent.dms.service.AuthenticationService;
import com.alcatel_lucent.dms.service.LDAPService;
import com.opensymphony.xwork2.validator.annotations.RequiredFieldValidator;
import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork2.validator.annotations.ValidatorType;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.SessionAware;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;

@SuppressWarnings("serial")
public class LoginAction extends JSONAction implements SessionAware {

    private String loginname;
    private String password;

    private AuthenticationService authenticationService;
    private Map<String, Object> session;

    public String getLoginname() {
        return loginname;
    }

    public void setLoginname(String loginname) {
        this.loginname = loginname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public void setSession(Map<String, Object> session) {
        this.session = session;
    }

    protected String performAction() throws Exception {
        User user = authenticationService.login(loginname, password);
        if (user != null) {
            session.put(UserContext.SESSION_USER_CONTEXT, new UserContext(getLocale(), user));
            log.debug("user: " + user);
            setMessage(getText("message.success"));
            setStatus(0);
            return SUCCESS;
        }

        setMessage(getText("message.loginfail"));
        setStatus(-1);
        return SUCCESS;
    }

	public AuthenticationService getAuthenticationService() {
		return authenticationService;
	}

	public void setAuthenticationService(AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}
}
