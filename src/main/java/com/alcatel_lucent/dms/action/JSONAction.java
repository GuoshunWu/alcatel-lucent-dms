package com.alcatel_lucent.dms.action;

import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;

import java.util.HashMap;
import java.util.Map;

/**
 * Base class of JSON-style actions.
 * By default it products json string containing status and message as action result.
 * Subclass is responsible to set status and message before return.
 * Subclass should always return SUCCESS, if action failed, it set status with proper error code. 
 * @author allany
 *
 */
@ParentPackage("json-default")
@Result(type="json", params={"noCache","true","ignoreHierarchy","false","includeProperties","status,message"})
abstract public class JSONAction extends BaseAction {
	private int status;
	private String message;

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
            e.printStackTrace();
            setStatus(-1);
            setMessage(e.getMessage());
            return SUCCESS;
        }
    }

}
