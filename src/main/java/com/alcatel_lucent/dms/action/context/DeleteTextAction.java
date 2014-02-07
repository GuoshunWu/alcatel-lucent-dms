package com.alcatel_lucent.dms.action.context;

import com.alcatel_lucent.dms.action.JSONAction;
import com.alcatel_lucent.dms.service.TextService;

@SuppressWarnings("serial")
public class DeleteTextAction extends JSONAction {
	
	private TextService textService;
	
	private Long id;	// text id

	@Override
	protected String performAction() throws Exception {
		log.info("[DeleteTextAction] id=" + id);
		textService.deleteText(id);
		setMessage(getText("message.success"));
		return SUCCESS;
	}

	public TextService getTextService() {
		return textService;
	}

	public void setTextService(TextService textService) {
		this.textService = textService;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

}
