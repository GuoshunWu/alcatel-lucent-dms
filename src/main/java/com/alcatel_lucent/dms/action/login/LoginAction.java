package com.alcatel_lucent.dms.action.login;

import com.alcatel_lucent.dms.action.BaseAction;
import org.apache.struts2.convention.annotation.*;

@SuppressWarnings("serial")

@ParentPackage("default")
//@InterceptorRef("defaultSecurityStackWithAuthentication")
public class LoginAction extends BaseAction{

    private String username;
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Action(results = {
            @Result(name = SUCCESS, type = "redirect", location = "/appmng.jsp"),
            @Result(name = LOGIN, type = "dispatcher", location = "/login.jsp")
    })
    public String execute() {
        log.info("username=" + username + ", password=" + password);

        return SUCCESS;
    }
}
