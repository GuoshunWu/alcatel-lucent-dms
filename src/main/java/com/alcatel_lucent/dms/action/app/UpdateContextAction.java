package com.alcatel_lucent.dms.action.app;

import com.alcatel_lucent.dms.action.JSONAction;
import com.alcatel_lucent.dms.service.DictionaryService;

@SuppressWarnings("serial")
public class UpdateContextAction extends JSONAction {

	private DictionaryService dictionaryService;
	
	private String id;
	private String context;
	
	@Override
	protected String performAction() throws Exception {
		dictionaryService.updateLabels(toIdList(id), null, null, context);
		return SUCCESS;
	}

	public DictionaryService getDictionaryService() {
		return dictionaryService;
	}

	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}
	

}
