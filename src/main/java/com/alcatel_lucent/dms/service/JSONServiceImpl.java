package com.alcatel_lucent.dms.service;

import com.alcatel_lucent.dms.UserContext;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.processors.PropertyNameProcessor;
import net.sf.json.util.PropertyFilter;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import views.JQGrid;

import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.lang.Math.ceil;

@Service("jsonService")
@SuppressWarnings("unchecked")
public class JSONServiceImpl implements JSONService {

    private static Logger log = LoggerFactory.getLogger(JSONServiceImpl.class);
    private static final DateFormat dFmt = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");


    // Create the node factory that gives us nodes.
    private JsonNodeFactory factory = new JsonNodeFactory(false);

    // create a json factory to write the treenode as json.
    private JsonFactory jsonFactory = new JsonFactory();
    private ObjectMapper mapper = new ObjectMapper();  //reuse

    public String toJSONString(Object entity, String propExp) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        return toJSON(entity, propExp).toString(4);
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
                    json.put(prop, PropertyUtils.getProperty(entity, prop.trim()));
                } else {
                    String refProp = prop.substring(0, pos).trim();
                    Object refObject = PropertyUtils.getProperty(entity, refProp);
                    if (refObject == null) {
                        json.put(refProp, null);
                    } else {
                        json.put(refProp, toJSON(refObject, prop.substring(pos)));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error(e.toString());
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
                jsonRow.put("id", PropertyUtils.getProperty(entity, idProp.trim()));
            }
            JSONArray jsonCell = new JSONArray();
            String[] propArray = cellProps.split(",");
            for (String prop : propArray) {
                Object value = null;
                try {
                    value = PropertyUtils.getProperty(entity, prop.trim());
                } catch (Exception e) {
                    log.error(e.toString());
                }
                if (value != null) {    // escape html tags in grid result
                    if (value instanceof Date) {
                        UserContext uc = UserContext.getInstance();
                        TimeZone timeZone = null == uc ? TimeZone.getDefault() : uc.getTimeZone();
                        String strTmp = null == uc ? "server" : "client";
                        dFmt.setTimeZone(timeZone);
                        log.debug("format Date with {} time zone {}", strTmp, timeZone);
                        value = dFmt.format((Date) value);
                    } else {
                        value = StringEscapeUtils.escapeHtml(value.toString());
                    }
                } else {
                    value = StringUtils.EMPTY;
                }
                jsonCell.add(value);
            }
            jsonRow.put("cell", jsonCell);
            jsonArrayGrid.add(jsonRow);
        }

        if (rows != null && page != null) {
            int totalPages = (records != null && records > 0) ? (int) ceil(records / (float) rows) : 0;
            if (page > totalPages) page = totalPages;

            jsonGrid.put("page", page);
            jsonGrid.put("total", totalPages);
            jsonGrid.put("records", records);

        } else {
            jsonGrid.put("records", entities.size());
        }
        jsonGrid.put("rows", jsonArrayGrid);

//        Map<String, Object> userData=new HashMap<String, Object>();
//        jsonGrid.put("userData",userData);

        log.debug(jsonGrid.toString(4));
        return jsonGrid;
    }

    @Override
    public String toGridJSONIncludeJSONString(Collection<?> entities, Integer rows, Integer page, Integer records, String idProp, String cellProps)
            throws JsonProcessingException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {

        JQGrid grid = new JQGrid();
        if (idProp != null) {
            grid.setIdProp(idProp);
        }
        for (Object entity : entities) {
            List<String> cells = new ArrayList<String>();
            String[] propArray = cellProps.split(",");
            for (String prop : propArray) {
                Object value = null;
                try {
                    value = PropertyUtils.getProperty(entity, prop.trim());
                } catch (Exception e) {
                    log.error(e.toString());
                }
                if (value != null) {    // escape html tags in grid result
                    if (value instanceof Date) {
                        UserContext uc = UserContext.getInstance();
                        TimeZone timeZone = null == uc ? TimeZone.getDefault() : uc.getTimeZone();
                        String strTmp = null == uc ? "server" : "client";
                        dFmt.setTimeZone(timeZone);
                        log.debug("format Date with {} time zone {}", strTmp, timeZone);
                        value = dFmt.format((Date) value);
                    } else if (value instanceof Collection) {
                        value = mapper.writeValueAsString(value);
                    } else {
                        value = StringEscapeUtils.escapeHtml(value.toString());
                    }
                } else {
                    value = StringUtils.EMPTY;
                }
                cells.add((String) value);
            }
            Map<String, Object> row = grid.addRow(cells);
            if (idProp != null) {
                String id = PropertyUtils.getProperty(entity, grid.getIdProp()).toString();
                row.put("id", id);
            }
        }

        if (rows != null && page != null) {
            int totalPages = (records != null && records > 0) ? (int) ceil(records / (float) rows) : 0;
            if (page > totalPages) page = totalPages;
            grid.setPage(page);
            grid.setTotal(totalPages);
            grid.setRecords(records);
        } else {
            grid.setRecords(entities.size());
        }
        return mapper.writeValueAsString(grid);
    }

    public JSONArray toTreeJSON2(Object entity, Map<String, Collection<String>> propFilter, Map<Class, Map<String, String>>... vpropRename) {

        JsonConfig config = getJsonConfig(propFilter, vpropRename);
        JSONArray jsonTree = JSONArray.fromObject(entity, config);
        Collection<Map.Entry> entries = findEntry(jsonTree, "attr");
        for (Map.Entry entry : entries) {
            entry.setValue(JSONObject.fromObject(String.format("{'id':%d}", entry.getValue())));
        }
        return jsonTree;
    }

    public JSONObject toTreeJSON(Object root, String[] idProp, String[] types, String[] dataProp, String[] childrenProp) {
        JSONObject result = new JSONObject();
        try {
            Object id = PropertyUtils.getProperty(root, idProp[0]);
            Object data = PropertyUtils.getProperty(root, dataProp[0]);
            JSONObject jsonAttr = new JSONObject();
            jsonAttr.put("id", id);
            jsonAttr.put("type", types[0]);
            result.put("attr", jsonAttr);
            result.put("data", data);
            if (idProp.length > 1 && types.length > 1 && dataProp.length > 1 && childrenProp.length > 0) {
                Object children = PropertyUtils.getProperty(root, childrenProp[0]);
                if (children != null && children instanceof Collection) {
                    JSONArray jsonChildren = new JSONArray();
                    for (Object obj : (Collection<?>) children) {
                        jsonChildren.add(toTreeJSON(obj, next(idProp), next(types), next(dataProp), next(childrenProp)));
                    }
                    result.put("children", jsonChildren);
                }
            }
            return result;
        } catch (Exception e) {
            log.error("Failed to populate tree json of object " + root, e);
            return result;
        }
    }

    private String[] next(String[] arr) {
        String[] result = new String[arr.length - 1];
        for (int i = 0; i < result.length; i++) {
            result[i] = arr[i + 1];
        }
        return result;
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
