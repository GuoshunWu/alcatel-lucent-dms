package com.alcatel_lucent.dms.action.app;

import org.apache.struts2.convention.annotation.Result;

import com.alcatel_lucent.dms.action.JSONAction;
import com.alcatel_lucent.dms.service.DictionaryService;

@SuppressWarnings("serial")
@Result(type = "json", params = {"noCache", "true", "ignoreHierarchy", "false", "includeProperties", "id,permanent,message,status"})
public class RemoveDictAction extends JSONAction {
	
	private DictionaryService dictionaryService;
	
    private boolean permanent;
    private String id;
    private Long appId;

    public String performAction() throws Exception {
    	log.info("RemoveDictAction: id=" + id + ", appId=" + appId + ", permanent=" + permanent);
    	if (permanent) {
    		dictionaryService.deleteDictionary(toIdList(id));
    	} else {
    		dictionaryService.removeDictionaryFromApplication(appId, toIdList(id));
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

	public Long getAppId() {
		return appId;
	}

	public void setAppId(Long appId) {
		this.appId = appId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public boolean isPermanent() {
		return permanent;
	}

	public void setPermanent(boolean permanent) {
		this.permanent = permanent;
	}
}
