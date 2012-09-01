package com.alcatel_lucent.dms.service;

import java.util.*;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.processors.PropertyNameProcessor;
import net.sf.json.util.PropertyFilter;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

@Service("jsonService")
@SuppressWarnings("unchecked")
public class JSONServiceImpl implements JSONService {

    private static Logger log= Logger.getLogger(JSONServiceImpl.class);

    public String toJSONString(Object entity, Map<String, Collection<String>> propFilter, Map<Class, Map<String, String>>... vpropRename) {
        JsonConfig config = getJsonConfig(propFilter, vpropRename);
        if (entity instanceof Collection) {
            return JSONArray.fromObject(entity, config).toString();
        }
        return JSONObject.fromObject(entity, config).toString();
    }

    public JSONObject toGridJSON(Object entity,Map<String,Object> pageData, Map<String, Collection<String>> propFilter,Map<Class, Map<String, String>>... vpropRename) {
        JsonConfig config = getJsonConfig(propFilter, vpropRename);

        JSONArray jsonArrayGrid = JSONArray.fromObject(entity, config);
        JSONObject jsonGrid = new JSONObject();
        jsonGrid.putAll(pageData);
        jsonGrid.put("rows", jsonArrayGrid);

        log.debug(jsonGrid);
        return jsonGrid;
    }

    public JSONArray toTreeJSON(Object entity, Map<String, Collection<String>> propFilter,Map<Class, Map<String, String>>... vpropRename) {

        JsonConfig config = getJsonConfig(propFilter, vpropRename);
        JSONArray jsonTree = JSONArray.fromObject(entity, config);
        Collection<Map.Entry> entries = findEntry(jsonTree, "attr");
        for (Map.Entry entry : entries) {
            entry.setValue(JSONObject.fromObject(String.format("{'id':%d}", entry.getValue())));
        }
        return jsonTree;
    }

    public JSONArray toSelectJSON(Object entity,Map<String, Collection<String>> propFilter, Map<Class, Map<String, String>>... vpropRename) {
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
