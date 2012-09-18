package com.alcatel_lucent.dms.rest;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.service.DictionaryService;

/**
 * Dictionary REST service.
 * URL: /rest/dict
 * Filter parameters:
 *   prod		(required) product id, in which product all dictionaries will be retrieved
 *   
 * Sort parameters:
 *   sidx		(optional) sort by, default is "base.name"
 *   sord		(optional) order, default is "ASC"
 *   
 * Format parameters:
 *   format		(required) format of result string, possible values: "grid|select|tree|json"
 *   prop		(required) properties to be retrieved
 *   			for grid: prop=<property_name_for_column1>,<property_name_for_column2>,...
 *   			for select: prop=<property_name_for_value>,<property_name_for_name>
 *   			for tree: prop=<property_name_for_id>,<property_name_for_name>
 *   			for json: prop={<prop1>,<prop2>,...} where each <prop> can be nested, 
 *   					e.g. <prop2>=<prop_name>{<sub_prop1>,<sub_prop2>}
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
    	Long prodId = Long.valueOf(requestMap.get("prod"));
    	String sidx = requestMap.get("sidx");
    	String sord = requestMap.get("sord");
    	Map param = new HashMap();
    	param.put("prodId", prodId);
    	String hql = "select d from Product p join p.applications app join app.dictionaries d where p.id=:prodId";
    	if (sidx == null || sidx.trim().isEmpty()) {
    		sidx = "base.name";
    	}
    	if (sord == null) {
    		sord = "ASC";
    	}
    	hql += " order by d." + sidx + " " + sord;
    	
    	String rows = requestMap.get("rows");
    	String page = requestMap.get("page");
    	Collection<Dictionary> dictionaries;
    	if (rows == null) {	// not paged
    		dictionaries = dao.retrieve(hql, param);
    	} else {	// paged
    		int first = (page == null ? 0 : (Integer.parseInt(page) - 1) * Integer.parseInt(rows));
    		dictionaries = dao.retrieve(hql, param, first, Integer.parseInt(rows));
    		// count total records
    		hql = "select a.dictionaries.size from Product p join p.applications a where p.id=:prodId";
			Integer records = (Integer) dao.retrieveOne(hql, param);
			requestMap.put("records", "" + records);
    	}
    	
		// additional properties
    	String prop = requestMap.get("prop");
		if (prop.indexOf(",s(") != -1) {	// has summary
			Map<Long, Map<Long, int[]>> summary = dictionaryService.getDictTranslationSummary(prodId);
			for(Dictionary dict : dictionaries) {
				dict.setS(summary.get(dict.getId()));
			}
		}
		
    	return toJSON(dictionaries, requestMap);
    }
}
