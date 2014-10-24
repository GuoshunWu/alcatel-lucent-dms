package com.alcatel_lucent.dms.action.app;

import java.util.Collection;

import org.apache.struts2.convention.annotation.Result;

import com.alcatel_lucent.dms.action.JSONAction;
import com.alcatel_lucent.dms.model.Task;
import com.alcatel_lucent.dms.service.DictionaryService;
import com.alcatel_lucent.dms.service.TaskService;

@SuppressWarnings("serial")
@Result(type = "json", params = {"noCache", "true", "ignoreHierarchy", "false", "includeProperties", "id,permanent,message,status"})
public class RemoveDictAction extends JSONAction {
	
	private DictionaryService dictionaryService;
	private TaskService taskService;
	
    private boolean permanent;	// deprecated, it is auto determined if permanently delete dict
    private String id;
    private Long appId;
    private String deleteTask;	// "true" if confirm to delete task

    public String performAction() throws Exception {
    	log.info("RemoveDictAction: id=" + id + ", appId=" + appId + ", deleteTask=" + deleteTask + ", permanent=" + permanent);
    	Collection<Task> tasks = taskService.findAllDictRelatedTasks(toIdList(id));
    	// need confirmation if there is any related task and deleteTask flag is not set to "true"
    	if (!tasks.isEmpty() && (deleteTask == null || !deleteTask.equals("true"))) {
    		StringBuffer taskNames = new StringBuffer();
    		for (Task task : tasks) {
    			taskNames.append(task.getName()).append("\n");
    		}
    		setStatus(1);
    		setMessage(taskNames.toString());
    		return SUCCESS;
    	}
    	if (!tasks.isEmpty()) {	// confirmed to delete related tasks
    		for (Task task : tasks) {
    			taskService.deleteTask(task.getId());
    		}
    	}
    	dictionaryService.removeDictionaryFromApplication(appId, toIdList(id));
    	setStatus(0);
    	setMessage(getText("message.success"));
    	return SUCCESS;
    }

	public DictionaryService getDictionaryService() {
		return dictionaryService;
	}

	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	public Long getAppId() {
		return appId;
	}

	public void setAppId(Long appId) {
		this.appId = appId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public boolean isPermanent() {
		return permanent;
	}

	public void setPermanent(boolean permanent) {
		this.permanent = permanent;
	}

	public String getDeleteTask() {
		return deleteTask;
	}

	public void setDeleteTask(String deleteTask) {
		this.deleteTask = deleteTask;
	}

	public TaskService getTaskService() {
		return taskService;
	}

	public void setTaskService(TaskService taskService) {
		this.taskService = taskService;
	}
}
