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
import com.alcatel_lucent.dms.util.ObjectComparator;

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
    	Collections.sort((ArrayList<LabelTranslation>)result, new ObjectComparator<LabelTranslation>(sidx, sord));
    	result = pageFilter(result, requestMap);
    	return toJSON(result, requestMap);
    }
}
