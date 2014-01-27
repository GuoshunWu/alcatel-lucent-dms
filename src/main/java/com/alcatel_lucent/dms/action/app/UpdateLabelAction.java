package com.alcatel_lucent.dms.action.app;

import com.alcatel_lucent.dms.action.JSONAction;
import com.alcatel_lucent.dms.service.DictionaryService;

@SuppressWarnings("serial")
public class UpdateLabelAction extends JSONAction {
	
	private DictionaryService dictionaryService;
	
	private String id;
	private String key;
	private String reference;
	private String maxLength;
	private String description;
	private String context;
	
	protected String performAction() throws Exception {
		log.info("UpdateLabelAction: id=" + id + ", maxLength=" + maxLength + ", description=" + description + ", context=" + context);
		if (key != null) {
			dictionaryService.updateLabelKey(Long.valueOf(id), key);
		} else if (reference != null) {
			dictionaryService.updateLabelReference(Long.valueOf(id), reference);
		} else {
			dictionaryService.updateLabels(toIdList(id), maxLength, description, context);
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
	public String getMaxLength() {
		return maxLength;
	}
	public void setMaxLength(String maxLength) {
		this.maxLength = maxLength;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getContext() {
		return context;
	}
	public void setContext(String context) {
		this.context = context;
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

}
