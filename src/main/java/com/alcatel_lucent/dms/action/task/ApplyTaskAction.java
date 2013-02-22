package com.alcatel_lucent.dms.action.task;

import com.alcatel_lucent.dms.action.JSONAction;
import com.alcatel_lucent.dms.service.TaskService;

@SuppressWarnings("serial")
public class ApplyTaskAction extends JSONAction {
	
	private TaskService taskService;
	
	private Long id;

	@Override
	protected String performAction() throws Exception {
		log.info("ApplyTaskAction: id=" + id);
		taskService.applyTask(id, false);
		setMessage(getText("message.success"));
		return SUCCESS;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public TaskService getTaskService() {
		return taskService;
	}

	public void setTaskService(TaskService taskService) {
		this.taskService = taskService;
	}

}
