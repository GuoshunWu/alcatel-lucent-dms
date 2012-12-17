package com.alcatel_lucent.dms.action.app;

import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;

import com.alcatel_lucent.dms.action.JSONAction;
import com.alcatel_lucent.dms.model.ProductBase;
import com.alcatel_lucent.dms.service.DaoService;
import com.alcatel_lucent.dms.service.ProductService;
/**
 * Action of creating a product
 *
 * @author allany
 */
@SuppressWarnings("serial")
@ParentPackage("json-default")
@Result(type="json", params={"noCache","true","ignoreHierarchy","false","includeProperties","id,message,status"})
public class CreateProductAction extends JSONAction {

    private ProductService productService;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // input parameters
    private String name;
    
    // response attributes
    private Long id;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    protected String performAction() throws Exception {
        log.info("Create product base: " + name);
        id = productService.createProductBase(name);
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
