package com.alcatel_lucent.dms.action.app;

import com.alcatel_lucent.dms.action.JSONAction;
import com.alcatel_lucent.dms.service.DictionaryService;

@SuppressWarnings("serial")
public class RemoveDictLanguageAction extends JSONAction {
	
	private DictionaryService dictionaryService;
	
	private String id;	// DictionaryLanguage ID list
	
	// or
	private String dicts;	// dict ID list
	private String code;	// language code
	
	protected String performAction() throws Exception {
		log.info("RemoveDictLanguageAction: id=" + id + ", dicts=" + dicts + ", code=" + code);
		if (id != null) {
			dictionaryService.removeDictionaryLanguage(toIdList(id));
		} else {
			dictionaryService.removeDictionaryLanguage(toIdList(dicts), code);
		}
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

	public String getDicts() {
		return dicts;
	}

	public void setDicts(String dicts) {
		this.dicts = dicts;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
	
	

}
