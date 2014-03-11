package com.alcatel_lucent.dms.interceptors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alcatel_lucent.dms.service.AuthenticationService;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

@SuppressWarnings("serial")
@Component("apiAuthenticationInterceptor")
public class APIAuthenticationInterceptor extends AbstractInterceptor {
	
	protected static Logger log = LoggerFactory.getLogger(APIAuthenticationInterceptor.class);
	
	@Autowired
	private AuthenticationService authenticationService;

	@Override
	public String intercept(ActionInvocation invocation) throws Exception {
		ActionContext context = invocation.getInvocationContext();
		HttpServletRequest request = (HttpServletRequest) context.get(ServletActionContext.HTTP_REQUEST);
		String authorization = request.getHeader("Authorization");
    	if (authorization == null || ! authorization.startsWith("Basic ")) {
    		return "401";
    	}
    	String base64 = authorization.substring(6).trim();
    	String credential = new String(Base64.decodeBase64(base64));
    	String[] userPass = credential.split(":", 2);
    	if (userPass.length < 2) {
    		return "401";
    	} else {
    		if (null == authenticationService.login(userPass[0], userPass[1])) {
    			return "403";
    		}
        	log.info("[API] " + userPass[0] + " - " + invocation.getAction().getClass().getSimpleName());
        	return invocation.invoke();
    	}
	}

}
