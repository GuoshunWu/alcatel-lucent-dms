package com.alcatel_lucent.dms.service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;

/**
 * Author: Allan YANG
 * Date: 2008-11-13
 */


public interface DaoService {
    Session getSession();

    Object retrieve(Class clazz, Serializable id);
    Object retrieve(Class clazz, Serializable id, String[] initProps);

    List retrieve(String hql);
    List retrieve(String hql, String[] initProps);

    List retrieve(String hql, Map paramMap);
    List retrieve(String hql, Map paramMap, String[] initProps);

    /**
     * Query entities.
     * @param hql hql
     * @param paramMap parameters of hql
     * @param firstResult index of record to be retrieved from, 0 identifies the beginning
     * @param maxResults max number of records to be retrieved, -1 identifies no limit
     * @return query results
     */
    List retrieve(String hql, Map paramMap, int firstResult, int maxResults);
    List retrieve(String hql, Map paramMap, int firstResult, int maxResults, String[] initProps);

    Object retrieveOne(String hql);
    Object retrieveOne(String hql, String[] initProps);

    Object retrieveOne(String hql, Map paramMap);
    Object retrieveOne(String hql, Map paramMap, String[] initProps);

    Object create(Object entity);

    /**
     * Create an entity.
     * @param entity transient object to be created
     * @param flush whether or not flush session after creation
     * @return entity created
     */
    Object create(Object entity, boolean flush);

    /**
     * Batch create entities (much faster than one by one).
     * @param entities entities to be created
     * @return entites created
     */
    Object[] createArray(Object[] entities);

    Object update(Object obj);

    /**
     * Update an entity
     * @param entity entity to be updated
     * @param propertyNames properties of the entity to be merged, null if all
     * @return entity updated
     */
    Object update(Object entity, String[] propertyNames);

    /**
     * Create or update an entity.
     * The operation is decided by whether identifier is null.
     * @param entity entity to be created or updated
     * @return entity created or updated
     */
    Object createOrUpdate(Object entity);

    void delete(Object obj);

    /**
     * Delete an entity.
     * @param entity entity to be deleted
     * @param flush whether or not flush session after delete
     */
    void delete(Object entity, boolean flush);

    /**
     * Removes the object from the database with with specified class
     * type and <code>id</code>.
     *
     * @param className the class type to remove
     * @param id        the id of the class type
     */
    void delete(String className, Serializable id);

    /**
     * Batch delete (in testing).
     * @param deleteStr hql of entities to be deleted
     * @param paramMap hql parameters
     * @return number of the entities deleted
     */
    int delete(String deleteStr, Map paramMap);
}
