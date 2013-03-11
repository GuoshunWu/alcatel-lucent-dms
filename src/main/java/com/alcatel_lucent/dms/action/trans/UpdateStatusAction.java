package com.alcatel_lucent.dms.action.trans;

import java.util.ArrayList;
import java.util.Collection;

import com.alcatel_lucent.dms.action.JSONAction;
import com.alcatel_lucent.dms.service.TextService;

@SuppressWarnings("serial")
public class UpdateStatusAction extends JSONAction {
	
	private TextService textService;
	private String ctid;
	private Integer transStatus;
	private String type;
	
	protected String performAction() throws Exception {
		log.info("UpdateStatusAction: ctid=" + ctid + ", status=" + transStatus + ", type=" + type);
		if (type.equals("trans")) {
			textService.updateTranslationStatus(toIdList(ctid), transStatus);
		} else if (type.equals("dict")) {
			textService.updateTranslationStatusByDict(toIdList(ctid), transStatus);
		} else if (type.equals("app")) {
			textService.updateTranslationStatusByApp(toIdList(ctid), transStatus);
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

	public String getCtid() {
		return ctid;
	}

	public void setCtid(String ctid) {
		this.ctid = ctid;
	}

}
