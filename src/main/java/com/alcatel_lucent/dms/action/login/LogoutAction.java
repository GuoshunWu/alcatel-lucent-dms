package com.alcatel_lucent.dms.action.login;

import java.util.Map;

import com.alcatel_lucent.dms.Global;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.SessionAware;

import com.alcatel_lucent.dms.UserContext;
import com.alcatel_lucent.dms.action.BaseAction;

@SuppressWarnings("serial")
public class LogoutAction extends BaseAction implements SessionAware {

    private Map<String, Object> session;

    @Override
    @Action(results = {
            @Result(name = SUCCESS, type = "redirect", location = "/login/forward-to-https")
    })
    public String execute() throws Exception {
        UserContext uc = (UserContext) session.get(UserContext.SESSION_USER_CONTEXT);
        if (uc != null) {
            log.info("User " + uc.getUser().getLoginName() + " logged out");
            session.remove(UserContext.SESSION_USER_CONTEXT);
            ServletActionContext.getRequest().getSession().invalidate();
            Global.logout(uc.getUser());
        }
        return SUCCESS;
    }

    @Override
    public void setSession(Map<String, Object> session) {
        this.session = session;
    }


}
