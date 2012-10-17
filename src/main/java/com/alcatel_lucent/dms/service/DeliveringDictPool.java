package com.alcatel_lucent.dms.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.BusinessWarning;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.DictionaryLanguage;
import com.alcatel_lucent.dms.model.Label;

public class DeliveringDictPool {
	
	private static Logger log = Logger.getLogger(DeliveringDictPool.class);
	private static final long TIMEOUT_VALUE = 60 * 60 * 1000;
	
	@Autowired
	private DictionaryService dictionaryService;
	
	private String baseDir;
	
	private Map<String, Date> lifeMap = new HashMap<String, Date>();
	private Map<String, Collection<Dictionary>> dictMap = new HashMap<String, Collection<Dictionary>>();
	private Map<String, Collection<BusinessWarning>> warningMap = new HashMap<String, Collection<BusinessWarning>>();
	
	
	public void addHandler(String handler) throws BusinessException {
		log.info("Add handler '" + handler + "' to pool.");
		checkTimeout();
		String dir = baseDir + "/" + handler;
		Collection<BusinessWarning> warnings = new ArrayList<BusinessWarning>();
		Collection<Dictionary> dictList = dictionaryService.previewDictionaries(dir, new File(dir), warnings);
		
		// add fake id for preview process
		long dictFid = 1;
		for (Dictionary dict : dictList) {
			dict.setId(dictFid++);
			if (dict.getDictLanguages() != null) {
				long dlFid = 1;
				for (DictionaryLanguage dl : dict.getDictLanguages()) {
					dl.setId(dlFid++);
				}
			}
			if (dict.getLabels() != null) {
				long labelFid = 1;
				for (Label label : dict.getLabels()) {
					label.setId(labelFid++);
				}
			}
		}
		
		synchronized (lifeMap) {
			dictMap.put(handler, dictList);
			warningMap.put(handler, warnings);
			lifeMap.put(handler, new Date());
		}
	}
	
	public Collection<Dictionary> getDictionaries(String handler) throws BusinessException {
		synchronized (lifeMap) {
			if (dictMap.containsKey(handler)) {
				return dictMap.get(handler);
			} else {
				throw new BusinessException(BusinessException.DELIVERY_TIMEOUT);
			}
		}
	}
	
	public Collection<BusinessWarning> getWarnings(String handler) {
		synchronized (lifeMap) {
			if (warningMap.containsKey(handler)) {
				return warningMap.get(handler);
			} else {
				throw new BusinessException(BusinessException.DELIVERY_TIMEOUT);
			}
		}
	}
	
	public Dictionary getDictionary(String handler, Long dictFid) throws BusinessException {
		Collection<Dictionary> dictList = getDictionaries(handler);
		for (Dictionary dict : dictList) {
			if (dict.getId().equals(dictFid)) {
				return dict;
			}
		}
		return null;
	}
	
	public void checkTimeout() {
		synchronized (lifeMap) {
			for (String key : lifeMap.keySet()) {
				Date createTime = lifeMap.get(key);
				if (System.currentTimeMillis() - createTime.getTime() > TIMEOUT_VALUE) {
					log.info("Handler '" + key + "' timed out.");
					lifeMap.remove(key);
					dictMap.remove(key);
					warningMap.remove(key);
				}
			}
		}
	}

	public String getBaseDir() {
		return baseDir;
	}

	public void setBaseDir(String baseDir) {
		this.baseDir = baseDir;
	}
}
