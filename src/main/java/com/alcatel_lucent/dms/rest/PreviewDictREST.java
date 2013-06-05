package com.alcatel_lucent.dms.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.service.DeliveringDictPool;
import com.alcatel_lucent.dms.util.ObjectComparator;

@Path("/delivery/dict")
@Component("PreviewDictREST")
public class PreviewDictREST extends BaseREST {

	@Autowired
	private DeliveringDictPool pool;
	
	@Override
	Class getEntityClass() {
		return Dictionary.class;
	}
	
	@Override
	String doGetOrPost(Map<String, String> requestMap) throws Exception {
		String handler = requestMap.get("handler");
		Collection<Dictionary> dictList = new ArrayList<Dictionary>(pool.getDictionaries(handler));
    	String sidx = requestMap.get("sidx");
    	String sord = requestMap.get("sord");
    	if (sidx == null || sidx.trim().isEmpty()) {
    		sidx = "name";
    	}
    	if (sord == null) {
    		sord = "ASC";
    	}
		Collections.sort((ArrayList<Dictionary>) dictList, new ObjectComparator(sidx, sord));
		requestMap.put("records", "" + dictList.size());
		return toJSON(dictList, requestMap);
	}

    @GET
    @Path("/{id}")
    public String getEntityById(@Context UriInfo ui, @PathParam("id") Long id) {
    	String prop = ui.getQueryParameters().getFirst("prop");
    	String handler = ui.getQueryParameters().getFirst("handler");
    	Dictionary dict = pool.getDictionary(handler, id);
    	try {
			return jsonService.toJSONString(dict, prop);
		} catch (Exception e) {
			e.printStackTrace();
    		log.error(e.toString());
    		throw new RESTException(e);
		}
    }
    	

}
