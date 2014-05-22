package com.alcatel_lucent.dms.rest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alcatel_lucent.dms.model.Text;
import com.alcatel_lucent.dms.service.TextService;
import com.alcatel_lucent.dms.util.ObjectComparator;

/**
 * Diff Text REST service.
 * URL: /rest/diff/texts
 * Filter parameters:
 *   text		(required) text id to be compared
 *   
 * Sort parameters:
 *   sidx		(optional) sort by, default is "context.id"
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
 *   		
 * @author allany
 *
 */

@Path("diff/texts")
@Component("diffTextREST")
public class DiffTextREST extends BaseREST {

	@Autowired
	private TextService textService;
	
	@Override
	String doGetOrPost(Map<String, String> requestMap) throws Exception {
		Long textId = Long.valueOf(requestMap.get("text"));
		List<Text> texts = new ArrayList<Text>(textService.getDiffTexts(textId));

		// additional calculation fields
		textService.populateTranslationSummary(texts);
		textService.populateRefs(texts);
		
		String sidx = requestMap.get("sidx");
    	String sord = requestMap.get("sord");
    	if (sidx == null || sidx.trim().isEmpty()) {
    		sidx = "context.id";
    	}
    	if (sord == null) {
    		sord = "ASC";
    	}
		Comparator comparator = new ObjectComparator<Text>(sidx, sord);
		Collections.sort(texts, comparator);
		return toJSON(texts, requestMap);
	}

	@Override
	Class getEntityClass() {
		return Text.class;
	}

}
