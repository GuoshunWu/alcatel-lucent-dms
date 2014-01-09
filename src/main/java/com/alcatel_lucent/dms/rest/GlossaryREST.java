package com.alcatel_lucent.dms.rest;

import com.alcatel_lucent.dms.model.Charset;
import com.alcatel_lucent.dms.model.Glossary;
import org.springframework.stereotype.Component;

import javax.ws.rs.Path;
import java.util.Collection;
import java.util.Map;

/**
 * Glossary REST service.
 * URL: /rest/glossaries
 *   
 * Sort parameters:
 *   sidx		(optional) sort by, default is "text"
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
@Path("glossaries")
@Component
@SuppressWarnings("unchecked")
public class GlossaryREST extends BaseREST {
	
	public Class<Glossary> getEntityClass() {
		return Glossary.class;
	}

	@Override
	protected String doGetOrPost(Map<String, String> requestMap) throws Exception {
		String hql = "from Glossary";
		String countHql = "select count(*) from Glossary";
    	String sidx = requestMap.get("sidx");
    	String sord = requestMap.get("sord");
    	if (sidx == null || sidx.trim().isEmpty()) {
    		sidx = "text";
    	}
    	if (sord == null) {
    		sord = "ASC";
    	}
    	hql += " order by " + sidx + " " + sord;
		Collection<Charset> result = retrieve(hql, null, countHql, null, requestMap);
		return toJSON(result, requestMap);
	}
}
