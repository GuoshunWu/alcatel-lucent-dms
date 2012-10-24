package com.alcatel_lucent.dms.action.app;

import org.apache.struts2.convention.annotation.Result;

import com.alcatel_lucent.dms.action.JSONAction;
import com.alcatel_lucent.dms.service.ProductService;

@SuppressWarnings("serial")
@Result(type="json", params={"noCache","true","ignoreHierarchy","false","includeProperties","status,message,id"})
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
