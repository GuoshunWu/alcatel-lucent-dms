package com.alcatel_lucent.dms.action.app;

import com.alcatel_lucent.dms.action.JSONAction;
import com.alcatel_lucent.dms.service.DictionaryService;

public class ChangeDictVersionAction extends JSONAction {
	
	private DictionaryService dictionaryService;
	private Long appId;
	private Long oldDictId;
	private Long newDictId;
	
	protected String performAction() throws Exception {
		dictionaryService.changeDictionaryInApp(appId, oldDictId, newDictId);
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

	public Long getOldDictId() {
		return oldDictId;
	}

	public void setOldDictId(Long oldDictId) {
		this.oldDictId = oldDictId;
	}

	public Long getNewDictId() {
		return newDictId;
	}

	public void setNewDictId(Long newDictId) {
		this.newDictId = newDictId;
	}
	
}
