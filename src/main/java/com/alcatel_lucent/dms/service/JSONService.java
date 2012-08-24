package com.alcatel_lucent.dms.service;

import java.util.Collection;
import java.util.Map;

import net.sf.json.JSONObject;

public interface JSONService {
	
	String toJSONString(Object entity, Map<String, Collection<String>> propFilter);

}
