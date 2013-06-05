package com.alcatel_lucent.dms.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.DictionaryLanguage;
import com.alcatel_lucent.dms.model.Label;
import com.alcatel_lucent.dms.service.DeliveringDictPool;
import com.alcatel_lucent.dms.util.ObjectComparator;

@Path("/delivery/labels")
@Component("PreviewDictLabelREST")
public class PreviewDictLabelREST extends BaseREST {

	@Autowired
	private DeliveringDictPool pool;
	
	@Override
	Class getEntityClass() {
		return DictionaryLanguage.class;
	}
	
	@Override
	String doGetOrPost(Map<String, String> requestMap) throws Exception {
		String handler = requestMap.get("handler");
		Long dictId = Long.valueOf(requestMap.get("dict"));
		Dictionary dict = pool.getDictionary(handler, dictId);
		if (dict.getLabels() == null) {
			return "";
		}
    	String sidx = requestMap.get("sidx");
    	String sord = requestMap.get("sord");
    	if (sidx == null || sidx.trim().isEmpty()) {
    		sidx = "sortNo";
    	}
    	if (sord == null) {
    		sord = "ASC";
    	}
		Collection<Label> labels = new ArrayList<Label>(dict.getLabels());
		Collections.sort((ArrayList<Label>) labels, new ObjectComparator(sidx, sord));
		labels = pageFilter(labels, requestMap);
		return toJSON(labels, requestMap);
	}

}
