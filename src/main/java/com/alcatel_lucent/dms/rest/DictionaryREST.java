package com.alcatel_lucent.dms.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alcatel_lucent.dms.model.Application;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.service.TranslationService;
import com.alcatel_lucent.dms.util.ObjectComparator;

/**
 * Dictionary REST service.
 * URL: /rest/dict
 * Filter parameters:
 *   prod		(optional) product id, in which product all dictionaries will be retrieved
 *   app		(optional) application id, in which application all dictionaries will be retrieved
 *   slibing	(optional) dictionary id, if specified, it will return all dictionaries under same base
 *   filters	(optional) jqGrid-style filter string, in json format, e.g.
 *   	{"groupOp":"AND","rules":[{"field":"base.name","op":"eq","data":"2"},{"field":"format","op":"eq","data":"dct"}]}
 * One of the "prod", "app" or "slibing" filter must be specified.
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
 *   idprop		(optional) property name for id, for grid only
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
    private TranslationService translationService;
    
	@Override
    @SuppressWarnings("rawtypes")
    public Class getEntityClass() {
    	return Dictionary.class;
    }
    
    @Override
    protected String doGetOrPost(Map<String, String> requestMap) throws Exception {
    	Long appId = null;
    	Long prodId = null;
    	Long slibingId = null;
    	if (requestMap.get("app") != null) {
    		appId = Long.valueOf(requestMap.get("app"));
    	} else if (requestMap.get("prod") != null) {
    		prodId = Long.valueOf(requestMap.get("prod"));
    	} else if (requestMap.get("slibing") != null) {
    		slibingId = Long.valueOf(requestMap.get("slibing"));
    	}
    	Map param = new HashMap();
    	Map<String, String> filters = getGridFilters(requestMap);
    	String hqlWhere = "";
    	if (filters != null) {
    		int i = 0;
    		for (String field : filters.keySet()) {
    			hqlWhere += " and " + toFieldName(field) + "=:p" + i;
    			param.put("p" + i, filters.get(field));
    			i++;
    		}
    	}
    	String hql, countHql;
    	if (appId != null) {
    		hql = "select obj,app from Application app join app.dictionaries obj where app.id=:appId";
    		countHql = "select count(*) from Application app join app.dictionaries obj where app.id=:appId";
    		param.put("appId", appId);
    	} else if (prodId != null) {
    		hql = "select obj,app from Product p join p.applications app join app.dictionaries obj where p.id=:prodId";
    		countHql = "select count(*) from Product p join p.applications app join app.dictionaries obj where p.id=:prodId";
    		param.put("prodId", prodId);
    	} else {
    		hql = "select obj from Dictionary obj where obj.base=(select d.base from Dictionary d where d.id=:slibingId)";
    		countHql = "select count(*) from Dictionary where base=(select d.base from Dictionary d where d.id=:slibingId)";
    		param.put("slibingId", slibingId);
    	}
    	hql += hqlWhere;
    	countHql += hqlWhere;

    	String sidx = requestMap.get("sidx");
    	String sord = requestMap.get("sord");
    	if (sidx == null || sidx.trim().isEmpty()) {
    		sidx = "base.name";
    	}
    	if (sord == null) {
    		sord = "ASC";
    	}
    	
    	Comparator<Dictionary> comparator = null;
    	Collection result = null;
    	if (sidx.startsWith("s(")) {	// sort in memory for field T/N/I
    		comparator = new ObjectComparator<Dictionary>(sidx, sord);
    		result = dao.retrieve(hql, param);
    	} else {	// otherwise, sort in HQL
	    	String orderBy = toFieldName(sidx);
	    	if (sidx.equals("labelNum")) {
	    		orderBy = "obj.labels.size";
	    	}
	    	hql += " order by " + orderBy + " " + sord;
	    	result = retrieve(hql, param, countHql, param, requestMap);
    	}
    	
    	ArrayList<Dictionary> dictionaries = new ArrayList<Dictionary>();
    	if (slibingId != null) {
    		dictionaries.addAll(result);
    	} else {	// if filtered by prod and app, add app information
	    	for (Object[] row : (Collection<Object[]>) result) {
	    		Dictionary dict = (Dictionary) row[0];
                // populate validation results
                dict.validate(false);
	    		dictionaries.add(dict);
	    		dict.setApp((Application) row[1]);
	    	}
    	}
    	
		// additional properties
    	String prop = requestMap.get("prop");
		if (prop.indexOf(",s(") != -1) {	// has summary
			Map<Long, Map<Long, int[]>> summary = new HashMap<Long, Map<Long, int[]>>();
			if (appId != null) {
				summary = translationService.getDictTranslationSummaryByApp(appId);
			} else if (prodId != null) {
				summary = translationService.getDictTranslationSummaryByProd(prodId);
			}
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

    	if (comparator != null) {	// if sort in memory, do sort and page filter
    		Collections.sort(dictionaries, comparator);
    		dictionaries = (ArrayList<Dictionary>) pageFilter(dictionaries, requestMap);
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
