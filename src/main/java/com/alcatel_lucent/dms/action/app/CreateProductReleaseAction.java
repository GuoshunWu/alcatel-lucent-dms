package com.alcatel_lucent.dms.action.app;

import com.alcatel_lucent.dms.action.JSONAction;
import com.alcatel_lucent.dms.model.Application;
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
@Result(type = "json", params = {"noCache", "true", "ignoreHierarchy", "false", "includeProperties", "id,message,status"})

@SuppressWarnings("unchecked")
public class CreateProductReleaseAction extends JSONAction {

    public void setDaoService(DaoService daoService) {
        this.daoService = daoService;
    }

    private DaoService daoService;

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
        log.setLevel(Level.DEBUG);
        log.debug("Create product release version: " + version + ", product base id=" + id + ", dup version id=" + dupVersionId);

        ProductBase pb= (ProductBase) daoService.retrieve(ProductBase.class,id);
        Product product = new Product();
        product.setVersion(version);
        product.setBase(pb);
        if (-1 != dupVersionId) {
            String hsql="select app from Product p join p.applications as app where p.id=:id";
            Map<String, Long> params = new HashMap<String, Long>();
            params.put("id", dupVersionId);
            List<Application> apps= daoService.retrieve(hsql, params);
            product.setApplications(new HashSet<Application>(apps));
        }
        product = (Product) daoService.create(product);

        if (null == product) {
            setStatus(-1);
            setMessage("Create product " + version + " fail.");
            return SUCCESS;
        }

        id = product.getId();
        setStatus(0);
        setMessage("Create product release version " + version + " success!");
        return SUCCESS;
    }
}
