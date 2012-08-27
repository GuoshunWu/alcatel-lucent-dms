package com.alcatel_lucent.dms.service;

import java.util.Collection;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.processors.PropertyNameProcessor;
import net.sf.json.util.PropertyFilter;

import org.springframework.stereotype.Service;

@Service("jsonService")
public class JSONServiceImpl implements JSONService {
	
	public String toJSONString(Object entity, final Map<String, Collection<String>> propFilter, Map<Class,Map<String,String>> propRename) {
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

            //register rename property
            for(final Map.Entry<Class,Map<String,String>> entry: propRename.entrySet()){
                config.registerJsonPropertyNameProcessor(entry.getKey(),new PropertyNameProcessor() {
                    @Override
                    public String processPropertyName(Class beanClass, String name) {
                        if(null!=(entry.getValue().get(name))){
                            return entry.getValue().get(name);
                        }
                        return name;
                    }
                });
            }


        if (entity instanceof Collection) {
			return JSONArray.fromObject(entity, config).toString();
		} else {
			return JSONObject.fromObject(entity, config).toString();
		}

	}
}
