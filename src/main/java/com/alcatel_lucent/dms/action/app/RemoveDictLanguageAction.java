package com.alcatel_lucent.dms.action.app;

import java.util.ArrayList;

import antlr.collections.List;

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
			setMessage(getText("message.success"));
		} else {
			int count = dictionaryService.removeDictionaryLanguageInBatch(toIdList(dicts), code);
			ArrayList msgParam = new ArrayList();
			msgParam.add(count);
			setMessage(getText("removeDictLanguage.success", msgParam));
		}
		setStatus(0);
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
