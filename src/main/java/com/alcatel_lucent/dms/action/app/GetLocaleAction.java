package com.alcatel_lucent.dms.action.app;

import com.opensymphony.xwork2.ActionContext;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;

import com.alcatel_lucent.dms.action.JSONAction;

/**
 * Action of creating a product
 *
 * @author allany
 */
@ParentPackage("json-default")
@Result(type = "json", params = {"noCache", "true", "ignoreHierarchy", "false", "includeProperties", "message,status"})

public class GetLocaleAction extends JSONAction {
    public String performAction() throws Exception {
        setStatus(0);
        setMessage(getLocale().toString());
        return SUCCESS;
    }
}
