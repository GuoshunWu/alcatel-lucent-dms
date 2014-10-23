package com.alcatel_lucent.dms.action.app;

import com.alcatel_lucent.dms.action.JSONAction;
import com.alcatel_lucent.dms.service.TextService;

public class UpdateLabelStatusAction extends JSONAction {
	
	private TextService textService;
	private String id;	// label id
	private Integer transStatus;	// translation status

	@Override
	protected String performAction() throws Exception {
		textService.updateTranslationStatusByLabel(toIdList(id), transStatus);
		setStatus(0);
		return SUCCESS;
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

	public TextService getTextService() {
		return textService;
	}

	public void setTextService(TextService textService) {
		this.textService = textService;
	}

}
