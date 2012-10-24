package com.alcatel_lucent.dms.service;

import com.alcatel_lucent.dms.BusinessException;

/**
 * Created by IntelliJ IDEA.
 * User: guoshunw
 * Date: 12-8-23
 * Time: 11:26AM
 * To change this template use File | Settings | File Templates.
 */
public interface ProductService {
    
	/**
	 * Create product base.
	 * @param name product name, must be unique
	 * @return product base id
	 * @throws BusinessException
	 */
    Long createProductBase(String name) throws BusinessException;
    
    /**
     * Create application base
     * @param productBaseId id of product base the application belongs to
     * @param name application name, must be unique inside product
     * @return application base id
     * @throws BusinessException
     */
    Long createApplicationBase(Long productBaseId, String name) throws BusinessException;
    
    /**
     * Remove an application from a product, without deleting it.
     * @param productId
     * @param appId
     */
    void removeApplicationFromProduct(Long productId, Long appId);

    /**
     * Add an application to a product
     * @param productId
     * @param appId
     * @return new added Application ID or null if adding failed
     *
     */
    Long addApplicationToProduct(Long productId, Long appId);

    /**
     * Add an application to a product
     * @param productId
     * @param oldAppId original application id
     * @param newAppId new application id
     */
    void changeApplicationInProduct(Long productId, Long oldAppId, Long newAppId);
    /**
     * Remove an application from all products, and delete it.
     * AppBase is also removed if no other application.
     * @param appId
     * @return ApplicationBase id of this application that was deleted or null if base ApplicationBase was not be deleted.
     */
    Long deleteApplication(Long appId);
    
    /**
     * Delete an application base.
     * The application base must be empty which contains no versions or dictionaries
     * @param appBaseId
     * @throws BusinessException
     */
    void deleteApplicationBase(Long appBaseId) throws BusinessException;
    
    /**
     * Delete a product base.
     * The product base must be empty which contains no versions or applications
     * @param prodBaseId
     * @throws BusinessException
     */
    void deleteProductBase(Long prodBaseId) throws BusinessException;
}
