package com.alcatel_lucent.dms.action.app;

import com.alcatel_lucent.dms.action.BaseAction;

public class CreateProductAction extends BaseAction {
	
	private String name;
	
	public String execute() {
		log.info("###Product name1: " + name);
		return null;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	

}
