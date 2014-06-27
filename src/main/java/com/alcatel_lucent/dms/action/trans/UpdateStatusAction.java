package com.alcatel_lucent.dms.action.trans;

import com.alcatel_lucent.dms.action.JSONAction;
import com.alcatel_lucent.dms.service.TextService;

@SuppressWarnings("serial")
public class UpdateStatusAction extends JSONAction {
	
	private TextService textService;
	private String id;	// dict id or app id
	private String ctid;	// translation id
	private String lang;	// language id (optional, only available when type is 'dict' or 'app')
	private Integer transStatus;
	private String type;
	
	protected String performAction() throws Exception {
		log.info("UpdateStatusAction: id=" + id + ", ctid=" + ctid + ", status=" + transStatus + ", type=" + type + ", lang=" + lang);
		if (type.equals("trans")) {
			textService.updateTranslationStatus(toIdList(ctid), transStatus);
		} else if (type.equals("dict")) {
			textService.updateTranslationStatusByDict(toIdList(id), lang == null ? null : toIdList(lang), transStatus);
		} else if (type.equals("app")) {
			textService.updateTranslationStatusByApp(toIdList(id), lang == null ? null : toIdList(lang), transStatus);
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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

}
