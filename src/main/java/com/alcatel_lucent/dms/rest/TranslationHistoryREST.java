package com.alcatel_lucent.dms.rest;

import com.alcatel_lucent.dms.model.TranslationHistory;
import org.springframework.stereotype.Component;

import javax.ws.rs.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
@Path("translationHistory")
@Component("translationHistoryREST")
public class TranslationHistoryREST extends BaseREST {

	@Override
	String doGetOrPost(Map<String, String> requestMap) throws Exception {
		Long transId = Long.valueOf(requestMap.get("transId"));
		String hql = "select th from TranslationHistory th where th.parent.id =:transId";
		String countHql = "select count(*) from TranslationHistory th where th.parent.id=:transId";
		Map param = new HashMap();
		param.put("transId", transId);
		
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
		Collection<TranslationHistory> data = retrieve(hql, param, countHql, param, requestMap);
		return toJSON(data, requestMap);
	}

	@Override
	Class getEntityClass() {
		return TranslationHistory.class;
	}

}