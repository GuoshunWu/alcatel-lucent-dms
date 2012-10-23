package com.alcatel_lucent.dms.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import net.sf.json.JSONArray;
import net.sf.json.JsonConfig;
import net.sf.json.util.PropertyFilter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.model.Application;
import com.alcatel_lucent.dms.model.ApplicationBase;
import com.alcatel_lucent.dms.model.Product;
import com.alcatel_lucent.dms.model.ProductBase;

/**
 * Created by IntelliJ IDEA.
 * User: guoshunw
 * Date: 12-8-23
 * Time: 上午11:27
 * To change this template use File | Settings | File Templates.
 */

@Service("productService")
@SuppressWarnings("unchecked")
public class ProductServiceImpl implements ProductService {

    @Autowired
    private DaoService dao;
    
    JsonConfig config = new JsonConfig();


    public Product create() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void delete(Long id) {
        //To change body of implemented methods use File | Settings | File Templates.
    }


    public Product retrieve(Long id) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }


    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public JSONArray retrieveAll() {

        config.setJsonPropertyFilter(new PropertyFilter() {
            List<String> excludesFields = Arrays.asList("products", "dictionaryBases", "productBase", "applications");

            @Override
            public boolean apply(Object source, String name, Object value) {
                return excludesFields.contains(name);
            }
        });

        Collection<ProductBase> pBases = (Collection<ProductBase>) dao.retrieve("from ProductBase", new String[]{"applicationBases"});
        return JSONArray.fromObject(pBases,config);
    }
    
    public void removeApplicationFromProduct(Long productId, Long appId) {
    	Product product = (Product) dao.retrieve(Product.class, productId);
    	product.removeApplication(appId);
    }

    @Override
    public Long addApplicationToProduct(Long productId, Long appId) {
        Application app= (Application) dao.retrieve(Application.class, appId);
        if(null==app){
            return null;
        }
        Product product = (Product) dao.retrieve(Product.class, productId);
        product.getApplications().add(app);
        return app.getId();
    }

    @Override
    public void changeApplicationInProduct(Long productId, Long oldAppId, Long newAppId) {
        removeApplicationFromProduct(productId,oldAppId);
        addApplicationToProduct(productId,newAppId);
    }

    public Long deleteApplication(Long appId) {
    	// remove links to products
    	String hql = "select distinct p from Product p join p.applications as a where a.id=:id";
    	Map param = new HashMap();
    	param.put("id", appId);
    	Collection<Product> products = dao.retrieve(hql, param);
    	for (Product prod : products) {
    		prod.removeApplication(appId);
    	}
    	Application app = (Application) dao.retrieve(Application.class, appId);
    	ApplicationBase appBase = app.getBase();
    	
    	// remove links to dictionaries
    	app.setDictionaries(null);
    	
    	// delete application
    	dao.delete(app);
    	
    	// delete appBase if it doesn't contain other application
/*
    	if (appBase.getApplications() == null || appBase.getApplications().size() == 0 ||
    			appBase.getApplications().size() == 1 && appBase.getApplications().iterator().next().getId().equals(appId)) {
    		dao.delete(appBase);
            return appBase.getId();
    	}
*/
        return null;
    }
    
    public void deleteApplicationBase(Long appBaseId) throws BusinessException {
    	ApplicationBase appBase = (ApplicationBase) dao.retrieve(ApplicationBase.class, appBaseId);
    	if (appBase.getApplications() != null && appBase.getApplications().size() > 0 ||
    			appBase.getDictionaryBases() != null && appBase.getDictionaryBases().size() > 0) {
    		throw new BusinessException(BusinessException.APPLICATION_BASE_NOT_EMPTY);
    	}
    	dao.delete(appBase);
    }
    
    public void deleteProductBase(Long prodBaseId) throws BusinessException {
    	ProductBase prodBase = (ProductBase) dao.retrieve(ProductBase.class, prodBaseId);
    	if (prodBase.getProducts() != null && prodBase.getProducts().size() > 0 ||
    			prodBase.getApplicationBases() != null && prodBase.getApplicationBases().size() > 0) {
    		throw new BusinessException(BusinessException.PRODUCT_BASE_NOT_EMPTY);
    	}
    	dao.delete(prodBase);
    }

}
