package com.alcatel_lucent.dms.interceptors;

import net.sf.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alcatel_lucent.dms.UserContext;
import com.alcatel_lucent.dms.action.BaseAction;
import com.alcatel_lucent.dms.action.Privileges;
import com.alcatel_lucent.dms.action.login.ForwardToHttpsAction;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

@SuppressWarnings("serial")
public class AuthorizationInterceptor extends AbstractInterceptor {
	
	private static Logger log = LoggerFactory.getLogger(ForwardToHttpsAction.class);
	
	@Override
	public String intercept(ActionInvocation invocation) throws Exception {
		final Object action = invocation.getAction();
		UserContext uc = UserContext.getInstance();
		if (uc != null) {
			log.info("[Action] " + uc.getUser().getLoginName() + " - " + action.getClass().getSimpleName());
		}
		if (!Privileges.getInstance().isAllowed((BaseAction) action)) {
			return "denied";
		}
		return invocation.invoke();
	}

}
