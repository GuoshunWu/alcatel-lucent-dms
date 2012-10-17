package com.alcatel_lucent.dms.rest;

import java.util.Map;

import javax.ws.rs.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.DictionaryLanguage;
import com.alcatel_lucent.dms.service.DeliveringDictPool;

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
		return toJSON(dict.getDictLanguages(), requestMap);
	}

}
