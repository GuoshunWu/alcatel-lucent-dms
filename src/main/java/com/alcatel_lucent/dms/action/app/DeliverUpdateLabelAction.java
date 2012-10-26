package com.alcatel_lucent.dms.action.app;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.action.JSONAction;
import com.alcatel_lucent.dms.model.Context;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.Label;
import com.alcatel_lucent.dms.service.DaoService;
import com.alcatel_lucent.dms.service.DeliveringDictPool;

@SuppressWarnings("serial")
public class DeliverUpdateLabelAction extends JSONAction {
	
	private DeliveringDictPool deliveringDictPool;
	private DaoService daoService;
	
	private String handler;
	private Long dict;
	private Long id;
	private String maxLength;
	private String description;
	private String context;
	
	@Override
	protected String performAction() throws Exception {
		log.info("DeliverUpdateDictLanguageAction: handler=" + handler + ", dict=" + dict + ", id=" + id + 
				", maxLength=" + maxLength + ", context=" + context);
		try {
			Dictionary dictionary = deliveringDictPool.getDictionary(handler, dict);
			for (Label label : dictionary.getLabels()) {
				if (label.getId().equals(id)) {
		    		if (maxLength != null) {
		    			label.setMaxLength(maxLength);
		    		}
		    		if (description != null) {
		    			label.setDescription(description);
		    		}
		    		if (context != null) {
			    		Context ctx = new Context();
			    		ctx.setName(context);
		    			label.setContext(ctx);
		    		}
					
					break;
				}
			}
	    } catch (BusinessException e) {
			setMessage(e.toString());
			setStatus(-1);
		}
		return SUCCESS;
	}

	public String getHandler() {
		return handler;
	}

	public void setHandler(String handler) {
		this.handler = handler;
	}

	public DeliveringDictPool getDeliveringDictPool() {
		return deliveringDictPool;
	}

	public void setDeliveringDictPool(DeliveringDictPool deliveringDictPool) {
		this.deliveringDictPool = deliveringDictPool;
	}

	public Long getDict() {
		return dict;
	}

	public void setDict(Long dict) {
		this.dict = dict;
	}

	public DaoService getDaoService() {
		return daoService;
	}

	public void setDaoService(DaoService daoService) {
		this.daoService = daoService;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getMaxLength() {
		return maxLength;
	}

	public void setMaxLength(String maxLength) {
		this.maxLength = maxLength;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}
}
