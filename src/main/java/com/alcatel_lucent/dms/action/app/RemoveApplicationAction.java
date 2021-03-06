package com.alcatel_lucent.dms.action.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;

import com.alcatel_lucent.dms.action.JSONAction;
import com.alcatel_lucent.dms.service.ProductService;

/**
 * Delete an application or remove application from a product
 *
 * @author allany
 */
@ParentPackage("dms-json")
@Result(type = "json", params = {"noCache", "true", "ignoreHierarchy", "false", "includeProperties", "id,permanent,message,status"})

public class RemoveApplicationAction extends JSONAction {

    public void setProductService(ProductService productService) {
        this.productService = productService;
    }

    private ProductService productService;

    private boolean permanent;
    private String oper;
    private String id;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String performAction() throws Exception {
        log.debug("productId=%d,id=%s, oper=%s, permanent=%b.", new Object[]{productId, id, oper, permanent});

        if (permanent) {
            id = "" + productService.deleteApplication(Long.valueOf(id));
        } else {
            productService.removeApplicationFromProduct(productId, toIdList(id));
        }

        setStatus(0);
        setMessage(getText("message.success"));

        return SUCCESS;
    }
}
