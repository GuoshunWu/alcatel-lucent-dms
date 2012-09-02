package com.alcatel_lucent.dms.service;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.Collection;
import java.util.Map;

public interface JSONService {

    static final int JSON_TREE = 1;
    static final int JSON_GRID = 2;
    static final int JSON_SELECT = 3;

    /**
     * Convert the entity to json string.
     *
     * @param entity      Will be transformed entity
     * @param propFilter  Filed filter map
     * @param vpropRename Optional field rename map
     * @return json string represent the entity
     */
    String toJSONString(Object entity, Map<String, Collection<String>> propFilter, Map<Class, Map<String, String>>... vpropRename);


    /**
     * Convert the entity to JSTree json data.
     *
     * @param entity      Will be transformed entity
     * @param propFilter  Filed filter map
     * @param vpropRename Optional field rename map
     * @return JSONArray represent the entity
     */
    JSONArray toSelectJSON(Object entity, Map<String, Collection<String>> propFilter, Map<Class, Map<String, String>>... vpropRename);

    /**
     * Convert the entity to JSTree json data.
     *
     * @param entity      Will be transformed entity
     * @param propFilter  Filed filter map
     * @param vpropRename Optional field rename map
     * @return JSONArray represent the entity
     */

    JSONArray toTreeJSON(Object entity, Map<String, Collection<String>> propFilter, Map<Class, Map<String, String>>... vpropRename);

    /**
     * Convert the entity to JGrid json data.
     *
     * @param entity      Will be transformed entity
     * @param records     Total records in the entity
     * @param rows        How many rows we want to have into the grid
     * @param page        The requested page
     * @param propFilter  Filed filter map
     * @param vpropRename Optional field rename map
     * @return JSONObject represent the entity
     */

    JSONObject toGridJSON(Object entity, int rows, int page, int records, Map<String, Collection<String>> propFilter, Map<Class, Map<String, String>>... vpropRename);

}
