package com.alcatel_lucent.dms.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import javax.ws.rs.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alcatel_lucent.dms.model.Translation;
import com.alcatel_lucent.dms.service.TextService;
import com.alcatel_lucent.dms.util.ObjectComparator;

/**
 * Diff Translation REST service.
 * URL: /rest/diff/text/translations
 * Filter parameters:
 *   text1		(required) text id (A) to be compared
 *   text2		(required) text id (B) to be compared
 *   
 * Sort parameters:
 *   sidx		(optional) sort by, default is "translation1.language.name"
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

@Path("diff/text/translations")
@Component("diffTranslationREST")
public class DiffTranslationREST extends BaseREST {
	
	@Autowired
	private TextService textService;

	@Override
	String doGetOrPost(Map<String, String> requestMap) throws Exception {
		Long textId1 = Long.valueOf(requestMap.get("text1"));
		Long textId2 = Long.valueOf(requestMap.get("text2"));
		Collection<Translation[]> diffTrans = textService.findDiffTranslations(textId1, textId2);
		ArrayList<TranslationPair> result = new ArrayList<TranslationPair>();
		for (Translation[] trans : diffTrans) {
			result.add(new TranslationPair(trans[0], trans[1]));
		}
		
		String sidx = requestMap.get("sidx");
    	String sord = requestMap.get("sord");
    	if (sidx == null || sidx.trim().isEmpty()) {
    		sidx = "a.language.name";
    	}
    	if (sord == null) {
    		sord = "ASC";
    	}
		Comparator comparator = new ObjectComparator<TranslationPair>(sidx, sord);
		Collections.sort(result, comparator);
		return toJSON(result, requestMap);
	}

	@Override
	Class getEntityClass() {
		return TranslationPair.class;
	}

}

