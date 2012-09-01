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
     * convert a entity to json string
     *
     * @param propFilter
     * @param vpropRename optional
     */
    String toJSONString(Object entity, Map<String, Collection<String>> propFilter, Map<Class, Map<String, String>>... vpropRename);
    JSONArray toSelectJSON(Object entity, Map<String, Collection<String>> propFilter, Map<Class, Map<String, String>>... vpropRename);
    JSONArray toTreeJSON(Object entity, Map<String, Collection<String>> propFilter,Map<Class, Map<String, String>>... vpropRename);
    JSONObject toGridJSON(Object entity,Map<String,Object> pageData, Map<String, Collection<String>> propFilter,Map<Class, Map<String, String>>... vpropRename);

}
