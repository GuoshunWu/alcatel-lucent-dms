package com.alcatel_lucent.dms.service;

import java.util.Collection;
import java.util.Map;

public interface JSONService {
	
	String toJSONString(Object entity, Map<String, Collection<String>> propFilter, Map<Class,Map<String,String>> propRename);

}
