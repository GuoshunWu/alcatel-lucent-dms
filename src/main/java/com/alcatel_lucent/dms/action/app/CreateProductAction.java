package com.alcatel_lucent.dms.action.app;

import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;

import com.alcatel_lucent.dms.action.JSONAction;
import com.alcatel_lucent.dms.model.ProductBase;
import com.alcatel_lucent.dms.service.DaoService;
/**
 * Action of creating a product
 *
 * @author allany
 */
@ParentPackage("json-default")
@Result(type="json", params={"noCache","true","ignoreHierarchy","false","includeProperties","id,message,status"})
public class CreateProductAction extends JSONAction {

    public void setDaoService(DaoService daoService) {
        this.daoService = daoService;
    }

    private DaoService daoService;

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
        log.info("Create product: " + name);
        //TODO create product
        ProductBase pb=new ProductBase();
        pb.setName(name);
        pb= (ProductBase) daoService.create(pb);

        if(null==pb){
            setStatus(-1);
            setMessage("Create product "+name+" fail.");
            return SUCCESS;
        }
        id=pb.getId();
        setStatus(0);
        //know how the locale is.
        System.out.println(getLocale());
        setMessage("Create product "+name+" success!");
        return SUCCESS;
    }



}
