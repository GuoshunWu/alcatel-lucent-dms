package com.alcatel_lucent.dms.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
    
    public Long createProductBase(String name) throws BusinessException {
    	ProductBase pb = findProductBaseByName(name);
    	if (pb != null) {
    		throw new BusinessException(BusinessException.PRODUCT_BASE_ALREADY_EXISTS, name);
    	} else {
    		pb = new ProductBase();
    		pb.setName(name);
    		pb = (ProductBase) dao.create(pb);
    		return pb.getId();
    	}
    }
    
    private ProductBase findProductBaseByName(String name) {
		String hql = "from ProductBase where name=:name";
		Map param = new HashMap();
		param.put("name", name);
		return (ProductBase) dao.retrieveOne(hql, param);
	}

	public Long createApplicationBase(Long productBaseId, String name) throws BusinessException {
		ApplicationBase ab = findApplicationBaseByName(productBaseId, name);
		if (ab != null) {
			throw new BusinessException(BusinessException.APPLICATION_BASE_ALREADY_EXISTS, name);
		} else {
			ab = new ApplicationBase();
			ab.setName(name);
			ab.setProductBase((ProductBase) dao.retrieve(ProductBase.class, productBaseId));
			ab = (ApplicationBase) dao.create(ab);
			return ab.getId();
		}
		
	}

	private ApplicationBase findApplicationBaseByName(Long productBaseId,
			String name) {
		String hql = "from ApplicationBase where productBase.id=:pbId and name=:name";
		Map param = new HashMap();
		param.put("pbId", productBaseId);
		param.put("name", name);
		return (ApplicationBase) dao.retrieveOne(hql, param);
	}


}
