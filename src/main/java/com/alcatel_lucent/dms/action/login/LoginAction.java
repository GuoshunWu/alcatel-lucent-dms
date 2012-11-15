package com.alcatel_lucent.dms.action.login;

import com.alcatel_lucent.dms.UserContext;
import com.alcatel_lucent.dms.action.BaseAction;
import com.alcatel_lucent.dms.model.User;
import com.alcatel_lucent.dms.service.LDAPService;
import com.opensymphony.xwork2.validator.annotations.RequiredFieldValidator;
import com.opensymphony.xwork2.validator.annotations.Validation;
import com.opensymphony.xwork2.validator.annotations.Validations;
import com.opensymphony.xwork2.validator.annotations.ValidatorType;
import org.apache.struts2.convention.annotation.*;
import org.apache.struts2.interceptor.SessionAware;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;

@SuppressWarnings("serial")
@ParentPackage("default")
@Validation()
public class LoginAction extends BaseAction implements SessionAware {

    private String loginname;
    private String password;

    private LDAPService ldapService;
    private Map<String, Object> session;

    public void setLdapService(LDAPService ldapService) {
        this.ldapService = ldapService;
    }

    public String getLoginname() {
        return loginname;
    }

    @RequiredFieldValidator(message = "Login name is required.")
    public void setLoginname(String loginname) {
        this.loginname = loginname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    //    @Override
    @Action(results = {
            @Result(name = SUCCESS, type = "redirect", location = "/appmng.jsp"),
            @Result(name = LOGIN, type = "dispatcher", location = "/login.jsp"),
            @Result(name = INPUT, type = "redirect", location = "/login.jsp")
})
    @Validations(requiredFields =
            {@RequiredFieldValidator(type = ValidatorType.FIELD, fieldName = "loginname", message = "You must enter a value for field.")})
    public String login() {
        log.info("username=" + loginname + ", password=" + password);
        User user = null;
        if (ldapService.login(loginname, password) && null != (user = ldapService.findUserByCSL(loginname))) {
            user.setLastLoginTime(new Timestamp(new Date().getTime()));
            session.put(UserContext.SESSION_USER_CONTEXT, new UserContext(getLocale(), user));
            log.debug("user: " + user);
            return SUCCESS;
        }
//      login failed

        return LOGIN;
    }


    @Override
    public void setSession(Map<String, Object> session) {
        this.session = session;
    }
}
