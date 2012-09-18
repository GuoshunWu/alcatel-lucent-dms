package com.alcatel_lucent.dms.service;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import com.alcatel_lucent.dms.util.Util;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.processors.PropertyNameProcessor;
import net.sf.json.util.PropertyFilter;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import static java.lang.Math.ceil;

@Service("jsonService")
@SuppressWarnings("unchecked")
public class JSONServiceImpl implements JSONService {

    private static Logger log = Logger.getLogger(JSONServiceImpl.class);

    public String toJSONString(Object entity, String propExp) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
    	return Util.jsonFormat(toJSON(entity, propExp).toString());
    }
    
    public JSON toJSON(Object entity, String propExp) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
    	if (entity instanceof Collection) {
    		JSONArray jsonArray = new JSONArray();
    		for (Object obj : (Collection<Object>) entity) {
    			jsonArray.add(toJSON(obj, propExp));
    		}
    		return jsonArray;
    	}
    	JSONObject json = new JSONObject();
    	String[] props = extractFirstLevelProperties(propExp);
    	for (String prop : props) {
    		try {
	    		int pos = prop.indexOf("{");
	    		if (pos == -1) {
	    			json.put(prop, PropertyUtils.getProperty(entity, prop));
	    		} else {
	    			String refProp = prop.substring(0, pos).trim();
	    			Object refObject = PropertyUtils.getProperty(entity, refProp);
	    			json.put(refProp, toJSON(refObject, prop.substring(pos)));
	    		}
    		} catch (Exception e) {
    			log.error(e);
    		}
    	}
    	return json;
    }
    
    private String[] extractFirstLevelProperties(String propExp) {
		propExp = propExp.trim();
		if (propExp.startsWith("{") && propExp.endsWith("}")) {
			propExp = propExp.substring(1, propExp.length() - 1).trim();
		}
		ArrayList<String> result = new ArrayList<String>();
		StringBuffer item = new StringBuffer();
		int deep = 0;
		for (int i = 0; i < propExp.length(); i++) {
			if (propExp.charAt(i) == '{') {
				deep++;
				item.append(propExp.charAt(i));
			} else if (propExp.charAt(i) == '}') {
				deep--;
				item.append(propExp.charAt(i));
			} else if (propExp.charAt(i) == ',' && deep == 0) {
				result.add(item.toString().trim());
				item = new StringBuffer();
			} else {
				item.append(propExp.charAt(i));
			}
		}
		if (!item.toString().isEmpty()) {
			result.add(item.toString().trim());
		}
		return result.toArray(new String[0]);
	}


    @Override
	public JSONObject toGridJSON(Collection<?> entities, Integer rows, Integer page, Integer records, String idProp, String cellProps) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {

        JSONArray jsonArrayGrid = new JSONArray();
        JSONObject jsonGrid = new JSONObject();
        
        for (Object entity : entities) {
        	JSONObject jsonRow = new JSONObject();
        	if (idProp != null) {
        		jsonRow.put("id", PropertyUtils.getProperty(entity, idProp));
        	}
        	JSONArray jsonCell = new JSONArray();
        	String[] propArray = cellProps.split(",");
        	for (String prop : propArray) {
        		Object value = null;
        		try {
        			value = PropertyUtils.getProperty(entity, prop);
        		} catch (Exception e) {
        			log.error(e);
        		}
        		jsonCell.add(value);
        	}
        	jsonRow.put("cell", jsonCell);
        	jsonArrayGrid.add(jsonRow);
        }

        int totalPages = records > 0 ? (int) ceil(records / (float) rows) : 0;
        if (page > totalPages) page = totalPages;

        jsonGrid.put("page", page);
        jsonGrid.put("total", totalPages);
        jsonGrid.put("records", records);
                
        jsonGrid.put("rows", jsonArrayGrid);

//        Map<String, Object> userData=new HashMap<String, Object>();
//        jsonGrid.put("userData",userData);
        
        log.debug(Util.jsonFormat(jsonGrid.toString()));
        return jsonGrid;
    }

    public JSONArray toTreeJSON(Object entity, Map<String, Collection<String>> propFilter, Map<Class, Map<String, String>>... vpropRename) {

        JsonConfig config = getJsonConfig(propFilter, vpropRename);
        JSONArray jsonTree = JSONArray.fromObject(entity, config);
        Collection<Map.Entry> entries = findEntry(jsonTree, "attr");
        for (Map.Entry entry : entries) {
            entry.setValue(JSONObject.fromObject(String.format("{'id':%d}", entry.getValue())));
        }
        return jsonTree;
    }



    public JSONArray toSelectJSON(Object entity, Map<String, Collection<String>> propFilter, Map<Class, Map<String, String>>... vpropRename) {
        JsonConfig config = getJsonConfig(propFilter, vpropRename);

        JSONArray jsonTree = JSONArray.fromObject(entity, config);
        return jsonTree;
    }

    /**
     * Construct JsonConfig according to the given parameter
     */
    private JsonConfig getJsonConfig(final Map<String, Collection<String>> propFilter, Map<Class, Map<String, String>>... vpropRename) {
        JsonConfig config = new JsonConfig();
        final Map<Class, Map<String, String>> propRename = vpropRename.length > 0 ? vpropRename[0] : new HashMap<Class, Map<String, String>>();

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
        for (final Map.Entry<Class, Map<String, String>> entry : propRename.entrySet()) {
            config.registerJsonPropertyNameProcessor(entry.getKey(), new PropertyNameProcessor() {
                @Override
                public String processPropertyName(Class beanClass, String name) {
                    if (null != (entry.getValue().get(name))) {
                        return entry.getValue().get(name);
                    }
                    return name;
                }
            });
        }

        return config;
    }

    /**
     * Find all the entries in json array with specific key
     */
    private Collection<Map.Entry> findEntry(JSONArray array, String key) {
        List<Map.Entry> entries = new ArrayList<Map.Entry>();
        for (Object obj : array) {
            if (obj instanceof JSONObject) {
                JSONObject jObj = (JSONObject) obj;
                for (Object entryObj : jObj.entrySet()) {
                    Map.Entry entry = (Map.Entry) entryObj;
                    if (entry.getKey().equals(key)) {
                        entries.add(entry);
                    }
                    if (entry.getValue() instanceof JSONArray) {
                        entries.addAll(findEntry((JSONArray) entry.getValue(), key));
                    }
                }
            } else { //JSONArray
                JSONArray jArray = (JSONArray) obj;
                entries.addAll(findEntry(jArray, key));
            }
        }
        return entries;
    }

}
