package com.alcatel_lucent.dms.action.admin;

import com.alcatel_lucent.dms.SystemError;
import com.alcatel_lucent.dms.action.JSONAction;
import com.alcatel_lucent.dms.service.LanguageService;

@SuppressWarnings("serial")
public class CharsetAction extends JSONAction {
	
	private LanguageService languageService;
	
	private String oper;
	private String id;
	private String name;

	@Override
	protected String performAction() throws Exception {
		log.info("CharsetAction: oper=" + oper + ", id=" + id + ", name=" + name);
		if (oper.equals("add")) {
			languageService.createCharset(name);
		} else if (oper.equals("edit")) {
			languageService.updateCharset(Long.valueOf(id), name);
		} else if (oper.equals("del")) {
			languageService.deleteCharset(toIdList(id));
		} else {
			throw new SystemError("Unknown oper: " + oper);
		}
		return SUCCESS;
	}

	public String getOper() {
		return oper;
	}

	public void setOper(String oper) {
		this.oper = oper;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public LanguageService getLanguageService() {
		return languageService;
	}

	public void setLanguageService(LanguageService languageService) {
		this.languageService = languageService;
	}

}
