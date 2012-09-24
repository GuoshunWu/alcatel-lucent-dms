package com.alcatel_lucent.dms.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alcatel_lucent.dms.model.Application;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.service.DictionaryService;
import com.alcatel_lucent.dms.service.LanguageService;

/**
 * Dictionary REST service.
 * URL: /rest/dict
 * Filter parameters:
 *   prod		(optional) product id, in which product all dictionaries will be retrieved
 *   app		(optional) application id, in which application all dictionaries will be retrieved
 *   filters	(optional) jqGrid-style filter string, in json format, e.g.
 *   	{"groupOp":"AND","rules":[{"field":"base.name","op":"eq","data":"2"},{"field":"format","op":"eq","data":"dct"}]}
 * One and only of the "prod" and "app" must be specified.  
 *   
 * Sort parameters:
 *   sidx		(optional) sort by, default is "base.name"
 *   			Application-related property values can be accessed by adding "app." prefix, e.g. "app.base.name"
 *   sord		(optional) order, default is "ASC"
 *   
 * Format parameters:
 *   format		(optional) format of result string, possible values: "json|grid|tree", default is "json"
 *   prop		(required) properties to be retrieved
 *   			for json: prop={<prop1>,<prop2>,...} where each <prop> can be nested, 
 *   					e.g. <prop2>=<prop_name>{<sub_prop1>,<sub_prop2>}
 *   			for grid: prop=<property_name_for_column1>,<property_name_for_column2>,...
 *   			for tree: prop=<property_name_for_id>,<property_name_for_name>
 *   			Application-related property values can be accessed by adding "app." prefix to the prop name. 
 *   rows		(optional) number of records to be retrieved, only be used when format is grid
 *   page		(optional) current page, only be used when format is grid
 *   		
 * @author allany
 *
 */
@Path("dict")
@Component("dictionaryREST")
public class DictionaryREST extends BaseREST {

    @Autowired
    private DictionaryService dictionaryService;
    
    @Override
    protected String doGetOrPost(Map<String, String> requestMap) throws Exception {
    	Long appId = null;
    	Long prodId = null;
    	if (requestMap.get("app") != null) {
    		appId = Long.valueOf(requestMap.get("app"));
    	} else {
    		prodId = Long.valueOf(requestMap.get("prod"));
    	}
    	Map param = new HashMap();
    	Map<String, String> filters = getGridFilters(requestMap);
    	String hqlWhere = "";
    	if (filters != null) {
    		int i = 0;
    		for (String field : filters.keySet()) {
    			hqlWhere += " and " + toFieldName(field) + "=:p" + i;
    			param.put("p" + i, filters.get(field));
    		}
    	}
    	String hql, countHql;
    	if (appId != null) {
    		hql = "select obj,app from Application app join app.dictionaries obj where app.id=:appId";
    		countHql = "select a.dictionaries.size from Application a where a.id=:appId";
    		param.put("appId", appId);
    	} else {
    		hql = "select obj,app from Product p join p.applications app join app.dictionaries obj where p.id=:prodId";
    		countHql = "select a.dictionaries.size from Product p join p.applications a where p.id=:prodId";
    		param.put("prodId", prodId);
    	}
    	hql += hqlWhere;

    	String sidx = requestMap.get("sidx");
    	String sord = requestMap.get("sord");
    	if (sidx == null || sidx.trim().isEmpty()) {
    		sidx = "base.name";
    	}
    	if (sord == null) {
    		sord = "ASC";
    	}
    	String orderBy = toFieldName(sidx);
    	if (sidx.equals("labelNum")) {
    		orderBy = "obj.labels.size";
    	}
    	hql += " order by " + orderBy + " " + sord;
    	Collection<Object[]> result = retrieve(hql, param, countHql, param, requestMap);
    	
    	Collection<Dictionary> dictionaries = new ArrayList<Dictionary>();
    	for (Object[] row : result) {
    		Dictionary dict = (Dictionary) row[0];
    		dictionaries.add(dict);
    		dict.setApp((Application) row[1]);
    	}
    	
		// additional properties
    	String prop = requestMap.get("prop");
		if (prop.indexOf(",s(") != -1) {	// has summary
			Map<Long, Map<Long, int[]>> summary = dictionaryService.getDictTranslationSummary(prodId);
			Collection<Long> allLanguageId = dao.retrieve("select id from Language");
			for(Dictionary dict : dictionaries) {
				Map<Long, int[]> dictSummary = summary.get(dict.getId());
				if (dictSummary == null) {
					dictSummary = new HashMap<Long, int[]>();
				}
				fillZero(allLanguageId, dictSummary);
				dict.setS(dictSummary);
			}
		}
		
    	return toJSON(dictionaries, requestMap);
    }

	private void fillZero(Collection<Long> langIds, Map<Long, int[]> map) {
		for (Long langId : langIds) {
			if (!map.containsKey(langId)) {
				map.put(langId, new int[] {0, 0, 0});
			}
		}
	}
	
	private String toFieldName(String name) {
		return name.startsWith("app.") ? name : "obj." + name; 
	}
}
