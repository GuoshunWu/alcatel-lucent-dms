package com.alcatel_lucent.dms.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.alcatel_lucent.dms.SpringContext;
import com.alcatel_lucent.dms.service.DictionaryService;

public class Application extends BaseEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7168527218137875020L;

    private ApplicationBase base;
    private String version;

	private Collection<Dictionary> dictionaries;

    public ApplicationBase getBase() {
        return base;
    }

    public String getName(){
        return base.getName();
    }
    
    public Integer getDictNum(){
        return dictionaries==null?0:dictionaries.size();
    }
    
    @SuppressWarnings("unchecked")
	public Collection getCell(){
        return Arrays.asList(getId(),getName(),version, getDictNum());
    }

    public void setBase(ApplicationBase base) {
        this.base = base;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }


	public Collection<Dictionary> getDictionaries() {
		return dictionaries;
	}

	public void setDictionaries(Collection<Dictionary> dictionaries) {
		this.dictionaries = dictionaries;
	}

	public void removeDictionary(Long dictId) {
		if (dictionaries != null && dictId != null) {
			for (Iterator<Dictionary> iterator = dictionaries.iterator(); iterator.hasNext(); ) {
				Dictionary dict = iterator.next();
				if (dict.getId().equals(dictId)) {
					iterator.remove();
				}
			}
		}
	}
	
	public int getLabelNum() {
		DictionaryService dictService = (DictionaryService) SpringContext.getBean("dictionaryService");
		return dictService.getLabelNumByApp(getId());
	}
	
    private Map<String, int[]> summaryCache;
    
    /**
     * Get translation status summary by language, used by front
     * @return
     */
    public Map<String, int[]> getS() {
    	return summaryCache;
    }
    
    public void setS(Map<Long, int[]> summary) {
		this.summaryCache = new HashMap<String, int[]>();
		if (summary == null) return;
		for (Long langId : summary.keySet()) {
			summaryCache.put(langId.toString(), summary.get(langId));
		}
    }
    
}
