package com.alcatel_lucent.dms.action.app;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;

import com.alcatel_lucent.dms.action.JSONAction;
import com.alcatel_lucent.dms.model.Application;
import com.alcatel_lucent.dms.model.Product;
import com.alcatel_lucent.dms.model.ProductBase;
import com.alcatel_lucent.dms.service.DaoService;
import com.alcatel_lucent.dms.service.ProductService;

/**
 * Action of creating a product
 *
 * @author allany
 */
@SuppressWarnings("serial")
@ParentPackage("dms-json")
@Result(type = "json", params = {"noCache", "true", "ignoreHierarchy", "false", "includeProperties", "id,message,status"})
public class CreateProductReleaseAction extends JSONAction {

    private ProductService productService;

    // input parameters
    private String version;
    private Long dupVersionId;

    //both input parameter and response attributes
    //if create success, then the id will be the product id.
    private Long id;


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

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String performAction() throws Exception {
        log.debug("Create product release version: " + version + ", product base id=" + id + ", dup version id=" + dupVersionId);
        if (dupVersionId != null && dupVersionId == -1) {
        	dupVersionId = null;
        }
        id = productService.createProduct(id, version, dupVersionId);
        setMessage(getText("message.success"));
        return SUCCESS;
    }

	public ProductService getProductService() {
		return productService;
	}

	public void setProductService(ProductService productService) {
		this.productService = productService;
	}
}
