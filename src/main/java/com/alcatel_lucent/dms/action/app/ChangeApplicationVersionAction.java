package com.alcatel_lucent.dms.action.app;

import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;

import com.alcatel_lucent.dms.action.JSONAction;
import com.alcatel_lucent.dms.service.ProductService;

/**
 * Action of creating a product
 *
 * @author allany
 */
@ParentPackage("dms-json")
@Result(type = "json", params = {"noCache", "true", "ignoreHierarchy", "false", "includeProperties", "productId,id,message,status"})

public class ChangeApplicationVersionAction extends JSONAction {

    public void setProductService(ProductService productService) {
        this.productService = productService;
    }

    private ProductService productService;

    private Long id;
    private Long productId;
    private Long newAppId;
    private String version;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Long getNewAppId() {
        return newAppId;
    }

    public void setNewAppId(Long newAppId) {
        this.newAppId = newAppId;
    }

    public String performAction() throws Exception {
//        log.setLevel(Level.DEBUG);
        log.debug(String.format("id=%d, productId=%d, version=%s, newAppId=%d.",id,productId,version, newAppId));
        productService.changeApplicationInProduct(productId,id,newAppId);
        setStatus(0);
        setMessage("Change success.");

        return SUCCESS;
    }
}
