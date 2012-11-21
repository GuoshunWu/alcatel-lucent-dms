package com.alcatel_lucent.dms.rest;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Path;

import org.springframework.stereotype.Component;

import com.alcatel_lucent.dms.model.Task;

/**
 * Task REST service.
 * URL: /rest/tasks
 * Filter parameters:
 *   prod		(required) product id
 *   
 * Sort parameters:
 *   sidx		(optional) sort by, default is "createTime"
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
 *   The result is not paged, that means "rows" and "page" parameter will not be supported.
 *   		
 * @author allany
 *
 */
@Path("tasks")
@Component("taskREST")
public class TaskREST extends BaseREST {

	@Override
	String doGetOrPost(Map<String, String> requestMap) throws Exception {
		String prod = requestMap.get("prod");
		String hql = "from Task where product.id=:prodId";
		String countHql = "select count(*) from Task where product.id=:prodId";
		Map param = new HashMap();
		param.put("prodId", Long.valueOf(prod));
		
    	String sidx = requestMap.get("sidx");
    	String sord = requestMap.get("sord");
    	if (sidx == null || sidx.trim().isEmpty()) {
    		sidx = "createTime";
    	}
    	if (sord == null) {
    		sord = "ASC";
    	}
    	hql += " order by " + sidx + " " + sord;
    	
		Collection<Task> tasks = retrieve(hql, param, countHql, param, requestMap);
		return toJSON(tasks, requestMap);
	}

	@Override
	Class getEntityClass() {
		return Task.class;
	}

}
