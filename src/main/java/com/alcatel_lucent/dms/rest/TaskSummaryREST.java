package com.alcatel_lucent.dms.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import javax.ws.rs.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alcatel_lucent.dms.model.Context;
import com.alcatel_lucent.dms.service.TaskService;

/**
 * Task summary REST service.
 * URL: /rest/task/summary
 * Filter parameters:
 *   task		(required) task id
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
@Path("task/summary")
@Component("taskSummaryREST")
public class TaskSummaryREST extends BaseREST {
	
	@Autowired
	private TaskService taskService;

	@Override
	String doGetOrPost(Map<String, String> requestMap) throws Exception {
		String task = requestMap.get("task");
		Map<Long, Map<Long, int[]>> taskSummary = taskService.getTaskSummary(Long.valueOf(task));
		Collection<TaskContext> result = new ArrayList<TaskContext>();
		for (Long contextId : taskSummary.keySet()) {
			Context context = (Context) dao.retrieve(Context.class, contextId);
			TaskContext tc = new TaskContext();
			tc.setContext(context);
			tc.setS(taskSummary.get(contextId));
			tc.count();
			result.add(tc);
		}
		requestMap.put("records", "" + result.size());
		return toJSON(result, requestMap);
	}

	@SuppressWarnings("rawtypes")
	@Override
	Class getEntityClass() {
		// not supported
		return null;
	}

}
