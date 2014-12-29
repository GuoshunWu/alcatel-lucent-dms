package com.alcatel_lucent.dms.action.api;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;

import com.alcatel_lucent.dms.action.BaseAction;

@ParentPackage("dms-api")
@SuppressWarnings("serial")
public abstract class APIAction extends BaseAction implements ServletRequestAware, ServletResponseAware {
	
	protected HttpServletRequest httpServletRequest;
	protected HttpServletResponse httpServletResponse;

	@Override
	public void setServletRequest(HttpServletRequest request) {
		this.httpServletRequest = request;
	}
	
	@Override
	public void setServletResponse(HttpServletResponse response) {
		this.httpServletResponse = response;
	}

    abstract protected String performAction() throws Exception;
    
    @Override
    public String execute() {
    	try {
        	return performAction();
    	} catch (Exception e) {
    		e.printStackTrace();
    		return ERROR;
    	}
    }
}
