package com.alcatel_lucent.dms.action.app;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;

import com.alcatel_lucent.dms.action.JSONAction;
import com.alcatel_lucent.dms.model.Application;
import com.alcatel_lucent.dms.model.ApplicationBase;
import com.alcatel_lucent.dms.model.Product;
import com.alcatel_lucent.dms.service.DaoService;
import com.alcatel_lucent.dms.service.ProductService;

/**
 * Action of creating a product
 * @deprecated
 * @author allany
 */
@ParentPackage("dms-json")
@Result(type = "json", params = {"noCache", "true", "ignoreHierarchy", "false", "includeProperties", "appId,appBaseId,message,status"})

public class CreateOrAddApplicationAction extends JSONAction {
    public void setDaoService(DaoService daoService) {
        this.daoService = daoService;
    }

    private DaoService daoService;


    public void setProductService(ProductService productService) {
        this.productService = productService;
    }

    private ProductService productService;

    //request parameter
    private Long productId;
    private Long appBaseId;
    private String appBaseName;
    private Long appId;
    private String appVersion;


    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getAppBaseId() {
        return appBaseId;
    }

    public void setAppBaseId(Long appBaseId) {
        this.appBaseId = appBaseId;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public String getAppBaseName() {
        return appBaseName;
    }

    public void setAppBaseName(String appBaseName) {
        this.appBaseName = appBaseName;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String performAction() throws Exception {
        log.debug(String.format("productId=%d, appBaseId=%d, appBaseName=%s, appId=%d, appVersion=%s.", productId, appBaseId, appBaseName, appId, appVersion));

        Map<String, Long> param = new HashMap<String, Long>();
        param.put("id", productId);
        Product product = (Product) daoService.retrieveOne("from Product where id=:id", param, Arrays.asList("base").toArray(new String[0]));
        ApplicationBase appBase = null;
        if (-1 == appBaseId) { //we need create new applicationBase
            appBase = new ApplicationBase();
            appBase.setProductBase(product.getBase());
            appBase.setName(appBaseName);
            appBase = (ApplicationBase) daoService.create(appBase);
        } else {
            appBase = (ApplicationBase) daoService.retrieve(ApplicationBase.class, appBaseId);
        }

        Application app = null;
        if (-1 == appId) { // we need create new application
            app = new Application();
            app.setBase(appBase);
            app.setVersion(appVersion);
            app = (Application) daoService.create(app);
        } else {
            app = (Application) daoService.retrieve(Application.class, appId);
        }

        appId=productService.addApplicationToProduct(productId,app.getId());
        if (null == appId) {
            setStatus(-1);
            setMessage(appBaseName + " already have a version in "+product.getBase().getName() + " version " + product.getVersion());
            return SUCCESS;
        }


        setStatus(0);
        setMessage("Add new version " + app.getVersion() + " to " + product.getBase().getName() + " version " + product.getVersion() + " success.");
        return SUCCESS;
    }
}
