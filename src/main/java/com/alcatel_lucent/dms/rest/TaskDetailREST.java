package com.alcatel_lucent.dms.rest;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Path;

import org.springframework.stereotype.Component;

import com.alcatel_lucent.dms.model.TaskDetail;

/**
 * Label REST service.
 * URL: /rest/labels
 * Filter parameters:
 *   task		(required) task id
 *   context	(required) context id
 *   language	(required) language id
 *   translated (required) "1" or "0" indicating translated details or not translated details
 *   
 * Sort parameters:
 *   sidx		(optional) sort by, default is "id"
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
@Path("task/details")
@Component("taskDetailREST")
public class TaskDetailREST extends BaseREST {

	@Override
	String doGetOrPost(Map<String, String> requestMap) throws Exception {
		String task = requestMap.get("task");
		String context = requestMap.get("context");
		String language = requestMap.get("language");
		String translated = requestMap.get("translated");
		String hql = "from TaskDetail where task.id=:taskId and text.context.id=:contextId and language.id=:languageId";
		if (translated != null && translated.equals("1")) {
			hql += " and newTranslation<>text.reference";
		} else {
			hql += " and (newTranslation is null or newTranslation='' or newTranslation=text.reference)";
		}
		String countHql = "select count(*) " + hql;
    	String sidx = requestMap.get("sidx");
    	String sord = requestMap.get("sord");
    	if (sidx == null || sidx.trim().isEmpty()) {
    		sidx = "id";
    	}
    	if (sord == null) {
    		sord = "ASC";
    	}
    	hql += " order by " +  sidx + " " + sord;
    	Map param = new HashMap();
    	param.put("taskId", Long.valueOf(task));
    	param.put("contextId", Long.valueOf(context));
    	param.put("languageId", Long.valueOf(language));
    	Collection<TaskDetail> result = retrieve(hql, param, countHql, param, requestMap);
		return toJSON(result, requestMap);
	}

	@Override
	Class getEntityClass() {
		return TaskDetail.class;
	}

}
