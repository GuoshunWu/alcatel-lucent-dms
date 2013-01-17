package com.alcatel_lucent.dms.action.app;

import com.alcatel_lucent.dms.action.JSONAction;
import com.alcatel_lucent.dms.service.DictionaryService;

@SuppressWarnings("serial")
public class UpdateDictLanguageAction extends JSONAction {

	private DictionaryService dictionaryService;
	
	private Long id;
	private String code;
	private Long languageId;
	private Long charsetId;
	
	protected String performAction() throws Exception {
		log.info("UpdateDictLanguageAction: id=" + id + ", code=" + code + ", languageId=" + languageId + ", charsetId=" + charsetId);
		dictionaryService.updateDictionaryLanguage(id, code, languageId, charsetId);
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

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getLanguageId() {
		return languageId;
	}

	public void setLanguageId(Long languageId) {
		this.languageId = languageId;
	}

	public Long getCharsetId() {
		return charsetId;
	}

	public void setCharsetId(Long charsetId) {
		this.charsetId = charsetId;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
}
