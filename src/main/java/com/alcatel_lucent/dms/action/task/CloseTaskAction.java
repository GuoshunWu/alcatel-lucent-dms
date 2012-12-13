package com.alcatel_lucent.dms.action.task;

import com.alcatel_lucent.dms.action.JSONAction;
import com.alcatel_lucent.dms.service.TaskService;

@SuppressWarnings("serial")
public class CloseTaskAction extends JSONAction {

	private TaskService taskService;
	
	private Long id;
	
	@Override
	protected String performAction() throws Exception {
		log.info("CloseTaskAction: id=" + id);
		taskService.closeTask(id);
		setMessage(getText("message.success"));
		return SUCCESS;
	}

	public TaskService getTaskService() {
		return taskService;
	}

	public void setTaskService(TaskService taskService) {
		this.taskService = taskService;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

}
