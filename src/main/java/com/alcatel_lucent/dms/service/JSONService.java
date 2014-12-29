package com.alcatel_lucent.dms.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Map;

public interface JSONService {

    static final int JSON_TREE = 1;
    static final int JSON_GRID = 2;
    static final int JSON_SELECT = 3;

    /**
     * Convert the entity to json string.
     *
     * @param entity  Will be transformed entity
     * @param propExp A presentation of properties, e.g.: {id,code,name,parent(id,name),children{name}}
     * @return json string represent the entity
     */
    String toJSONString(Object entity, String propExp) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException;


    /**
     * Convert the entity to selection json data.
     *
     * @param entity      Will be transformed entity
     * @param propFilter  Filed filter map
     * @param vpropRename Optional field rename map
     * @return JSONArray represent the entity
     */
    JSONArray toSelectJSON(Object entity, Map<String, Collection<String>> propFilter, Map<Class, Map<String, String>>... vpropRename);

    /**
     * Convert the entity to JSTree json data
     *
     * @param root
     * @param idProp
     * @param typeProp
     * @param dataProp
     * @param childrenProp
     * @return
     */
    public JSONObject toTreeJSON(Object root, String[] idProp, String[] typeProp, String[] dataProp, String[] childrenProp);

    /**
     * Convert the entity to JSTree json data.
     *
     * @param entity      Will be transformed entity
     * @param propFilter  Filed filter map
     * @param vpropRename Optional field rename map
     * @return JSONArray represent the entity
     */

    JSONArray toTreeJSON2(Object entity, Map<String, Collection<String>> propFilter, Map<Class, Map<String, String>>... vpropRename);


    /**
     * Convert entities to jqGrid JSON format.
     *
     * @param entities  collection of entity beans
     * @param rows      number of rows in each page
     * @param page      current page
     * @param records   total number of records
     * @param idProp    property name for "id" field
     * @param cellProps property names for "cell" field, split by comma. Sample: id,code,name,parent.name,children[0].code
     * @return JSON object
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */
    JSONObject toGridJSON(Collection<?> entities, Integer rows, Integer page, Integer records, String idProp, String cellProps) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException;


    /**
     * Convert entities to jqGrid JSON format.
     *
     * @param entities  collection of entity beans
     * @param rows      number of rows in each page
     * @param page      current page
     * @param records   total number of records
     * @param idProp    property name for "id" field
     * @param cellProps property names for "cell" field, split by comma. Sample: id,code,name,parent.name,children[0].code
     * @return JSON string
     *
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws com.fasterxml.jackson.core.JsonProcessingException
     */
    String toGridJSONIncludeJSONString(Collection<?> entities, Integer rows, Integer page, Integer records, String idProp, String cellProps) throws JsonProcessingException, IllegalAccessException, NoSuchMethodException, InvocationTargetException;


}
