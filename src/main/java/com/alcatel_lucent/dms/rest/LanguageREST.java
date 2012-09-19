package com.alcatel_lucent.dms.rest;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.Path;
import org.springframework.stereotype.Component;
import com.alcatel_lucent.dms.model.Language;

//import com.alcatel_lucent.dms.service.ProductService;

/**
 * Language REST service.
 * URL: /rest/languages
 * Filter parameters:
 *   prod		(optional) list of product id, all languages used in the product(s) will be retrieved
 *   app		(optional) list of application id, all languages used in the application(s) will be retrieved
 *   dict		(optional) list of dictionary id, all languages used in the dictionary(s) will be retrieved
 *   Only one of the above filter parameter can be provided, 
 *   if no filter parameter is provided, all languages will be retrieved
 *   
 * Sort parameters:
 *   sidx		(optional) sort by, default is "name"
 *   sord		(optional) order, default is "ASC"
 *   
 * Format parameters:
 *   format		(optional) format of result string, possible values: "json|grid|tree", default is "json"
 *   prop		(required) properties to be retrieved
 *   			for json: prop={<prop1>,<prop2>,...} where each <prop> can be nested, 
 *   					e.g. <prop2>=<prop_name>{<sub_prop1>,<sub_prop2>}
 *   			for grid: prop=<property_name_for_column1>,<property_name_for_column2>,...
 *   			for tree: prop=<property_name_for_id>,<property_name_for_name>
 *   The result is not paged, that means "rows" and "page" parameter will not be supported.
 *   		
 * @author allany
 *
 */
@Path("languages")
@Component("languageREST")
@SuppressWarnings("unchecked")
public class LanguageREST extends BaseREST {

    @Override
	protected String doGetOrPost(Map<String, String> requestMap) throws Exception {
    	String hql;
    	Map param = new HashMap();
    	String dictStr = requestMap.get("dict");
    	if (dictStr != null && !dictStr.trim().isEmpty()) {	// query by dictionaries
    		hql = "select distinct obj" +
    				" from DictionaryLanguage dl join dl.language obj" +
    				" where dl.dictionary.id in (:dictId)";
    		param.put("dictId", toIdList(dictStr));
    	} else {	
	    	String appStr = requestMap.get("app");
	    	if (appStr != null && !appStr.trim().isEmpty()) {	// query by applications
	    		hql = "select distinct obj" +
	    				" from Application a join a.dictionaries d join d.dictLanguages dl join dl.language obj" +
	    				" where a.id in (:appId)";
	    		param.put("appId", toIdList(appStr));
	    	} else {
	    		String prodStr = requestMap.get("prod");
	    		if (prodStr != null && !prodStr.trim().isEmpty()) {	// query by products
		    		hql = "select distinct obj" +
		    				" from Product p join p.applications a join a.dictionaries d join d.dictLanguages dl join dl.language obj" +
		    				" where p.id in (:prodId)";
		    		param.put("prodId", toIdList(prodStr));
	    		} else {	// query all
	    			hql = "from Language obj where name not like '%-%'";
	    		}
	    	}
    	}
    	String sidx = requestMap.get("sidx");
    	String sord = requestMap.get("sord");
    	if (sidx == null || sidx.trim().isEmpty()) {
    		sidx = "name";
    	}
    	if (sord == null) {
    		sord = "ASC";
    	}
    	hql += " order by obj." + sidx + " " + sord;
        Collection<Language> result = retrieve(hql, param, null, null, requestMap);
        return toJSON(result, requestMap);
    }
    
}

