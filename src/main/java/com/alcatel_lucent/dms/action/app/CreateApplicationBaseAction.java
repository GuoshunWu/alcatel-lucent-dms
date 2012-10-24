package com.alcatel_lucent.dms.action.app;

import com.alcatel_lucent.dms.action.JSONAction;
import com.alcatel_lucent.dms.service.ProductService;

@SuppressWarnings("serial")
public class CreateApplicationBaseAction extends JSONAction {
	
	private ProductService productService;
	
	private Long prod;	// input
	private String name;	// input
	private Long id;	// output

	@Override
	protected String performAction() throws Exception {
		log.info("CreateApplicationBaseAction: prod=" + prod + ",name=" + name);
		id = productService.createApplicationBase(prod, name);
		return SUCCESS;
	}

	public ProductService getProductService() {
		return productService;
	}

	public void setProductService(ProductService productService) {
		this.productService = productService;
	}

	public Long getProd() {
		return prod;
	}

	public void setProd(Long prod) {
		this.prod = prod;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

}
