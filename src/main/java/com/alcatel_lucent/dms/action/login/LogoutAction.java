package com.alcatel_lucent.dms.action.login;

import com.alcatel_lucent.dms.UserContext;
import com.alcatel_lucent.dms.action.BaseAction;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.SessionAware;

import java.util.Map;

@SuppressWarnings("serial")
public class LogoutAction extends BaseAction implements SessionAware {

    private Map<String, Object> session;

    public String getLocation() {
        return "/login/forward-to-https?" + buildQueryString();
    }

    @Override
    @Action(results = {
            @Result(name = SUCCESS, type = "redirect", location = "${location}")
    })
    public String execute() throws Exception {
        UserContext uc = (UserContext) session.get(UserContext.SESSION_USER_CONTEXT);
        if (uc != null) {
            log.info("User " + uc.getUser().getLoginName() + " logged out");
            ServletActionContext.getRequest().getSession().invalidate();
        }
        return SUCCESS;
    }

    @Override
    public void setSession(Map<String, Object> session) {
        this.session = session;
    }


}
