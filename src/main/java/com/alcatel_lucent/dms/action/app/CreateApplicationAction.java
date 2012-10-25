package com.alcatel_lucent.dms.action.app;

import org.apache.struts2.convention.annotation.Result;

import com.alcatel_lucent.dms.action.JSONAction;
import com.alcatel_lucent.dms.service.ProductService;

@Result(type = "json", params = {"noCache", "true", "ignoreHierarchy", "false", "includeProperties", "id,message,status"})
@SuppressWarnings("serial")
public class CreateApplicationAction extends JSONAction {

	private ProductService productService;
	
	private Long id;	// IN: app base id, OUT: new app id 
	private String version;
	private Long dupVersionId;	// duplicate dictionaries from another app
	
	@Override
	protected String performAction() throws Exception {
		log.info("CreateApplicationAction: id=" + id + ", version=" + version + ", dupVersionId=" + dupVersionId);
		if (dupVersionId != null && dupVersionId == -1) {
			dupVersionId = null;
		}
		id = productService.createApplication(id, version, dupVersionId);
		setMessage(getText("message.success"));
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

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Long getDupVersionId() {
		return dupVersionId;
	}

	public void setDupVersionId(Long dupVersionId) {
		this.dupVersionId = dupVersionId;
	}

}
