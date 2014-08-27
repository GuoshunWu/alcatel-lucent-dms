package com.alcatel_lucent.dms.action.login;

import com.alcatel_lucent.dms.Global;
import com.alcatel_lucent.dms.UserContext;
import com.alcatel_lucent.dms.action.BaseAction;
import com.alcatel_lucent.dms.model.User;
import com.alcatel_lucent.dms.service.AuthenticationService;
import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.SessionAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import javax.servlet.SessionCookieConfig;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.SimpleTimeZone;

@SuppressWarnings("serial")
//@Namespace("/")
public class LoginAction extends BaseAction implements SessionAware {

    private static Logger log = LoggerFactory.getLogger(LoginAction.class);

    private String loginname;
    private String password;

    private AuthenticationService authenticationService;
    private Map<String, Object> session;
    private Integer timeZoneOffset;

    private HttpServletRequest request = ServletActionContext.getRequest();

    @Value("${httpPort}")
    private String httpPort;

    //    build number for deploy
    @Value("${buildNumber}")
    private String buildNumber;
    @Value("${version}")
    private String version;

    public void setTimeZoneOffset(Integer timeZoneOffset) {
        this.timeZoneOffset = timeZoneOffset;
    }

    public void setHttpPort(String httpPort) {
        this.httpPort = httpPort;
    }

    @RequiredStringValidator(key = "message.name_required", message = "Please specify your name")
    public String getLoginname() {
        return loginname;
    }

    public String getLocation() {
        return "http://" + request.getServerName() + ":" + httpPort + request.getContextPath() + "/entry.action?naviTo=main.jsp";
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
//        log.info("In login action, requested session id="+request.getRequestedSessionId());
//        log.info("In login action, session id=" + request.getSession().getId());
    }

    @Action(value = "/login", results = {
            @Result(type = "redirect", location = "${location}"),
            @Result(name = INPUT, location = "/login.jsp")
    })

    public String execute() throws Exception {
        User user = authenticationService.login(loginname, password);
        if (user != null) {
            log.info("timeZoneOffset={}", timeZoneOffset);

            session.put(UserContext.SESSION_USER_CONTEXT, new UserContext(getLocale(), user, new SimpleTimeZone(timeZoneOffset, user.getName() + "_TimeZone")));
            Global.login(user);

            log.debug("user: " + user);
            log.debug("redirect to " + getLocation());

//          Tomcat will create new session id if there is no JSESSIONID cookie when https jump to http
            HttpServletResponse response = ServletActionContext.getResponse();
            SessionCookieConfig scc = ServletActionContext.getServletContext().getSessionCookieConfig();
            Cookie cookie = new Cookie(scc.getName(), request.getSession().getId());
            cookie.setPath(scc.getPath());
            response.addCookie(cookie);
            return SUCCESS;
        }
        addActionError(getText("message.loginfail"));
        return INPUT;
    }

    public AuthenticationService getAuthenticationService() {
        return authenticationService;
    }

    public String getBuildNumber() {
        return buildNumber;
    }

    public String getVersion() {
        return version;
    }

    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }
}
