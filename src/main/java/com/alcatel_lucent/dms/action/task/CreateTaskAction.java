package com.alcatel_lucent.dms.action.task;

import com.alcatel_lucent.dms.action.JSONAction;
import com.alcatel_lucent.dms.service.TaskService;

@SuppressWarnings("serial")
public class CreateTaskAction extends JSONAction {

	private TaskService taskService;
	
	private Long prod;
	private Long app;
	private String name;
	private String dict;
	private String language;
	
	@Override
	protected String performAction() throws Exception {
		log.info("CreateTaskAction: prod=" + prod + ", app=" + app + ", dict=" + dict + ", language=" + language);
		taskService.createTask(prod, app, name, toIdList(dict), toIdList(language));
		setMessage(getText("message.success"));
		return SUCCESS;
	}

	public String getDict() {
		return dict;
	}

	public void setDict(String dict) {
		this.dict = dict;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public Long getProd() {
		return prod;
	}

	public void setProd(Long prod) {
		this.prod = prod;
	}

	public TaskService getTaskService() {
		return taskService;
	}

	public void setTaskService(TaskService taskService) {
		this.taskService = taskService;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getApp() {
		return app;
	}

	public void setApp(Long app) {
		this.app = app;
	}

}
