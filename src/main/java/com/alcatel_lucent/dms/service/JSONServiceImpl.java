package com.alcatel_lucent.dms.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.util.PropertyFilter;

import org.springframework.stereotype.Service;

@Service("jsonService")
public class JSONServiceImpl implements JSONService {
	
	public String toJSONString(Object entity, final Map<String, Collection<String>> propFilter) {
		JsonConfig config = new JsonConfig();
		
        	config.setJsonPropertyFilter(new PropertyFilter() {
	            @Override
	            public boolean apply(Object source, String name, Object value) {
	            	String className = source.getClass().getName();
	            	int pos = className.lastIndexOf(".");
	            	if (pos != -1) {
	            		className = className.substring(pos + 1);
	            	}
	            	Collection<String> props = propFilter.get(className);
	            	if (props != null) {
	            		return !props.contains(name);
	            	}
	                return true;
	            }
	        });
		
		if (entity instanceof Collection) {
			return JSONArray.fromObject(entity, config).toString();
		} else {
			return JSONObject.fromObject(entity, config).toString();
		}
	}
}
