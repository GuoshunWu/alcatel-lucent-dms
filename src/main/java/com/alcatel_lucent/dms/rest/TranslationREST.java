package com.alcatel_lucent.dms.rest;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Path;

import org.springframework.stereotype.Component;

import com.alcatel_lucent.dms.model.Text;
import com.alcatel_lucent.dms.model.Translation;

/**
 * Translation REST service.
 * URL: /rest/text/translations
 * Filter parameters:
 *   text		(required) text id
 *   status		(optional) status
 *   
 * Sort parameters:
 *   sidx		(optional) sort by, default is "reference"
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
@Path("text/translations")
@Component("translationREST")
public class TranslationREST extends BaseREST {
	
	@Override
	String doGetOrPost(Map<String, String> requestMap) throws Exception {
		Long textId = Long.valueOf(requestMap.get("text"));
		String statusStr = requestMap.get("status");
		Integer status = null;
		if (statusStr != null && !statusStr.trim().isEmpty()) {
			status = Integer.valueOf(statusStr);
		}
		
		String hql = "from Translation where text.id=:textId";
		String countHql = "select count(*) from Translation where text.id=:textId";
		Map param = new HashMap();
		param.put("textId", textId);
		if (status != null) {
			hql += " and status=:status";
			countHql += " and status=:status";
			param.put("status", status);
		}
    	String sidx = requestMap.get("sidx");
    	String sord = requestMap.get("sord");
    	if (sidx == null || sidx.trim().isEmpty()) {
    		sidx = "language.name";
    	}
    	if (sord == null) {
    		sord = "ASC";
    	}
    	hql += " order by " + sidx + " " + sord;
		Collection<Text> texts = retrieve(hql, param, countHql, param, requestMap);
		return toJSON(texts, requestMap);
	}

	@Override
	Class getEntityClass() {
		return Translation.class;
	}

}
