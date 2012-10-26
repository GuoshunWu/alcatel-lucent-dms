package com.alcatel_lucent.dms.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.alcatel_lucent.dms.model.AlcatelLanguageCode;
import com.alcatel_lucent.dms.model.Charset;
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
        if (null == alCode) {
            isoCode = getISOLanguageCode(languageCode);
            if (null == isoCode) {
                return null;
            }
            return isoCode.getLanguage();
        }
        return alCode.getLanguage();
    }

	@Override
	public Language findLanguageByName(String name) {
		String hql = "from Language where name=:name";
		Map param = new HashMap();
		param.put("name", name);
		return (Language) dao.retrieveOne(hql, param);
	}
}
