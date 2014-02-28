package com.alcatel_lucent.dms.action.app;

import org.apache.struts2.convention.annotation.Result;

import com.alcatel_lucent.dms.action.JSONAction;
import com.alcatel_lucent.dms.model.Application;
import com.alcatel_lucent.dms.model.Product;
import com.alcatel_lucent.dms.service.DaoService;
import com.alcatel_lucent.dms.service.ProductService;

@Result(type = "json", params = {"noCache", "true", "ignoreHierarchy", "false", "includeProperties", "id,message,status,productBaseId,productBaseName,versions\\[\\d+\\]\\.id,versions\\[\\d+\\]\\.version"})
@SuppressWarnings("serial")
public class CreateApplicationAction extends JSONAction {

	private DaoService dao;
	private ProductService productService;
	
	private Long id;	// IN: app base id, OUT: new app id 
	private String version;
	private Long dupVersionId;	// duplicate dictionaries from another app
	
	private Long productBaseId;
	private String productBaseName;
	private Product[] versions;
	
	@Override
	protected String performAction() throws Exception {
		log.info("CreateApplicationAction: id=" + id + ", version=" + version + ", dupVersionId=" + dupVersionId);
		if (dupVersionId != null && dupVersionId == -1) {
			dupVersionId = null;
		}
		Application app = productService.createApplication(id, version, dupVersionId);
		id = app.getId();
		if (!app.getBase().getProductBase().getProducts().isEmpty()) {
			setProductBaseId(app.getBase().getProductBase().getId());
			setProductBaseName(app.getBase().getProductBase().getName());
			setVersions(app.getBase().getProductBase().getProducts().toArray(new Product[] {}));
		}
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

	public DaoService getDao() {
		return dao;
	}

	public void setDao(DaoService dao) {
		this.dao = dao;
	}

	public Long getProductBaseId() {
		return productBaseId;
	}

	public void setProductBaseId(Long productBaseId) {
		this.productBaseId = productBaseId;
	}

	public String getProductBaseName() {
		return productBaseName;
	}

	public void setProductBaseName(String productBaseName) {
		this.productBaseName = productBaseName;
	}

	public Product[] getVersions() {
		return versions;
	}

	public void setVersions(Product[] versions) {
		this.versions = versions;
	}

}
