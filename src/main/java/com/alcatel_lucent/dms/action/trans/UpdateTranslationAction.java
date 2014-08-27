package com.alcatel_lucent.dms.action.trans;

import java.util.Collection;

import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;

import com.alcatel_lucent.dms.action.JSONAction;
import com.alcatel_lucent.dms.service.TextService;

@ParentPackage("dms-json")
@Result(type="json", params={"noCache","true","ignoreHierarchy","false","includeProperties","status,message,id,ctid,translation,dicts.*"})
@SuppressWarnings("serial")
public class UpdateTranslationAction extends JSONAction {
	
	private Long id;	// label id
	private Long ctid;	// translation id
	private String translation;
	private Boolean confirm;	// confirm if apply the change to other dictionaries
	private String[] dicts;	// other dictionaries in which the translation is also referred to
	
	private TextService textService;
	
	@Override
	protected String performAction() throws Exception {
		log.info("UpdateTranslationAction: id=" + id + ", ctid=" + ctid + ", translation=" + translation + ", confirm=" + confirm);
		/*
		Collection<String> dictList = textService.updateTranslation(id, ctid, translation, confirm);
		// check if need confirm
		// return status=1 if confirmation is needed
		if (!dictList.isEmpty() && confirm == null) {
			dicts = dictList.toArray(new String[0]);
			setStatus(1);
			return SUCCESS;
		} else {
			setMessage(getText("message.success"));
			return SUCCESS;
		}
		*/
		
		// always DO NOT change translation in other dictionary
		textService.updateTranslation(id, ctid, translation, false);
		setMessage(getText("message.success"));
		return SUCCESS;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTranslation() {
		return translation;
	}

	public void setTranslation(String translation) {
		this.translation = translation;
	}

	public Long getCtid() {
		return ctid;
	}

	public void setCtid(Long ctid) {
		this.ctid = ctid;
	}

	public Boolean getConfirm() {
		return confirm;
	}

	public void setConfirm(Boolean confirm) {
		this.confirm = confirm;
	}

	public String[] getDicts() {
		return dicts;
	}

	public void setDicts(String[] dicts) {
		this.dicts = dicts;
	}

	public TextService getTextService() {
		return textService;
	}

	public void setTextService(TextService textService) {
		this.textService = textService;
	}
	
	

}
