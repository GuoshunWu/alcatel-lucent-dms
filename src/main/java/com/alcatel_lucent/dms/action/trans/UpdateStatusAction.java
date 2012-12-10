package com.alcatel_lucent.dms.action.trans;

import java.util.ArrayList;
import java.util.Collection;

import com.alcatel_lucent.dms.action.JSONAction;
import com.alcatel_lucent.dms.service.TextService;

@SuppressWarnings("serial")
public class UpdateStatusAction extends JSONAction {
	
	private TextService textService;
	private String id;
	private Integer transStatus;
	private String type;
	
	protected String performAction() throws Exception {
		log.info("UpdateStatusAction: id=" + id + ", status=" + transStatus + ", type=" + type);
		if (type.equals("trans")) {
			textService.updateTranslationStatus(toIdList(id), transStatus);
		} else if (type.equals("dict")) {
			textService.updateTranslationStatusByDict(toIdList(id), transStatus);
		} else if (type.equals("app")) {
			textService.updateTranslationStatusByApp(toIdList(id), transStatus);
		} else {
			setStatus(-1);
			return SUCCESS;
		}
		setStatus(0);
		return SUCCESS;
	}
	
	public TextService getTextService() {
		return textService;
	}

	public void setTextService(TextService textService) {
		this.textService = textService;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Integer getTransStatus() {
		return transStatus;
	}

	public void setTransStatus(Integer transStatus) {
		this.transStatus = transStatus;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
