package com.alcatel_lucent.dms.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.alcatel_lucent.dms.service.DaoService;
import com.alcatel_lucent.dms.service.JSONService;

@Produces({MediaType.APPLICATION_JSON + ";CHARSET=UTF-8", MediaType.TEXT_HTML + ";CHARSET=UTF-8"})
public abstract class BaseREST {
	
	protected Logger log = Logger.getLogger(this.getClass());
    
	@Autowired
    protected DaoService dao;

    @Autowired
    protected JSONService jsonService;
    
    @GET
    public String doGet(@Context UriInfo ui) {
    	Map<String, String> map = new HashMap<String, String>();
    	for (String key : ui.getQueryParameters().keySet()) {
    		map.put(key, ui.getQueryParameters().getFirst(key));
    	}
    	try {
    		return doGetOrPost(map);
    	} catch (Exception e) {
    		e.printStackTrace();
    		log.error(e);
    		throw new RESTException(e);
    	}
    }
    
    @POST
    @Consumes("application/x-www-form-urlencoded")
    public String retrieveLabels(MultivaluedMap<String, String> formParams, @Context UriInfo ui) {
    	Map<String, String> map = new HashMap<String, String>();
    	for (String key : formParams.keySet()) {
    		map.put(key, formParams.getFirst(key));
    	}
    	for (String key : ui.getQueryParameters().keySet()) {
    		map.put(key, ui.getQueryParameters().getFirst(key));
    	}
    	try {
    		return doGetOrPost(map);
    	} catch (Exception e) {
    		e.printStackTrace();
    		log.error(e);
    		throw new RESTException(e);
    	}
    }
    
    protected String toJSON(Object data, Map<String, String> requestMap) throws Exception {
    	String format = requestMap.get("format");
    	String prop = requestMap.get("prop");
    	String idprop = requestMap.get("idprop");
    	if (format == null) {
    		return jsonService.toJSONString(data, prop);
    	} else if (format.trim().equals("grid")) {
    		Integer rows = requestMap.get("rows") == null ? null : Integer.valueOf(requestMap.get("rows"));
    		Integer page = requestMap.get("page") == null ? null : Integer.valueOf(requestMap.get("page"));
    		Integer records = requestMap.get("records") == null ? null : Integer.valueOf(requestMap.get("records"));
			JSONObject json = jsonService.toGridJSON((Collection<?>) data, rows, page, records, idprop == null ? "id" : idprop, prop);
			return json.toString();
    	} else if (format.trim().equals("tree")) {
    		// TODO: select tree
    		return null;
    	} else {
    		throw new RESTException("Unknown format '" + format + "'");
    	}
    }
    
    protected Collection retrieve(String hql, Map<?,?> param, String countHql, Map<?,?> countParam, Map<String, String> requestMap) {
    	String rows = requestMap.get("rows");
    	String page = requestMap.get("page");
    	Collection<?> result;
    	if (rows == null) {	// not paged
    		result = dao.retrieve(hql, param);
    	} else {	// paged
    		int first = (page == null ? 0 : (Integer.parseInt(page) - 1) * Integer.parseInt(rows));
    		result = dao.retrieve(hql, param, first, Integer.parseInt(rows));
    		
    		// count total records
    		if (countHql != null) {
				Number records = (Number) dao.retrieveOne(countHql, countParam);
				requestMap.put("records", "" + records);
    		}
    	}
    	return result;
    }
    
    protected Collection<Long> toIdList(String idStr) {
    	String[] ids = idStr.split(",");
    	Collection<Long> result = new ArrayList<Long>();
    	for (String id : ids) {
    		result.add(Long.valueOf(id));
    	}
    	return result;
    }
    
    protected Map<String, String> getGridFilters(Map<String, String> requestMap) {
    	String filterStr = requestMap.get("filters");
    	if (filterStr == null || filterStr.trim().isEmpty()) return null;
    	Map<String, String> result = new HashMap<String, String>();
    	JSONObject json = JSONObject.fromObject(filterStr);
    	JSONArray jsonRules = json.getJSONArray("rules");
    	Iterator<JSONObject> iter= jsonRules.iterator();
    	while (iter.hasNext()) {
    		JSONObject rule = iter.next();
    		result.put(rule.getString("field"), rule.getString("data"));
    	}
    	return result;
    }

    
    abstract String doGetOrPost(Map<String, String> requestMap) throws Exception;

}
