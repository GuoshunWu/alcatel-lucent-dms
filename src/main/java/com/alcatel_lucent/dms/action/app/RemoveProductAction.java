package com.alcatel_lucent.dms.action.app;

import com.alcatel_lucent.dms.action.JSONAction;
import com.alcatel_lucent.dms.service.ProductService;

@SuppressWarnings("serial")
public class RemoveProductAction extends JSONAction {
	
	private ProductService productService;
	
	private Long id;	// product id

	@Override
	protected String performAction() throws Exception {
		log.info("RemoveProductAction: id=" + id);
		productService.deleteProduct(id);
		setMessage(getText("message.success"));
		return SUCCESS;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

}
