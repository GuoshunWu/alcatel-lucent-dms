package com.alcatel_lucent.dms.service;

import com.alcatel_lucent.dms.model.Product;

/**
 * Created by IntelliJ IDEA.
 * User: guoshunw
 * Date: 12-8-23
 * Time: 上午11:26
 * To change this template use File | Settings | File Templates.
 */
public interface ProductService {
    Product create();
    void delete(Long id);
    Product retrieve(Long id);
    
    /**
     * Remove an application from a product, without deleting it.
     * @param productId
     * @param appId
     */
    void removeApplicationFromProduct(Long productId, Long appId);
    
    /**
     * Remove an application from all products, and delete it.
     * AppBase is also removed if no other application.
     * @param appId
     */
    void deleteApplication(Long appId);
}
