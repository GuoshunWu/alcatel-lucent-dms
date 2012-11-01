package com.alcatel_lucent.dms.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.model.Application;
import com.alcatel_lucent.dms.model.ApplicationBase;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.Product;
import com.alcatel_lucent.dms.model.ProductBase;
import com.alcatel_lucent.dms.model.Task;

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
    
    public void removeApplicationFromProduct(Long productId, Collection<Long> appIds) {
    	Product product = (Product) dao.retrieve(Product.class, productId);
    	for (Long appId : appIds) {
    		product.removeApplication(appId);
    	}
    }

    @Override
    public Long addApplicationToProduct(Long productId, Long appId) throws BusinessException {
        Application app= (Application) dao.retrieve(Application.class, appId);
        Long appBaseId = app.getBase().getId();
        Product product = (Product) dao.retrieve(Product.class, productId);
        if (product.getApplications() != null) {
        	for (Application existApp : product.getApplications()) {
        		if (existApp.getBase().getId() == appBaseId) {
        			throw new BusinessException(BusinessException.APPLICATION_ALREADY_IN_PRODUCT);
        		}
        	}
        }
        product.getApplications().add(app);
        return app.getId();
    }

    @Override
    public void changeApplicationInProduct(Long productId, Long oldAppId, Long newAppId) {
        removeApplicationFromProduct(productId,oldAppId);
        addApplicationToProduct(productId,newAppId);
    }

    public Long deleteApplication(Long appId) {
    	Application app = (Application) dao.retrieve(Application.class, appId);
    	if (app.getDictionaries() != null && app.getDictionaries().size() > 0) {
    		throw new BusinessException(BusinessException.APPLICATION_NOT_EMPTY);
    	}
    	// remove links to products
    	String hql = "select distinct p from Product p join p.applications as a where a.id=:id";
    	Map param = new HashMap();
    	param.put("id", appId);
    	Collection<Product> products = dao.retrieve(hql, param);
    	for (Product prod : products) {
    		prod.removeApplication(appId);
    	}
    	// delete application
    	dao.delete(app);
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
    
    public void deleteProduct(Long productId) throws BusinessException {
    	Product product = (Product) dao.retrieve(Product.class, productId);
    	if (product.getApplications() != null && product.getApplications().size() > 0) {
    		throw new BusinessException(BusinessException.PRODUCT_NOT_EMPTY);
    	}
    	String hql = "from Task where product.id=:productId";
    	Map param = new HashMap();
    	param.put("productId", productId);
    	Collection<Task> tasks = dao.retrieve(hql, param);
    	if (!tasks.isEmpty()) {
    		throw new BusinessException(BusinessException.PRODUCT_CONTAINS_TASK);
    	}
    	dao.delete(product);
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
	
	public Long createApplication(Long appBaseId, String version, Long inheritAppId) throws BusinessException {
		version = version.trim();
		ApplicationBase appBase = (ApplicationBase) dao.retrieve(ApplicationBase.class, appBaseId);
		Application app = findApplication(appBaseId, version);
		if (app != null) {
			throw new BusinessException(BusinessException.APPLICATION_ALREADY_EXIST, version);
		} 
		app = new Application();
		app.setBase(appBase);
		app.setVersion(version);
		app = (Application) dao.create(app);
		if (inheritAppId != null) {
			Application inheritApp = (Application) dao.retrieve(Application.class, inheritAppId);
			if (inheritApp.getDictionaries() != null) {
				app.setDictionaries(new HashSet<Dictionary>(inheritApp.getDictionaries()));
			}
		}
		return app.getId();
	}

	private Application findApplication(Long appBaseId, String version) {
		String hql = "from Application where base.id=:appBaseId and version=:version";
		Map param = new HashMap();
		param.put("appBaseId", appBaseId);
		param.put("version", version);
		return (Application) dao.retrieveOne(hql, param);
	}

	private ApplicationBase findApplicationBaseByName(Long productBaseId,
			String name) {
		String hql = "from ApplicationBase where productBase.id=:pbId and name=:name";
		Map param = new HashMap();
		param.put("pbId", productBaseId);
		param.put("name", name);
		return (ApplicationBase) dao.retrieveOne(hql, param);
	}

	@Override
	public Long createProduct(Long productBaseId, String version, Long inheritProdId)
			throws BusinessException {
		version = version.trim();
		ProductBase prodBase = (ProductBase) dao.retrieve(ProductBase.class, productBaseId);
		Product prod = findProduct(productBaseId, version);
		if (prod != null) {
			throw new BusinessException(BusinessException.PRODUCT_ALREADY_EXISTS, version);
		}
		prod = new Product();
		prod.setBase(prodBase);
		prod.setVersion(version);
		prod = (Product) dao.create(prod);
		if (inheritProdId != null) {
			Product inheritProd = (Product) dao.retrieve(Product.class, inheritProdId);
			if (inheritProd.getApplications() != null) {
				prod.setApplications(new HashSet<Application>(inheritProd.getApplications()));
			}
		}
		return prod.getId();
	}

	private Product findProduct(Long productBaseId, String version) {
		String hql = "from Product where base.id=:productBaseId and version=:version";
		Map param = new HashMap();
		param.put("productBaseId", productBaseId);
		param.put("version", version);
		return (Product) dao.retrieveOne(hql, param);
	}


}
