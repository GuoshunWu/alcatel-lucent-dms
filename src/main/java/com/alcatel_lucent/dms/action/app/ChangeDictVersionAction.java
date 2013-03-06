package com.alcatel_lucent.dms.action.app;

import com.alcatel_lucent.dms.action.JSONAction;
import com.alcatel_lucent.dms.service.DictionaryService;

@SuppressWarnings("serial")
public class ChangeDictVersionAction extends JSONAction {
	
	private DictionaryService dictionaryService;
	private Long appId;
	private Long id;	// old dict id
	private Long newDictId;
	
	protected String performAction() throws Exception {
		dictionaryService.changeDictionaryInApp(appId, newDictId);
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

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getNewDictId() {
		return newDictId;
	}

	public void setNewDictId(Long newDictId) {
		this.newDictId = newDictId;
	}
	
}
