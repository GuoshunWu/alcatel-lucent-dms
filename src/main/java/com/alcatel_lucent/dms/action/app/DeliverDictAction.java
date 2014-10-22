package com.alcatel_lucent.dms.action.app;

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
import com.alcatel_lucent.dms.service.ImportSettings;
import com.alcatel_lucent.dms.service.JSONService;

import java.util.*;

import org.springframework.transaction.UnexpectedRollbackException;

@SuppressWarnings("serial")
public class DeliverDictAction extends ProgressAction {
	
	private String handler;
	private Long app;
	private Boolean autoCreateLang;
	private Boolean removeOldLabels;
	
	private DeliveringDictPool deliveringDictPool;
	private DictionaryService dictionaryService;
	private JSONService jsonService;
	
	private DeliveryReport report;

	@Override
	protected String performAction() throws Exception {
		try {
			Collection<Dictionary> dictList = deliveringDictPool.getDictionaries(handler);
			report = importDictionaries(app, dictList, Constants.ImportingMode.DELIVERY);
			String json = jsonService.toJSONString(report, 
					"dictNum,labelNum,diffLabelNum,diffTranslationNum,diffTranslatedNum,translationNum,translationWC" +
					",distinctTranslationNum,distinctTranslationWC" +
					",untranslatedNum,untranslatedWC,translatedNum,translatedWC" +
					",matchedNum,matchedWC");
			setMessage(json);
			deliveringDictPool.removeHandler(handler);
		} catch (BusinessException e) {
			setMessage(e.toString());
			setStatus(-1);
		}
		return SUCCESS;
	}

    private DeliveryReport importDictionaries(Long appId, Collection<Dictionary> dictList, Constants.ImportingMode mode) throws BusinessException {
        DeliveryReport report = new DeliveryReport();
        int total = dictList.size();
        int cur = 1;
        Iterator<Dictionary> dictionaryIterator = dictList.iterator();
        while(dictionaryIterator.hasNext()){
            Dictionary dict = dictionaryIterator.next();
            // remote from the collection to free memory
            dictionaryIterator.remove();

            Map<String, String> langCharset = new HashMap<String, String>();
            if (dict.getDictLanguages() != null) {
                for (DictionaryLanguage dl : dict.getDictLanguages()) {
                    langCharset.put(dl.getLanguageCode(), dl.getCharset().getName());
                }
            }
            Collection<BusinessWarning> warnings = new ArrayList<BusinessWarning>();
            ProgressQueue.setProgress("[" + cur + "/" + total + "] Importing " + dict.getName(), 0);
            try {
            	ImportSettings settings = new ImportSettings();
            	if (autoCreateLang != null) {
            		settings.setAutoCreateLang(autoCreateLang);
            	}
            	if (removeOldLabels != null) {
            		settings.setRemoveOldLabels(removeOldLabels);
            	}
            	dictionaryService.importDictionary(appId, dict, dict.getVersion(), mode, null, langCharset, settings, warnings, report);
            } catch (UnexpectedRollbackException e) {
            	if (mode == Constants.ImportingMode.TEST) {
            		log.info("Rolled back all changes of importing because of TEST mode");
            	} else {
            		throw e;
            	}
            }
            cur++;
        }
//        report.setWarningMap(warningMap);
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

	public JSONService getJsonService() {
		return jsonService;
	}

	public void setJsonService(JSONService jsonService) {
		this.jsonService = jsonService;
	}

	public Boolean getAutoCreateLang() {
		return autoCreateLang;
	}

	public void setAutoCreateLang(Boolean autoCreateLang) {
		this.autoCreateLang = autoCreateLang;
	}

	public Boolean getRemoveOldLabels() {
		return removeOldLabels;
	}

	public void setRemoveOldLabels(Boolean removeOldLabels) {
		this.removeOldLabels = removeOldLabels;
	}

}
