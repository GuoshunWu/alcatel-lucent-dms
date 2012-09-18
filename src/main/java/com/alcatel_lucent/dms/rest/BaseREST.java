package com.alcatel_lucent.dms.rest;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

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
    	if (format == null) {
    		return jsonService.toJSONString(data, prop);
    	} else if (format.trim().equals("grid")) {
    		Integer rows = requestMap.get("rows") == null ? null : Integer.valueOf(requestMap.get("rows"));
    		Integer page = requestMap.get("page") == null ? null : Integer.valueOf(requestMap.get("page"));
    		Integer records = requestMap.get("records") == null ? null : Integer.valueOf(requestMap.get("records"));
			JSONObject json = jsonService.toGridJSON((Collection) data, rows, page, records, "id", prop);
			return json.toString();
    	} else if (format.trim().equals("select")) {
    		// TODO: select json
    		return null;
    	} else {
    		throw new RESTException("Unknown format '" + format + "'");
    	}
    }
    
    abstract String doGetOrPost(Map<String, String> requestMap) throws Exception;

}