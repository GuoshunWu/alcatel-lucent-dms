package com.alcatel_lucent.dms.action.app;

import com.alcatel_lucent.dms.action.JSONAction;
import com.alcatel_lucent.dms.service.ProductService;


@SuppressWarnings("serial")
public class RemoveApplicationBaseAction extends JSONAction {

	private ProductService productService;
	
	private Long id;
	
	@Override
	protected String performAction() throws Exception {
		productService.deleteApplicationBase(id);
		return SUCCESS;
	}

	public ProductService getProductService() {
		return productService;
	}

	public void setProductService(ProductService productService) {
		this.productService = productService;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

}
