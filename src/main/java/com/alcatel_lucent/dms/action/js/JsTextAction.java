package com.alcatel_lucent.dms.action.js;

import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;

import com.opensymphony.xwork2.ActionSupport;

@SuppressWarnings("serial")
@Action(results={@Result(name="success", location="/include/js_text.jsp", type="dispatcher")})
public class JsTextAction extends ActionSupport {
	
	private Set<String> keys = new HashSet<String>();
	
	public String execute() {
		ResourceBundle bundle = ResourceBundle.getBundle("js_en");
		keys.addAll(bundle.keySet());
		return SUCCESS;
	}

	public Set<String> getKeys() {
		return keys;
	}

	public void setKeys(Set<String> keys) {
		this.keys = keys;
	}
}
