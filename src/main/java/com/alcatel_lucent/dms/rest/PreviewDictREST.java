package com.alcatel_lucent.dms.rest;

import java.util.Map;

import javax.ws.rs.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.service.DeliveringDictPool;

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
		pool.addHandler("NOE_6.7.2");
		String handler = requestMap.get("handler");
		return toJSON(pool.getDictionaries(handler), requestMap);
	}

}
