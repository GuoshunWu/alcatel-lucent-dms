package com.alcatel_lucent.dms.action.app;

import com.alcatel_lucent.dms.action.JSONAction;
import com.alcatel_lucent.dms.service.DictionaryService;

@SuppressWarnings("serial")
public class AddLabelAction extends JSONAction {
	
	private DictionaryService dictionaryService;
	
	private Long dict;
	private String key;
	private String reference;
	private String maxLength;
	private String context;
	private String description;

	@Override
	protected String performAction() throws Exception {
		log.info("AddLabelAction: dict=" + dict + ", key=" + key + ", reference=" + reference + 
				", maxLength=" + maxLength + ", context=" + context + ", description=" + description);
		dictionaryService.addLabel(dict, key, reference, maxLength, context, description);
		setMessage(getText("operation.success"));
		return SUCCESS;
	}

	public DictionaryService getDictionaryService() {
		return dictionaryService;
	}

	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public String getMaxLength() {
		return maxLength;
	}

	public void setMaxLength(String maxLength) {
		this.maxLength = maxLength;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Long getDict() {
		return dict;
	}

	public void setDict(Long dict) {
		this.dict = dict;
	}

}
