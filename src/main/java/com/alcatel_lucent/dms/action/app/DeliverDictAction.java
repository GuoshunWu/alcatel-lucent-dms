package com.alcatel_lucent.dms.action.app;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.BusinessWarning;
import com.alcatel_lucent.dms.Constants;
import com.alcatel_lucent.dms.action.ProgressAction;
import com.alcatel_lucent.dms.action.ProgressQueue;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.DictionaryLanguage;
import com.alcatel_lucent.dms.service.DeliveringDictPool;
import com.alcatel_lucent.dms.service.DeliveryReport;
import com.alcatel_lucent.dms.service.DictionaryService;

@SuppressWarnings("serial")
public class DeliverDictAction extends ProgressAction {
	
	private String handler;
	private Long app;
	
	private DeliveringDictPool deliveringDictPool;
	private DictionaryService dictionaryService;
	
	private DeliveryReport report;

	@Override
	protected String performAction() throws Exception {
		try {
			Collection<Dictionary> dictList = deliveringDictPool.getDictionaries(handler);
			report = importDictionaries(app, dictList, Constants.DELIVERY_MODE);
			setMessage(report.toHTML());
			deliveringDictPool.removeHandler(handler);
		} catch (BusinessException e) {
			setMessage(e.toString());
			setStatus(-1);
		}
		return SUCCESS;
	}

    private DeliveryReport importDictionaries(Long appId, Collection<Dictionary> dictList, int mode) throws BusinessException {
        DeliveryReport report = new DeliveryReport();
        Map<String, Collection<BusinessWarning>> warningMap = new TreeMap<String, Collection<BusinessWarning>>();
        int total = dictList.size();
        int cur = 1;
        for (Dictionary dict : dictList) {
            Map<String, String> langCharset = new HashMap<String, String>();
            if (dict.getDictLanguages() != null) {
                for (DictionaryLanguage dl : dict.getDictLanguages()) {
                    langCharset.put(dl.getLanguageCode(), dl.getCharset().getName());
                }
            }
            Collection<BusinessWarning> warnings = new ArrayList<BusinessWarning>();
            ProgressQueue.setProgress("[" + cur + "/" + total + "] Importing " + dict.getName(), 0);
            dictionaryService.importDictionary(appId, dict, dict.getVersion(), mode, null, langCharset, warnings, report);
            warningMap.put(dict.getName(), warnings);
            cur++;
        }
        report.setWarningMap(warningMap);
        log.info(report.toString());
        return report;
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
