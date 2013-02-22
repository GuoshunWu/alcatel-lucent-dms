package com.alcatel_lucent.dms.action.app;

import java.util.Collection;
import java.util.Map;

import org.apache.struts2.convention.annotation.Result;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.BusinessWarning;
import com.alcatel_lucent.dms.Constants;
import com.alcatel_lucent.dms.action.JSONAction;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.service.DeliveringDictPool;
import com.alcatel_lucent.dms.service.DeliveryReport;
import com.alcatel_lucent.dms.service.DictionaryService;

@SuppressWarnings("serial")
@Result(type="json", params={"noCache","true","ignoreHierarchy","false","includeProperties","status,message,warnings"})
public class DeliverDictAction extends JSONAction {
	
	private String handler;
	private Long app;
	
	private DeliveringDictPool deliveringDictPool;
	private DictionaryService dictionaryService;
	
	private DeliveryReport report;

	@Override
	protected String performAction() throws Exception {
		try {
			Collection<Dictionary> dictList = deliveringDictPool.getDictionaries(handler);
			report = dictionaryService.importDictionaries(app, dictList, Constants.DELIVERY_MODE);
			setMessage(report.toHTML());
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

	public Long getApp() {
		return app;
	}

	public void setApp(Long app) {
		this.app = app;
	}

	public DictionaryService getDictionaryService() {
		return dictionaryService;
	}

	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	public DeliveringDictPool getDeliveringDictPool() {
		return deliveringDictPool;
	}

	public void setDeliveringDictPool(DeliveringDictPool deliveringDictPool) {
		this.deliveringDictPool = deliveringDictPool;
	}

	public DeliveryReport getReport() {
		return report;
	}

	public void setReport(DeliveryReport report) {
		this.report = report;
	}

}
