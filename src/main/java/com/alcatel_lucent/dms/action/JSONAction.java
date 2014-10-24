package com.alcatel_lucent.dms.action;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;

/**
 * Base class of JSON-style actions.
 * By default it products json string containing status and message as action result.
 * Subclass is responsible to set status and message before return.
 * Subclass should always return SUCCESS, if action failed, it set status with proper error code. 
 * @author allany
 *
 */
@ParentPackage("dms-json")
@Result(type="json", params={"noCache","true","ignoreHierarchy","false","includeProperties","status,message"})
@SuppressWarnings("serial")
abstract public class JSONAction extends BaseAction {
	protected int status;
	protected String message;

	public void setStatus(int status) {
		this.status = status;
	}
	public int getStatus() {
		return status;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getMessage() {
		return message;
	}
    
    abstract protected String performAction() throws Exception;
    
    public String execute() {
        try {
            return performAction();
        } catch (Exception e) {
        	log.error(e.getMessage(), e);
            e.printStackTrace();
            setStatus(-1);
            setMessage(e.toString());
            return SUCCESS;
        }
    }
    


}
