package com.alcatel_lucent.dms.action.admin;

import com.alcatel_lucent.dms.SystemError;
import com.alcatel_lucent.dms.action.JSONAction;
import com.alcatel_lucent.dms.service.LanguageService;

@SuppressWarnings("serial")
public class LanguageAction extends JSONAction {
	
	private LanguageService languageService;
	
	private String oper;
	private String id;
	private String name;
	private Long defaultCharset;

	@Override
	protected String performAction() throws Exception {
		if (oper.equals("add")) {
			languageService.createLanguage(name, defaultCharset);
		} else if (oper.equals("edit")) {
			languageService.updateLanguage(Long.valueOf(id), name, defaultCharset);
		} else if (oper.equals("del")) {
			languageService.deleteLanguages(toIdList(id));
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

	public Long getDefaultCharset() {
		return defaultCharset;
	}

	public void setDefaultCharset(Long defaultCharset) {
		this.defaultCharset = defaultCharset;
	}

	public LanguageService getLanguageService() {
		return languageService;
	}

	public void setLanguageService(LanguageService languageService) {
		this.languageService = languageService;
	}

}
