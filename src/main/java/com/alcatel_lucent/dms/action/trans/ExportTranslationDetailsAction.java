package com.alcatel_lucent.dms.action.trans;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;

import com.alcatel_lucent.dms.SystemError;
import com.alcatel_lucent.dms.action.BaseAction;
import com.alcatel_lucent.dms.service.TranslationService;

@SuppressWarnings("serial")
public class ExportTranslationDetailsAction extends BaseAction {
	
	private TranslationService translationService;
	private InputStream inputStream;
	
	private String dict;
	private String lang;
	
	@Override
	@Action(results = {
            @Result(name = SUCCESS, type = "stream", params={"contentType","application/vnd.ms-excel","contentDisposition","attachment;filename=translation_details.xls"})
    })
	public String execute() {
		try {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			translationService.exportTranslations(toIdList(dict), toIdList(lang), output);
			inputStream = new ByteArrayInputStream(output.toByteArray());
			return SUCCESS;
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.toString());
			throw new SystemError(e);
		}
	}
	
	public TranslationService getTranslationService() {
		return translationService;
	}
	public void setTranslationService(TranslationService translationService) {
		this.translationService = translationService;
	}

	public String getDict() {
		return dict;
	}

	public void setDict(String dict) {
		this.dict = dict;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}

}
