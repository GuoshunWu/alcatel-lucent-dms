package com.alcatel_lucent.dms.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alcatel_lucent.dms.model.AlcatelLanguageCode;
import com.alcatel_lucent.dms.model.Charset;
import com.alcatel_lucent.dms.model.Language;

public class BaseServiceImpl {
	private DaoService dao;

	// base data for dms
	private static Map<String, AlcatelLanguageCode> alcatelLanguageCodes = null;
	private static Map<String, Charset> charsets = null;
	private static Map<Long, Language> languages = null;

	public void init() {
		// Load the base data
	}

	public Map<String, AlcatelLanguageCode> getAlcatelLanguageCodes() {
		if (null != alcatelLanguageCodes)
			return alcatelLanguageCodes;

		alcatelLanguageCodes = new HashMap<String, AlcatelLanguageCode>();
		List<AlcatelLanguageCode> alLangCodes = dao
				.retrieve("from AlcatelLanguageCode");
		for (AlcatelLanguageCode alLangCode : alLangCodes) {
			alcatelLanguageCodes.put(alLangCode.getCode(), alLangCode);
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

	public void destroy() {

	}

	public void setDao(DaoService dao) {
		this.dao = dao;
	}

	public DaoService getDao() {
		return dao;
	}

}
