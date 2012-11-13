package com.alcatel_lucent.dms.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Path;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alcatel_lucent.dms.model.Label;
import com.alcatel_lucent.dms.model.LabelTranslation;
import com.alcatel_lucent.dms.model.Translation;
import com.alcatel_lucent.dms.service.DictionaryService;

/**
 * Label REST service.
 * URL: /rest/labels
 * Filter parameters:
 *   dict		(required) dictionary id
 *   language	(optional) language id
 *   	If language is supplied, relative LabelTranslation and Translation object can be accessed by
 *   	adding "ot" or "ct" prefix to the property name, e.g. ot.needTranslation,ct.translation
 *   	Otherwise, only label properties can be accessed. 
 *   filters	(optional) jqGrid-style filter string, in json format, e.g.
 *   	{"groupOp":"AND","rules":[{"field":"status","op":"eq","data":"2"}]}
 *   
 * Sort parameters:
 *   sidx		(optional) sort by, default is "sortNo"
 *   			Translation-related properties can be specified by adding "ot" (LabelTranslation) or "ct" (Translation) prefix
 *   sord		(optional) order, default is "ASC"
 *   
 * Format parameters:
 *   format		(optional) format of result string, possible values: "json|grid|tree", default is "json"
 *   prop		(required) properties to be retrieved
 *   			for json: prop={<prop1>,<prop2>,...} where each <prop> can be nested, 
 *   					e.g. <prop2>=<prop_name>{<sub_prop1>,<sub_prop2>}
 *   			for grid: prop=<property_name_for_column1>,<property_name_for_column2>,...
 *   			for tree: prop=<property_name_for_id>,<property_name_for_name>
 *   idprop		(optional) property name for id, for grid only
 *   rows		(optional) number of records to be retrieved, only be used when format is grid
 *   page		(optional) current page, only be used when format is grid
 *   		
 * @author allany
 *
 */
@Path("labels")
@Component("labelREST")
public class LabelREST extends BaseREST {
	
	private static Logger log= Logger.getLogger(LabelREST.class);
	
    @Autowired
    private DictionaryService dictionaryService;
    
    @Override
    @SuppressWarnings("rawtypes")
    public Class getEntityClass() {
    	return Label.class;
    }
    
    @Override
    protected String doGetOrPost(Map<String, String> requestMap) throws Exception {
    	Long dictId = Long.valueOf(requestMap.get("dict"));
    	String sidx = requestMap.get("sidx");
    	String sord = requestMap.get("sord");
    	if (sidx == null || sidx.trim().isEmpty()) {
    		sidx = "sortNo";
    	}
    	if (sord == null) {
    		sord = "ASC";
    	}
    	if (!sidx.startsWith("ot.") && !sidx.startsWith("ct.")) {
    		sidx = "obj." + sidx;
    	}
    	
    	Long langId = requestMap.get("language") == null ? null : Long.valueOf(requestMap.get("language"));
    	String hql;
    	String countHql = "select labels.size from Dictionary where id=:dictId";
    	Map param = new HashMap();
    	Map countParam = new HashMap();
    	param.put("dictId", dictId);
    	countParam.put("dictId", dictId);
    	Collection<Label> labels;
    	if (langId == null) {
    		hql = "select obj from Label obj where obj.dictionary.id=:dictId";
    		hql += " order by " + sidx + " " + sord;
    		labels = retrieve(hql, param, countHql, countParam, requestMap);
    	} else {
    		hql = "select obj,ot,ct" +
    				" from Label obj join obj.origTranslations ot left join obj.text.translations ct" +
    				" where obj.dictionary.id=:dictId and ot.language.id=:langId and ct.language.id=:langId";
        	Map<String, String> filters = getGridFilters(requestMap);
        	if (filters != null) {
        		int i = 0;
        		for (String field : filters.keySet()) {
                	// entity specific conversion
        			Object value = filters.get(field);
        			if (field.equals("ct.status") && value != null && !value.toString().isEmpty()) {
        				value = Integer.valueOf(value.toString());
        			}
        			hql += " and " + field + "=:p" + i;
        			param.put("p" + i, value);
        			i++;
        		}
        	}

    		hql += " order by " + sidx + " " + sord;
    		param.put("langId", langId);
    		Collection<Object[]> result = retrieve(hql, param, countHql, countParam, requestMap);
    		labels = new ArrayList<Label>();
    		for (Object[] row : result) {
    			Label label = (Label) row[0];
    			label.setOt((LabelTranslation) row[1]);
    			label.setCt((Translation) row[2]);
    			labels.add(label);
    		}
    	}
    	
    	return toJSON(labels, requestMap);
    }

}
