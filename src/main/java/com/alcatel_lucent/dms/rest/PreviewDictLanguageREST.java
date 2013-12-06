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
import com.alcatel_lucent.dms.service.DeliveringDictPool;
import com.alcatel_lucent.dms.util.ObjectComparator;

@Path("/delivery/dictLanguages")
@Component("PreviewDictLanguageREST")
public class PreviewDictLanguageREST extends BaseREST {

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
		Collection<DictionaryLanguage> dlList = dict.getDictLanguages();
		if (dlList != null) {
			requestMap.put("records", "" + dlList.size());
			dlList = new ArrayList<DictionaryLanguage>(dlList);
	    	String sidx = requestMap.get("sidx");
	    	String sord = requestMap.get("sord");
	    	if (sidx == null || sidx.trim().isEmpty()) {
	    		sidx = "languageCode";
	    	}
	    	if (sord == null) {
	    		sord = "ASC";
	    	}
	    	Collections.sort((ArrayList<DictionaryLanguage>) dlList, new ObjectComparator(sidx, sord));
		}
		return toJSON(dlList, requestMap);
	}

}
