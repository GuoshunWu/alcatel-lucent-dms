package com.alcatel_lucent.dms.action.app;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.action.JSONAction;
import com.alcatel_lucent.dms.service.DictionaryService;

@SuppressWarnings("serial")
public class UpdateDictAction extends JSONAction {
	
	private DictionaryService dictionaryService;
	
	private Long id;
	private String format;
	private String encoding;
	
	@Override
	protected String performAction() throws Exception {
		log.info("UpdateDictAction: id=" + id + ", format=" + format + ", encoding=" + encoding);
		try {
			if (format != null && !format.trim().isEmpty()) {
				dictionaryService.updateDictionaryFormat(id, format);
			}
			if (encoding != null && !encoding.trim().isEmpty()) {
				dictionaryService.updateDictionaryEncoding(id, encoding);
			}
		} catch (BusinessException e) {
			setMessage(e.toString());
			setStatus(-1);
		}
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

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}
}
