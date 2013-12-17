package com.alcatel_lucent.dms.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.Constants;
import com.alcatel_lucent.dms.model.AlcatelLanguageCode;
import com.alcatel_lucent.dms.model.Charset;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.ISOLanguageCode;
import com.alcatel_lucent.dms.model.Language;

@Service(value = "languageService")
@SuppressWarnings("unchecked")
public class LanguageServiceImpl extends BaseServiceImpl implements LanguageService {

    // base data for dms
    private static Map<String, AlcatelLanguageCode> alcatelLanguageCodes = null;
    private static Map<String, ISOLanguageCode> isoLanguageCodes = null;
    private static Map<String, ISOLanguageCode> isoLanguageCodesUpperCase = null;
    private static Map<String, Charset> charsets = null;
    private static Map<Long, Language> languages = null;

    public Map<String, ISOLanguageCode> getISOLanguageCodes() {
        if (null != isoLanguageCodes)
            return isoLanguageCodes;

        isoLanguageCodes = new HashMap<String, ISOLanguageCode>();
        isoLanguageCodesUpperCase = new HashMap<String, ISOLanguageCode>();
        List<ISOLanguageCode> isoLangCodes = dao
                .retrieve("from ISOLanguageCode");
        for (ISOLanguageCode isoLangCode : isoLangCodes) {
            isoLanguageCodes.put(isoLangCode.getCode(), isoLangCode);
            isoLanguageCodesUpperCase.put(isoLangCode.getCode().toUpperCase(), isoLangCode);
            isoLangCode.getLanguage().getDefaultCharset();
        }
        return isoLanguageCodes;
    }

    public Map<String, AlcatelLanguageCode> getAlcatelLanguageCodes() {
        if (null != alcatelLanguageCodes)
            return alcatelLanguageCodes;

        alcatelLanguageCodes = new HashMap<String, AlcatelLanguageCode>();
        List<AlcatelLanguageCode> alLangCodes = dao
                .retrieve("from AlcatelLanguageCode");
        for (AlcatelLanguageCode alLangCode : alLangCodes) {
            alcatelLanguageCodes.put(alLangCode.getCode(), alLangCode);
            alLangCode.getLanguage().getDefaultCharset();
        }
        return alcatelLanguageCodes;
    }

    public Map<String, Charset> getCharsets() {
        if (null != charsets)
            return charsets;

        charsets = new HashMap<String, Charset>();
        List<Charset> sets = dao.retrieve("from Charset");
        for (Charset charset : sets) {
            charsets.put(charset.getName(), charset);
        }
        return charsets;
    }

    public Charset getCharset(String name) {
        return getCharsets().get(name);
    }

    public AlcatelLanguageCode getAlcatelLanguageCode(String code) {
        return getAlcatelLanguageCodes().get(code);
    }

    public ISOLanguageCode getISOLanguageCode(String code) {
        getISOLanguageCodes();
        return isoLanguageCodesUpperCase.get(code.replace(
                '_', '-').toUpperCase());
    }

    public Map<Long, Language> getLanguages() {
        if (null != languages)
            return languages;

        languages = new HashMap<Long, Language>();
        List<Language> langs = dao.retrieve("from Language");
        for (Language language : langs) {
            languages.put(language.getId(), language);
        }
        return languages;
    }

    public Language getLanguage(String languageCode) {
        // query alcatelLanguageCode table to find the related Language
        ISOLanguageCode isoCode = null;
        AlcatelLanguageCode alCode = getAlcatelLanguageCode(languageCode);
        if (null != alCode) return alCode.getLanguage();

        isoCode = getISOLanguageCode(languageCode);
        if (null != isoCode) return isoCode.getLanguage();

        if (languageCode.length() == 5 && (-1 != languageCode.indexOf('-') || -1 != languageCode.indexOf("_"))) {
            String[] codes = languageCode.split("[\\-_]");
            if (codes[0].equalsIgnoreCase(codes[1])) {
                isoCode = getISOLanguageCode(codes[0].toLowerCase());
                if (null != isoCode) return isoCode.getLanguage();
            }
        }
        return null;
    }

    @Override
    public Language findLanguageByName(String name) {
        String hql = "from Language where name=:name";
        Map param = new HashMap();
        param.put("name", name);
        return (Language) dao.retrieveOne(hql, param);
    }

    public Collection<Language> getLanguagesInProduct(Long productId) {
        String hql = "select distinct obj" +
                " from Product p join p.applications a join a.dictionaries d join d.dictLanguages dl join dl.language obj" +
                " where p.id=:prodId order by obj.name";
        Map param = new HashMap();
        param.put("prodId", productId);
        return dao.retrieve(hql, param);
    }

    public Collection<Language> getLanguagesInApplication(Long appId) {
        String hql = "select distinct obj" +
                " from Application a join a.dictionaries d join d.dictLanguages dl join dl.language obj" +
                " where a.id=:appId order by obj.name";
        Map param = new HashMap();
        param.put("appId", appId);
        return dao.retrieve(hql, param);
    }

    @Override
    public String getPreferredLanguageCode(Collection<Long> dictIdList,
                                           Long languageId) {
        Dictionary dict = (Dictionary) dao.retrieve(Dictionary.class, (Long) dictIdList.iterator().next());
        if (dict.getFormat().equals(Constants.DictionaryFormat.DCT.toString())) {
            AlcatelLanguageCode alCode = getDefaultAlcatelLanguageCode(languageId);
            if (alCode != null) {
                return alCode.getCode();
            } else {
                ISOLanguageCode isoCode = getDefaultISOLanguageCode(languageId);
                if (isoCode != null) {
                    return isoCode.getCode();
                } else {
                    return null;
                }
            }
        } else {
            ISOLanguageCode isoCode = getDefaultISOLanguageCode(languageId);
            if (isoCode != null) {
                return isoCode.getCode();
            } else {
                return null;
            }
        }
    }

    private AlcatelLanguageCode getDefaultAlcatelLanguageCode(Long languageId) {
        String hql = "from AlcatelLanguageCode where language.id=:langId and defaultCode=true";
        Map param = new HashMap();
        param.put("langId", languageId);
        return (AlcatelLanguageCode) dao.retrieveOne(hql, param);
    }

    private ISOLanguageCode getDefaultISOLanguageCode(Long languageId) {
        String hql = "from ISOLanguageCode where language.id=:langId and defaultCode=true";
        Map param = new HashMap();
        param.put("langId", languageId);
        return (ISOLanguageCode) dao.retrieveOne(hql, param);
    }

    @Override
    public Charset getPreferredCharset(Collection<Long> dictIdList,
                                       Long languageId) {
        Dictionary dict = (Dictionary) dao.retrieve(Dictionary.class, (Long) dictIdList.iterator().next());
        if (dict.getFormat().equals(Constants.DictionaryFormat.DCT.toString())) {
            if (dict.getEncoding().equals("ISO-8859-1")) {
                Language language = (Language) dao.retrieve(Language.class, languageId);
                return getCharset(language.getDefaultCharset());
            } else {
                return getCharset(dict.getEncoding());
            }
        } else {
            return getCharset("UTF-8");
        }
    }

    @Override
    public Language createLanguage(String name, Long defaultCharsetId) {
        Language language = findLanguageByName(name.trim());
        if (language != null) {
            throw new BusinessException(BusinessException.LANGUAGE_ALREADY_EXISTS, name);
        }
        language = new Language();
        language.setName(name.trim());
        language.setDefaultCharset(((Charset) dao.retrieve(Charset.class, defaultCharsetId)).getName());
        language = (Language) dao.create(language);
        languages = null;    // reset cache
        return language;
    }

    @Override
    public Language updateLanguage(Long id, String name, Long defaultCharsetId) {
        Language language = (Language) dao.retrieve(Language.class, id);
        if (language != null) {
            if (name != null && !name.trim().equals("")) {
                language.setName(name.trim());
            }
            if (defaultCharsetId != null) {
                language.setDefaultCharset(((Charset) dao.retrieve(Charset.class, defaultCharsetId)).getName());
            }
        }
        languages = null;    // reset cache
        return language;
    }

    @Override
    public void deleteLanguages(Collection<Long> idList) {
        String hql = "select count(*) from DictionaryLanguage where language.id in (:idList)";
        Map param = new HashMap();
        param.put("idList", idList);
        Number count = (Number) dao.retrieveOne(hql, param);
        if (count.intValue() == 0) {
            hql = "select count(*) from Translation where language.id in (:idList)";
            count = (Number) dao.retrieveOne(hql, param);
        }
        if (count.intValue() > 0) {
            throw new BusinessException(BusinessException.LANGUAGE_IS_IN_USE);
        } else {
            for (Long id : idList) {
                dao.delete(Language.class, id);
            }
            languages = null;    // reset cache
        }
    }

    @Override
    public Charset createCharset(String name) {
        Charset charset = getCharset(name.trim());
        if (charset != null) {
            throw new BusinessException(BusinessException.CHARSET_ALREADY_EXISTS, name);
        }
        charset = new Charset();
        charset.setName(name);
        charset = (Charset) dao.create(charset);
        charsets = null;    // reset cache
        return charset;
    }

    @Override
    public Charset updateCharset(Long id, String name) {
        Charset charset = (Charset) dao.retrieve(Charset.class, id);
        if (charset != null) {
            charset.setName(name);
            charsets = null;    // reset cache
        }
        return charset;
    }

    @Override
    public void deleteCharset(Collection<Long> idList) {
        String hql = "select count(*) from DictionaryLanguage where charset.id in (:idList)";
        Map param = new HashMap();
        param.put("idList", idList);
        Number count = (Number) dao.retrieveOne(hql, param);
        if (count.intValue() > 0) {
            throw new BusinessException(BusinessException.CHARSET_IS_IN_USE);
        } else {
            for (Long id : idList) {
                dao.delete(Charset.class, id);
            }
            charsets = null;    // reset cache
        }
    }

    @Override
    public Locale getLocale(Language language) {
        Collection<ISOLanguageCode> isoCodes = language.getIsoCodes();
        if (isoCodes != null) {
            for (ISOLanguageCode isoCode : isoCodes) {
                if (isoCode.isDefaultCode()) {
                    String code = isoCode.getCode();
                    if (code.length() == 2) {
                        return new Locale(code.substring(0, 2));
                    } else if (code.length() == 5) {
                        return new Locale(code.substring(0, 2), code.substring(3));
                    }
                }
            }
        }
        return Locale.ENGLISH;    // default ENGLISH for unknown locales
    }
}
