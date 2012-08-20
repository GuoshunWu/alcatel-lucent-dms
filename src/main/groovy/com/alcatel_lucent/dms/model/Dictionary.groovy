package com.alcatel_lucent.dms.model

/**
 * Created by IntelliJ IDEA.
 * User: guoshunw
 * Date: 12-8-16
 * Time: 下午2:59
 * To change this template use File | Settings | File Templates.
 */
class Dictionary extends BaseEntity{

    DictionaryBase base
    Application application
    String version
    boolean locked


    Collection<DictionaryLanguage> dictLanguages
    Collection<Label> labels

    public String getName(){
        return base.name
    }

    public void setName(String name){
        base.name=name
    }
    public String getFormat(){
        return base.format
    }

    public void setFormat(String format){
        base.format=format
    }

    public String getEncoding(){
        return base.encoding
    }

    public void setEncoding(String encoding){
        base.encoding=encoding
    }

    public String getPath(){
        return base.path
    }

    public void setPath(String path){
        base.path=path
    }


    public Language getLanguageByCode(String langCode) {
        if (dictLanguages != null) {
            for (DictionaryLanguage dl : dictLanguages) {
                if (dl.getLanguageCode().equals(langCode)) {
                    return dl.getLanguage()
                }
            }
        }
        return null
    }

    public String getLanguageCode(Long languageId) {
        if (dictLanguages != null) {
            for (DictionaryLanguage dl : dictLanguages) {
                if (dl.getLanguage().getId().equals(languageId)) {
                    return dl.getLanguageCode()
                }
            }
        }
        return null
    }

    public HashSet<String> getAllLanguageCodes() {
        HashSet<String> result = new HashSet<String>()
        if (dictLanguages != null) {
            for (DictionaryLanguage dl : dictLanguages) {
                result.add(dl.getLanguageCode())
            }
        }
        return result
    }

    public ArrayList<Language> getAllLanguages() {
        ArrayList<Language> result = new ArrayList<Language>()
        if (dictLanguages != null) {
            for (DictionaryLanguage dl : dictLanguages) {
                result.add(dl.getLanguage())
            }
        }
        return result
    }

    public Map<Long, String> getLangCodeMap() {
        Map<Long, String> result = new HashMap<Long, String>()
        if (dictLanguages != null) {
            for (DictionaryLanguage dl : dictLanguages) {
                result.put(dl.getLanguage().getId(), dl.getLanguageCode())
            }
        }
        return result
    }

    public DictionaryLanguage getDictLanguage(Long languageId) {
        if (dictLanguages != null) {
            for (DictionaryLanguage dl : dictLanguages) {
                if (dl.getLanguage().getId().equals(languageId)) {
                    return dl
                }
            }
        }
        return null
    }

    public DictionaryLanguage getDictLanguage(String languageCode) {
        if (dictLanguages != null) {
            for (DictionaryLanguage dl : dictLanguages) {
                if (dl.getLanguageCode().equals(languageCode)) {
                    return dl
                }
            }
        }
        return null
    }


    public Label getLabel(String key) {
        if (labels != null) {
            for (Label label : labels) {
                if (label.getKey().equals(key)) {
                    return label
                }
            }
        }
        return null
    }
}
