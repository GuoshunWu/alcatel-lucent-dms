package com.alcatel_lucent.dms.action.login;

import com.alcatel_lucent.dms.UserContext;
import com.alcatel_lucent.dms.action.JSONAction;
import com.alcatel_lucent.dms.model.User;
import com.alcatel_lucent.dms.service.AuthenticationService;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.SessionAware;
import org.springframework.beans.factory.annotation.Value;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@SuppressWarnings("serial")
public class ForwardToHttpsAction extends ActionSupport {
    private static Logger log = Logger.getLogger(ForwardToHttpsAction.class);

    @Value("${httpsPort}")
    private String httpsPort;

    private HttpServletRequest request = ServletActionContext.getRequest();

    public void setHttpsPort(String httpsPort) {
        this.httpsPort = httpsPort;
    }

    public String getLocation() {
        return "https://" + request.getServerName() + ":" + httpsPort + request.getContextPath() + "/entry.action?naviTo=login.jsp";
    }

    @Override
    @Action(results = {@Result(type = "redirect", location = "${location}")})
    public String execute() {
        log.debug("location=" + getLocation());
        return SUCCESS;
    }
}
