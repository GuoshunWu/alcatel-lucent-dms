package com.alcatel_lucent.dms.action.login;

import com.alcatel_lucent.dms.action.BaseAction;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import javax.servlet.http.HttpServletRequest;

@SuppressWarnings("serial")
public class ForwardToHttpsAction extends BaseAction {
    private static Logger log = LoggerFactory.getLogger(ForwardToHttpsAction.class);

    @Value("${httpsPort}")
    private String httpsPort;

    private HttpServletRequest request = ServletActionContext.getRequest();

    public void setHttpsPort(String httpsPort) {
        this.httpsPort = httpsPort;
    }

    public String getLocation() {
        return buildNavigateURL("https", httpsPort, "login.jsp");
    }

    @Override
    @Action(results = {@Result(type = "redirect", location = "${location}")})
    public String execute() {
        log.debug("location=" + getLocation());
        return SUCCESS;
    }
}
