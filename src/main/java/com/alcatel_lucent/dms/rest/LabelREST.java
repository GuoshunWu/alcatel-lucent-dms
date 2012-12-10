package com.alcatel_lucent.dms.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
 *   NOTE: only support filter "ct.status" for the moment
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
//    	if (!sidx.startsWith("ot.") && !sidx.startsWith("ct.")) {
//    		sidx = "obj." + sidx;
//    	}
    	
    	Long langId = requestMap.get("language") == null ? null : Long.valueOf(requestMap.get("language"));
    	String hql;
    	String countHql = "select labels.size from Dictionary where id=:dictId";
    	Map param = new HashMap();
    	Map countParam = new HashMap();
    	param.put("dictId", dictId);
    	countParam.put("dictId", dictId);
		hql = "select obj from Label obj where obj.dictionary.id=:dictId";
		hql += " order by " + sidx + " " + sord;
		Collection<Label> labels; 
		Map<Long, Label> labelMap = new HashMap<Long, Label>();
		if (langId == null) {
			labels  = retrieve(hql, param, countHql, countParam, requestMap);
		} else {
		// add ot and ct information if a specific language was specified
			labels = dao.retrieve(hql, param);
			for (Label label : labels) {
				labelMap.put(label.getId(), label);
			}
        	Map<String, String> filters = getGridFilters(requestMap);
        	Integer statusFilter = null;
        	if (filters != null) {
        		String statusParam = filters.get("ct.status");
        		if (statusParam != null && !statusParam.isEmpty()) {
        			statusFilter = Integer.valueOf(statusParam);
        		}
        	}
    		hql = "select l.id,ot" +
    				" from Label l join l.origTranslations ot" +
    				" where l.dictionary.id=:dictId and ot.language.id=:langId";
    		param.put("langId", langId);
    		Collection<Object[]> qr = dao.retrieve(hql, param);
    		for (Object[] row : qr) {
    			Long labelId = ((Number) row[0]).longValue();
    			LabelTranslation ot = (LabelTranslation) row[1];
    			Label label = labelMap.get(labelId);
    			if (label != null) {
    				label.setOt(ot);
    			}
    		}
    		hql = "select l.id,ct" +
    				" from Label l join l.text.translations ct" +
    				" where l.dictionary.id=:dictId and ct.language.id=:langId";
    		qr = dao.retrieve(hql, param);
    		for (Object[] row : qr) {
    			Long labelId = ((Number) row[0]).longValue();
    			Translation ct = (Translation) row[1];
    			Label label = labelMap.get(labelId);
    			if (label != null) {
    				label.setCt(ct);
    			}
    		}

        	// populate default ct and ot values, and apply status filter
    		Iterator<Label> iter = labels.iterator();
    		while (iter.hasNext()) {
    			Label label = iter.next();
    			if (label.getOt() == null) {
    				LabelTranslation ot = new LabelTranslation();
    				ot.setOrigTranslation(label.getReference());
    				ot.setNeedTranslation(true);
    				label.setOt(ot);
    			}
    			if (label.getCt() == null) {
    				Translation ct = new Translation();
    				ct.setId(-(label.getId() * 1000 + langId));	// virtual tid < 0, indicating a non-existing ct object
    				ct.setTranslation(label.getReference());
    				ct.setStatus(label.getOt().isNeedTranslation() ? Translation.STATUS_UNTRANSLATED : Translation.STATUS_TRANSLATED);
    				label.setCt(ct);
    			}
    			if (statusFilter != null && statusFilter != label.getCt().getStatus()) {
    				iter.remove();
    			}
    		}
    		requestMap.put("records", "" + labels.size());
    		/*
    		if (statusFilter != null) {
    			int count;
    			if (statusFilter == Translation.STATUS_TRANSLATED) {
    				count = countT;
    			} else if (statusFilter == Translation.STATUS_IN_PROGRESS) {
    				count = countI;
    			} else {
    				int countAll = Integer.parseInt(requestMap.get("records"));
    				count = countAll - countT - countI;
    			}
    			requestMap.put("records", "" + count);
    		}
    		*/
    		
    		// filter by page
    		labels = pageFilter(labels, requestMap);
    	}
    	
    	return toJSON(labels, requestMap);
    }

}
