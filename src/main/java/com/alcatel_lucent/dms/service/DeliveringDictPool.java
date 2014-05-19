package com.alcatel_lucent.dms.service;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.model.Dictionary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
@Service("deliveringDictPool")
public class DeliveringDictPool {
	
	private static Logger log = LoggerFactory.getLogger(DeliveringDictPool.class);
	private static final long TIMEOUT_VALUE = 60 * 60 * 1000;
	
	@Autowired
	private DictionaryService dictionaryService;
	
    @Value("${dms.deliver.dir}")
	private String baseDir;
	
	private Map<String, Date> lifeMap = new HashMap<String, Date>();
	private Map<String, Collection<Dictionary>> dictMap = new HashMap<String, Collection<Dictionary>>();
//	private Map<String, Collection<BusinessWarning>> warningMap = new HashMap<String, Collection<BusinessWarning>>();
	
	
	public void addHandler(String handler, Long appId) throws BusinessException {
		log.info("Add handler '" + handler + "' to pool.");
		checkTimeout();
		String dir = baseDir + "/" + handler;
		Collection<Dictionary> dictList = dictionaryService.previewDictionaries(dir, new File(dir), appId);

		synchronized (lifeMap) {
			dictMap.put(handler, dictList);
//			warningMap.put(handler, warnings);
			lifeMap.put(handler, new Date());
		}
	}
	
	public void removeHandler(String handler) {
		synchronized (lifeMap) {
			dictMap.remove(handler);
			lifeMap.remove(handler);
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
/*	
	public Collection<BusinessWarning> getWarnings(String handler) {
		synchronized (lifeMap) {
			if (warningMap.containsKey(handler)) {
				return warningMap.get(handler);
			} else {
				throw new BusinessException(BusinessException.DELIVERY_TIMEOUT);
			}
		}
	}
	
*/	
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
			Collection<String> keysToRemove = new ArrayList<String>();
			for (String key : lifeMap.keySet()) {
				Date createTime = lifeMap.get(key);
				if (System.currentTimeMillis() - createTime.getTime() > TIMEOUT_VALUE) {
					log.info("Handler '" + key + "' timed out.");
					keysToRemove.add(key);
				}
			}
			for (String key : keysToRemove) {
				lifeMap.remove(key);
				dictMap.remove(key);
//				warningMap.remove(key);
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
