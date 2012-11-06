package com.alcatel_lucent.dms.rest;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Path;

import org.springframework.stereotype.Component;

import com.alcatel_lucent.dms.model.DictionaryLanguage;

/**
 * DictionaryLanguage REST service.
 * URL: /rest/dictLanguages
 * Filter parameters:
 *   dict		(required) dictionary id
 *   
 * Sort parameters:
 *   sidx		(optional) sort by, default is "sortNo"
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
 *   The result is not paged, that means "rows" and "page" parameter will not be supported.
 *   		
 * @author allany
 *
 */
@Path("dictLanguages")
@Component("dictionaryLanguageREST")
public class DictionaryLanguageREST extends BaseREST {
	
	@Override
    @SuppressWarnings("rawtypes")
    public Class getEntityClass() {
		return DictionaryLanguage.class;
	}
	
    @Override
    protected String doGetOrPost(Map<String, String> requestMap) throws Exception {
    	Long dictId = Long.valueOf(requestMap.get("dict"));
    	String sidx = requestMap.get("sidx");
    	String sord = requestMap.get("sord");
    	if (sidx == null || sidx.trim().isEmpty()) {
    		sidx = "sortNo";
    	}
    	if (sord == null) {
    		sord = "ASC";
    	}
    	Map param = new HashMap();
    	param.put("dictId", dictId);
    	String hql = "from DictionaryLanguage obj where obj.dictionary.id=:dictId";
    	String countHql = "select d.dictLanguages.size from Dictionary d where d.id=:dictId";
		hql += " order by " + sidx + " " + sord;
    	Collection<DictionaryLanguage> result = retrieve(hql, param, countHql, param, requestMap);
    	return toJSON(result, requestMap);
    }

}
