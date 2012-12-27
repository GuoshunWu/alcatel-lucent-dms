package com.alcatel_lucent.dms.action.trans;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;

import com.alcatel_lucent.dms.SystemError;
import com.alcatel_lucent.dms.action.BaseAction;
import com.alcatel_lucent.dms.service.TranslationService;

@SuppressWarnings("serial")
public class ExportTranslationReportAction extends BaseAction {

	private TranslationService translationService;
	private Long prod;
	private String type;
	private String language;
	private InputStream inputStream;
	
	@Override
	@Action(results = {
            @Result(name = SUCCESS, type = "stream", params={"contentType","application/vnd.ms-excel","contentDisposition","attachment;filename=translation_report.xls"})
    })
	public String execute() {
		try {
			ByteArrayOutputStream output = new ByteArrayOutputStream(); 
			if (type != null && type.equals("app")) {
				translationService.generateAppTranslationReport(prod, toIdList(language), output);
			} else {
				translationService.generateDictTranslationReport(prod, toIdList(language), output);
			}
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
	public Long getProd() {
		return prod;
	}
	public void setProd(Long prod) {
		this.prod = prod;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}
}
