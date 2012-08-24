package com.alcatel_lucent.dms.action;

import org.apache.log4j.Logger;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;

import com.opensymphony.xwork2.ActionSupport;

public class BaseAction extends ActionSupport{
	
	protected Logger log = Logger.getLogger(this.getClass());
}
