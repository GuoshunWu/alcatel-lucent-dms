package com.alcatel_lucent.dms.rest;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Path;

import org.springframework.stereotype.Component;

import com.alcatel_lucent.dms.model.DictionaryHistory;

/**
 * DictionaryHistory REST service.
 * URL: /rest/dictHistory
 * Filter parameters:
 *   dict		(required) dictionary id
 *   
 * Sort parameters:
 *   sidx		(optional) sort by, default is "operationTime" (desc)
 *   sord		(optional) order, default is "ASC"
 *   
 * Format parameters:
 *   format		(optional) format of result string, possible values: "json|grid|tree", default is "json"
 *   prop		(required) properties to be retrieved
 *   			for json: prop={<prop1>,<prop2>,...} where each <prop> can be nested, 
 *   					e.g. <prop2>=<prop_name>{<sub_prop1>,<sub_prop2>}
 *   			for grid: prop=<property_name_for_column1>,<property_name_for_column2>,...
 *   			for tree: prop=<property_name_for_id>,<property_name_for_name>
 *   idprop		(optional) property name for id, for grid only
 *   rows		(optional) number of records to be retrieved, only be used when format is grid
 *   page		(optional) current page, only be used when format is grid
 *   		
 * @author allany
 *
 */
@Path("dictHistory")
@Component("dictionaryHistoryREST")
public class DictionaryHistoryREST extends BaseREST {

	@Override
	String doGetOrPost(Map<String, String> requestMap) throws Exception {
		Long dictId = Long.valueOf(requestMap.get("dict"));
		String hql = "select obj from DictionaryHistory obj where obj.dictionary.id=:dictId";
		String countHql = "select count(*) from DictionaryHistory obj where obj.dictionary.id=:dictId";
		Map param = new HashMap();
		param.put("dictId", dictId);
		
    	String sidx = requestMap.get("sidx");
    	String sord = requestMap.get("sord");
    	if (sidx == null || sidx.trim().isEmpty()) {
    		sidx = "obj.operationTime";
    		sord = "DESC";
    	} else {
    		sidx = "obj." + sidx;
    	}
    	if (sord == null) {
    		sord = "ASC";
    	}
		Collection<DictionaryHistory> data = retrieve(hql, param, countHql, param, requestMap);
		return toJSON(data, requestMap);
	}

	@Override
	Class getEntityClass() {
		return DictionaryHistory.class;
	}

}
