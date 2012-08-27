package com.alcatel_lucent.dms.action.app;

import com.alcatel_lucent.dms.action.JSONAction;

/**
 * Action of creating a product
 * @author allany
 *
 */
public class CreateProductAction extends JSONAction {
	
	// input parameters
	private String name;
	
	public String execute() {
		log.info("Create product  2: " + name);
		//TODO create product
		setStatus(0);
		setMessage("OK");
		return SUCCESS;
	}

}
