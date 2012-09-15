package com.alcatel_lucent.dms.action.app;

import com.alcatel_lucent.dms.action.JSONAction;
import com.alcatel_lucent.dms.service.ProductService;
import org.apache.log4j.Level;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;

/**
 * Action of creating a product
 *
 * @author allany
 */
@ParentPackage("json-default")
@Result(type = "json", params = {"noCache", "true", "ignoreHierarchy", "false", "includeProperties", "id,permanent,message,status"})

public class RemoveApplicationAction extends JSONAction {

    public void setProductService(ProductService productService) {
        this.productService = productService;
    }

    private ProductService productService;

    private boolean permanent;
    private String oper;
    private Long id;
    private Long productId;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public boolean isPermanent() {
        return permanent;
    }

    public void setPermanent(boolean permanent) {
        this.permanent = permanent;
    }

    public String getOper() {
        return oper;
    }

    public void setOper(String oper) {
        this.oper = oper;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String performAction() throws Exception {
        log.setLevel(Level.DEBUG);
        log.debug(String.format("productId=%d,id=%d, oper=%s, permanent=%b.",productId, id, oper, permanent));

        if (permanent) {
            id=productService.deleteApplication(id);
        } else {
            productService.removeApplicationFromProduct(productId,id);
        }
        setStatus(0);
        setMessage("Change success.");

        return SUCCESS;
    }
}
