package com.alcatel_lucent.dms.action.app;

import com.alcatel_lucent.dms.action.JSONAction;
import com.alcatel_lucent.dms.service.DictionaryService;

@SuppressWarnings("serial")
public class DeleteLabelAction extends JSONAction {
	
	private String id;
	
	private DictionaryService dictionaryService;

	@Override
	protected String performAction() throws Exception {
		dictionaryService.deleteLabels(toIdList(id));
		return SUCCESS;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public DictionaryService getDictionaryService() {
		return dictionaryService;
	}

	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

}
