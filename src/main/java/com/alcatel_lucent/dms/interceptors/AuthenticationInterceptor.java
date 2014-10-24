package com.alcatel_lucent.dms.interceptors;

import com.alcatel_lucent.dms.UserContext;
import com.alcatel_lucent.dms.action.BaseAction;
import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import com.opensymphony.xwork2.interceptor.MethodFilterInterceptor;
import com.opensymphony.xwork2.util.LocalizedTextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.struts2.StrutsStatics;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: guoshunw
 * Date: 12-11-12
 * Time: 上午10:00
 * To change this template use File | Settings | File Templates.
 */
@Deprecated
public class AuthenticationInterceptor extends AbstractInterceptor implements StrutsStatics {

    protected static Logger log = LoggerFactory.getLogger(AuthenticationInterceptor.class);

    @SuppressWarnings("unchecked")
//    @Override
    protected String doIntercept(ActionInvocation invocation) throws Exception {
        log.debug("Start AuthenticationInterceptor");
        final Object action = invocation.getAction();
        final ActionContext context = invocation.getInvocationContext();
        Map session = context.getSession();
        log.info("Action: " + action.getClass().getSimpleName());


        UserContext uCtx = UserContext.getInstance();    // get user profile from http session
        if (uCtx == null) {       // not logged in
            return Action.LOGIN;  // “message is defined as a global result in struts.xml

//        log.info(uctx.getUser().getLoginName() + " - " + ActionContext.getContext().getName());
            // now you have user info and action name, to do privilege check here
        }
        // if permission denied, return “message�?directly
        return invocation.invoke();          // pass the privilege check
    }

    @Override
    public void destroy() {
        log.debug("AuthenticationInterceptor destroyed.");
    }

    @Override
    public String intercept(ActionInvocation invocation) throws Exception {
        log.debug("Start AuthenticationInterceptor intercept");
        final Object action = invocation.getAction();
        final ActionContext context = invocation.getInvocationContext();
        Map session = context.getSession();


        UserContext uCtx = UserContext.getInstance();    // get user profile from http session
        log.info("Action: " + action.getClass().getSimpleName());
        if (uCtx == null) {       // not logged in
//            return Action.LOGIN;  // “message�?is defined as a global result in struts.xml

//        log.info(uctx.getUser().getLoginName() + " - " + ActionContext.getContext().getName());
            // now you have user info and action name, to do privilege check here
        }
        // if permission denied, return “message�?directly
        return invocation.invoke();          // pass the privilege check.
    }

    @Override
    public void init() {
        log.debug("AuthenticationInterceptor initialized.");
    }
}
