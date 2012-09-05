package com.alcatel_lucent.dms.action.app;

import com.alcatel_lucent.dms.action.JSONAction;
import com.alcatel_lucent.dms.model.Application;
import com.alcatel_lucent.dms.model.ApplicationBase;
import com.alcatel_lucent.dms.model.Product;
import com.alcatel_lucent.dms.model.ProductBase;
import com.alcatel_lucent.dms.service.DaoService;
import org.apache.log4j.Level;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;

import java.util.*;

/**
 * Action of creating a product
 *
 * @author allany
 */
@ParentPackage("json-default")
@Result(type = "json", params = {"noCache", "true", "ignoreHierarchy", "false", "includeProperties", "message,status"})

@SuppressWarnings("unchecked")
public class CreateOrAddApplication extends JSONAction {

    public void setDaoService(DaoService daoService) {
        this.daoService = daoService;
    }

    private DaoService daoService;

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
        Product product = (Product) daoService.retrieveOne("from Product where id=:id", param, Arrays.asList("applications","base").toArray(new String[0]));
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

        //todo: persistence
        product.getApplications().add(app);
        daoService.update(product);
        if (null == product) {
            setStatus(-1);
            setMessage("Add new version " + appVersion + " to product fail.");
            return SUCCESS;
        }

        setStatus(0);
        setMessage("Add new version " + app.getVersion() + " to product " + product.getBase().getName() + " version " + product.getVersion() + " success.");
        return SUCCESS;
    }
}
