package com.alcatel_lucent.dms.action.app;

import com.alcatel_lucent.dms.action.JSONAction;
import com.alcatel_lucent.dms.service.DictionaryService;

@SuppressWarnings("serial")
public class RemoveDictLanguageAction extends JSONAction {
	
	private DictionaryService dictionaryService;
	
	private String id;
	
	protected String performAction() throws Exception {
		log.info("RemoveDictLanguageAction: id=" + id);
		dictionaryService.removeDictionaryLanguage(toIdList(id));
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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	

}
