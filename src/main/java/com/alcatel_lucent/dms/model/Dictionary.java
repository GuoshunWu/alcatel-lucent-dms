package com.alcatel_lucent.dms.model;

import java.util.*;

public class Dictionary extends BaseEntity {

    private static final long serialVersionUID = 4926531636839152201L;

    private Collection<DictionaryLanguage> dictLanguages;
    private Collection<Label> labels;
    private boolean locked;
    private DictionaryBase base;
    private String version;


    public String getName(){
        return base.getName();
    }

    public void setName(String name){
        base.setName(name);
    }
    public String getFormat(){
        return base.getFormat();
    }

    public void setFormat(String format){
        base.setFormat(format);
    }

    public String getEncoding(){
        return base.getEncoding();
    }

    public void setEncoding(String encoding){
        base.setEncoding(encoding);
    }

    public String getPath(){
        return base.getPath();
    }

    public void setPath(String path){
        base.setPath(path);
    }

    public DictionaryBase getBase() {
        return base;
    }

    public int getLabelNum(){
        return labels==null?0:labels.size();
    }

    public void setBase(DictionaryBase base) {
        this.base = base;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Dictionary() {
        super();
    }


    public Collection<DictionaryLanguage> getDictLanguages() {
        return dictLanguages;
    }

    public void setDictLanguages(Collection<DictionaryLanguage> dictLanguages) {
        this.dictLanguages = dictLanguages;
    }

    public Collection<Label> getLabels() {
        return labels;
    }

    public void setLabels(Collection<Label> labels) {
        this.labels = labels;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public Language getLanguageByCode(String langCode) {
        if (dictLanguages != null) {
            for (DictionaryLanguage dl : dictLanguages) {
                if (dl.getLanguageCode().equals(langCode)) {
                    return dl.getLanguage();
                }
            }
        }
        return null;
    }

    public String getLanguageCode(Long languageId) {
        if (dictLanguages != null) {
            for (DictionaryLanguage dl : dictLanguages) {
                if (dl.getLanguage().getId().equals(languageId)) {
                    return dl.getLanguageCode();
                }
            }
        }
        return null;
    }

    public HashSet<String> getAllLanguageCodes() {
        HashSet<String> result = new HashSet<String>();
        if (dictLanguages != null) {
            for (DictionaryLanguage dl : dictLanguages) {
                result.add(dl.getLanguageCode());
            }
        }
        return result;
    }
    
    public ArrayList<String> getAllLanguageCodesOrdered() {
    	 ArrayList<String> result = new ArrayList<String>();
        if (dictLanguages != null) {
            for (DictionaryLanguage dl : dictLanguages) {
                result.add(dl.getLanguageCode());
            }
        }
        return result;
    }


    public ArrayList<Language> getAllLanguages() {
        ArrayList<Language> result = new ArrayList<Language>();
        if (dictLanguages != null) {
            for (DictionaryLanguage dl : dictLanguages) {
                result.add(dl.getLanguage());
            }
        }
        return result;
    }

    public Map<Long, String> getLangCodeMap() {
        Map<Long, String> result = new HashMap<Long, String>();
        if (dictLanguages != null) {
            for (DictionaryLanguage dl : dictLanguages) {
                result.put(dl.getLanguage().getId(), dl.getLanguageCode());
            }
        }
        return result;
    }

    public DictionaryLanguage getDictLanguage(Long languageId) {
        if (dictLanguages != null) {
            for (DictionaryLanguage dl : dictLanguages) {
                if (dl.getLanguage().getId().equals(languageId)) {
                    return dl;
                }
            }
        }
        return null;
    }

    public DictionaryLanguage getDictLanguage(String languageCode) {
        if (dictLanguages != null) {
            for (DictionaryLanguage dl : dictLanguages) {
                if (dl.getLanguageCode().equals(languageCode)) {
                    return dl;
                }
            }
        }
        return null;
    }


    public Label getLabel(String key) {
        if (labels != null) {
            for (Label label : labels) {
                if (label.getKey().equals(key)) {
                    return label;
                }
            }
        }
        return null;
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
    
    private Application app;	// transient variable for REST service
    public void setApp(Application app) {
    	this.app = app;
    }
    
    public Application getApp() {
    	return app;
    }

}
