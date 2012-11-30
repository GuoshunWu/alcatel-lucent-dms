package com.alcatel_lucent.dms.action.app;

import java.util.Collection;

import com.alcatel_lucent.dms.action.JSONAction;
import com.alcatel_lucent.dms.service.DictionaryService;

@SuppressWarnings("serial")
public class AddDictLanguageAction extends JSONAction {
	
	private DictionaryService dictionaryService;
	
	private String dicts;
	private String code;
	private Long languageId;
	private Long charsetId;

	protected String performAction() throws Exception {
		log.info("AddDictLanguageAction: dicts=" + dicts + ", languageId=" + languageId + ", code=" + code + ", charsetId=" + charsetId);
		Collection<Long> idList = toIdList(dicts);
		for (Long dictId : idList) {
			dictionaryService.addLanguage(dictId, code, languageId, charsetId);
		}
		setStatus(0);
		setMessage(getText("message.success"));
		return SUCCESS;
	}

	public String getDicts() {
		return dicts;
	}
	public void setDicts(String dicts) {
		this.dicts = dicts;
	}
	public Long getLanguageId() {
		return languageId;
	}
	public void setLanguageId(Long languageId) {
		this.languageId = languageId;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public DictionaryService getDictionaryService() {
		return dictionaryService;
	}
	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	public Long getCharsetId() {
		return charsetId;
	}

	public void setCharsetId(Long charsetId) {
		this.charsetId = charsetId;
	}
	
}
