package com.alcatel_lucent.dms.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alcatel_lucent.dms.model.Context;
import com.alcatel_lucent.dms.model.Label;
import com.alcatel_lucent.dms.model.LabelTranslation;
import com.alcatel_lucent.dms.model.Translation;
import com.alcatel_lucent.dms.service.DictionaryService;
import com.alcatel_lucent.dms.service.TranslationService;

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
	
	private static Logger log= LoggerFactory.getLogger(LabelREST.class);
	
    @Autowired
    private DictionaryService dictionaryService;
    
    @Autowired
    private TranslationService translationService;
    
    @Override
    @SuppressWarnings("rawtypes")
    public Class getEntityClass() {
    	return Label.class;
    }
    
    @Override
    protected String doGetOrPost(Map<String, String> requestMap) throws Exception {
    	Long dictId = Long.valueOf(requestMap.get("dict"));
    	String text = requestMap.get("text");
    	if (text != null && text.trim().isEmpty()) {
    		text = null;
    	}
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
		String hql = "select obj from Label obj where obj.dictionary.id=:dictId";
    	String countHql = "select count(*) from Label where dictionary.id=:dictId";
    	Map param = new HashMap();
    	Map countParam = new HashMap();
    	param.put("dictId", dictId);
    	countParam.put("dictId", dictId);
    	if (text != null) {
    		hql += " and obj.reference like :text";
    		countHql += " and reference like :text";
    		param.put("text", "%" + text + "%");
    		countParam.put("text", "%" + text + "%");
    	}
    	LabelSorter tniSorter = null;
		if (!sidx.equals("t") && !sidx.equals("n") && !sidx.equals("i")) {
			hql += " order by " + sidx + " " + sord;
		} else {
			tniSorter = new LabelSorter(sidx, sord);
		}
		Collection<Label> labels; 
		Map<Long, Label> labelMap = new HashMap<Long, Label>();
		if (langId == null) {
			if (tniSorter == null) {
				labels  = retrieve(hql, param, countHql, countParam, requestMap);
			} else {
				labels = new ArrayList<Label>(dao.retrieve(hql, param, null));
				requestMap.put("records", "" + labels.size());
			}
			// add T/N/I information if no language was specified
			Map<Long, int[]> summary = translationService.getLabelTranslationSummary(dictId);
			for (Label label : labels) {
				int[] tni = summary.get(label.getId());
				label.setT(tni[0]);
				label.setN(tni[1]);
				label.setI(tni[2]);
			}
			if (tniSorter != null) {
				Collections.sort((ArrayList<Label>)labels, tniSorter);
				labels = pageFilter(labels, requestMap);
			}
			
		} else {
		// add ot and ct information if a specific language was specified
			labels = translationService.getLabelsWithTranslation(dictId, langId);
        	Map<String, String> filters = getGridFilters(requestMap);
        	Integer statusFilter = null;
        	if (filters != null) {
        		String statusParam = filters.get("ct.status");
        		if (statusParam != null && !statusParam.isEmpty()) {
        			statusFilter = Integer.valueOf(statusParam);
                	// apply status filter
            		Iterator<Label> iter = labels.iterator();
            		while (iter.hasNext()) {
            			Label label = iter.next();
            			if (statusFilter != label.getCt().getStatus()) {
            				iter.remove();
            			}
            		}
        		}
        	}
    		requestMap.put("records", "" + labels.size());
    		// filter by page
    		labels = pageFilter(labels, requestMap);
    	}
    	
    	return toJSON(labels, requestMap);
    }

	public TranslationService getTranslationService() {
		return translationService;
	}

	public void setTranslationService(TranslationService translationService) {
		this.translationService = translationService;
	}

}

class LabelSorter implements Comparator<Label> {
	private String field, sord;
	public LabelSorter(String field, String sord) {
		this.field = field;
		this.sord = sord;
	}
	@Override
	public int compare(Label label1, Label label2) {
		if (field.equals("t")) {
			return (sord.equalsIgnoreCase("ASC") ? 1 : -1 ) * (label1.getT() - label2.getT());
		} else if (field.equals("n")) {
			return (sord.equalsIgnoreCase("ASC") ? 1 : -1 ) * (label1.getN() - label2.getN());
		} else if (field.equals("i")) {
			return (sord.equalsIgnoreCase("ASC") ? 1 : -1 ) * (label1.getI() - label2.getI());
		}
		return 0;
	}
}
