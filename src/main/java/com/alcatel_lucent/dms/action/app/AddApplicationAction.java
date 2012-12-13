package com.alcatel_lucent.dms.action.app;

import com.alcatel_lucent.dms.action.JSONAction;
import com.alcatel_lucent.dms.service.ProductService;

@SuppressWarnings("serial")
public class AddApplicationAction extends JSONAction {
	
	private ProductService productService;

    private Long productId;
    private Long appId;

	@Override
	protected String performAction() throws Exception {
		log.info("AddApplication: productId=" + productId + ", appId=" + appId);
		productService.addApplicationToProduct(productId, appId);
		setMessage(getText("message.success"));
		return SUCCESS;
	}

	public ProductService getProductService() {
		return productService;
	}

	public void setProductService(ProductService productService) {
		this.productService = productService;
	}

	public Long getProductId() {
		return productId;
	}

	public void setProductId(Long productId) {
		this.productId = productId;
	}

	public Long getAppId() {
		return appId;
	}

	public void setAppId(Long appId) {
		this.appId = appId;
	}

}
