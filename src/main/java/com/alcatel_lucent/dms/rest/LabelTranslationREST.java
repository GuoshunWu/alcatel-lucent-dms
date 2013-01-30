package com.alcatel_lucent.dms.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alcatel_lucent.dms.model.Label;
import com.alcatel_lucent.dms.model.LabelTranslation;
import com.alcatel_lucent.dms.service.DaoService;
import com.alcatel_lucent.dms.service.TranslationService;

/**
 * LabelTranslation REST service.
 * URL: /rest/label/translation
 * Filter parameters:
 *   label		(required) label id
 *   status	(optional) status filter
 *   
 * Sort parameters:
 *   sidx		(optional) sort by, default is "languageCode"
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
@Path("label/translation")
@Component("labelTranslationREST")
public class LabelTranslationREST extends BaseREST {
	private static Logger log= LoggerFactory.getLogger(LabelTranslationREST.class);
	
	@Autowired
	private DaoService dao;
	
	@Autowired
	private TranslationService translationService;
	
    @Override
    @SuppressWarnings("rawtypes")
    public Class getEntityClass() {
    	return LabelTranslation.class;
    }

    @Override
    protected String doGetOrPost(Map<String, String> requestMap) throws Exception {
    	Long labelId = Long.valueOf(requestMap.get("label"));
    	String status = requestMap.get("status");
    	String sidx = requestMap.get("sidx");
    	String sord = requestMap.get("sord");
    	if (sidx == null || sidx.trim().isEmpty()) {
    		sidx = "languageCode";
    	}
    	if (sord == null) {
    		sord = "ASC";
    	}
    	
    	Collection<LabelTranslation> result = new ArrayList<LabelTranslation>(
    			translationService.getLabelTranslations(labelId, status == null ? null : Integer.parseInt(status)));
    	requestMap.put("records", "" + result.size());
    	Collections.sort((ArrayList<LabelTranslation>)result, new LabelTranslationSorter(sidx, sord));
    	result = pageFilter(result, requestMap);
    	return toJSON(result, requestMap);
    }
}

class LabelTranslationSorter implements Comparator<LabelTranslation> {
	private String field, sord;
	public LabelTranslationSorter(String field, String sord) {
		this.field = field;
		this.sord = sord;
	}
	@Override
	public int compare(LabelTranslation lt1, LabelTranslation lt2) {
		if (field.equals("sortNo")) {
			return (sord.equalsIgnoreCase("ASC") ? 1 : -1 ) * (lt1.getSortNo() - lt2.getSortNo());
		} else if (field.equals("languageCode")) {
			return (sord.equalsIgnoreCase("ASC") ? 1 : -1 ) * (lt1.getLanguageCode().compareTo(lt2.getLanguageCode()));
		} else if (field.equals("language.name") || field.equals("language")) {
			return (sord.equalsIgnoreCase("ASC") ? 1 : -1 ) * (lt1.getLanguage().getName().compareTo(lt2.getLanguage().getName()));
		} else if (field.equals("translation")) {
			return (sord.equalsIgnoreCase("ASC") ? 1 : -1 ) * (lt1.getTranslation().compareTo(lt2.getTranslation()));
		}
		return 0;
	}
}
