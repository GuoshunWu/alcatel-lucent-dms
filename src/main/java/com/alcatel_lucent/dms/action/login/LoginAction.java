package com.alcatel_lucent.dms.action.login;

import com.alcatel_lucent.dms.UserContext;
import com.alcatel_lucent.dms.action.BaseAction;
import com.alcatel_lucent.dms.action.JSONAction;
import com.alcatel_lucent.dms.model.User;
import com.alcatel_lucent.dms.service.AuthenticationService;
import com.alcatel_lucent.dms.service.LDAPService;
import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.validator.annotations.RequiredFieldValidator;
import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork2.validator.annotations.ValidatorType;
import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.*;
import org.apache.struts2.interceptor.SessionAware;
import org.apache.struts2.interceptor.TokenInterceptor;
import org.springframework.beans.factory.annotation.Value;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;

@SuppressWarnings("serial")
//@Namespace("/")
public class LoginAction extends ActionSupport implements SessionAware {

    private static Logger log = Logger.getLogger(LoginAction.class);

    private String loginname;
    private String password;

    private AuthenticationService authenticationService;
    private Map<String, Object> session;

    private HttpServletRequest request = ServletActionContext.getRequest();

    @Value("${httpPort}")
    private String httpPort;

    public void setHttpPort(String httpPort) {
        this.httpPort = httpPort;
    }

    @RequiredStringValidator(key = "message.name_required", message = "Please specify your name")
    public String getLoginname() {
        return loginname;
    }

    public String getLocation() {
        return "http://" + request.getServerName() + ":" + httpPort + request.getContextPath() + "/entry.action";
    }

    public String getNaviTo() {
        log.debug("get NaviTo invoked.");
        return "appmng.jsp";
    }

    public void setLoginname(String loginname) {
        this.loginname = loginname;
    }

    @RequiredStringValidator(key = "message.pwd_required", message = "Password is required")
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

    @Action(value = "/login", results = {
            @Result(type = "redirect", location = "${location}"),
            @Result(name = INPUT, location = "/login.jsp"),
            @Result(name = TokenInterceptor.INVALID_TOKEN_CODE, location = "/login.jsp")
    })
    public String execute() throws Exception {
        User user = authenticationService.login(loginname, password);
        if (user != null) {
            session.put(UserContext.SESSION_USER_CONTEXT, new UserContext(getLocale(), user));
            log.debug("user: " + user);
            log.debug("redirect to " + getLocation());
            return SUCCESS;
        }
        addActionError(getText("message.loginfail"));
        return INPUT;
    }

    public AuthenticationService getAuthenticationService() {
        return authenticationService;
    }

    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }
}
