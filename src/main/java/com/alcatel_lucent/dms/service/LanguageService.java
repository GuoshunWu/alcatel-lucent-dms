package com.alcatel_lucent.dms.service;

import java.util.Collection;
import java.util.Map;

import com.alcatel_lucent.dms.model.AlcatelLanguageCode;
import com.alcatel_lucent.dms.model.Charset;
import com.alcatel_lucent.dms.model.ISOLanguageCode;
import com.alcatel_lucent.dms.model.Language;

public interface LanguageService {
	Map<String, ISOLanguageCode> getISOLanguageCodes();
	
	Map<String, AlcatelLanguageCode> getAlcatelLanguageCodes();
	
	Map<String, Charset> getCharsets();
	
	Charset getCharset(String name);
	
	AlcatelLanguageCode getAlcatelLanguageCode(String code);
	
	ISOLanguageCode getISOLanguageCode(String code);
	
	Map<Long, Language> getLanguages();

    /**
     * find Language Object according to its code, return null if such language
     * not exist.
     * @param languageCode language code
     * */
    Language getLanguage(String languageCode);

	Language findLanguageByName(String name);
	
	/**
	 * Get all languages included in a product.
	 * @param productId product id
	 * @return languages
	 */
	Collection<Language> getLanguagesInProduct(Long productId);
	
	/**
	 * Get all languages included in an application.
	 * @param appId application id
	 * @return languages
	 */
	Collection<Language> getLanguagesInApplication(Long appId);

	String getPreferredLanguageCode(Collection<Long> dictIdList, Long languageId);

	Charset getPreferredCharset(Collection<Long> dictIdList, Long languageId);
	
	/**
	 * Create a language.
	 * @param name language name
	 * @param defaultCharsetId default charset id
	 * @return new Language object
	 */
	Language createLanguage(String name, Long defaultCharsetId);

	/**
	 * Update a language.
	 * @param id language id
	 * @param name new name, null if not changed
	 * @param defaultCharsetId new default charset id, null if not changed
	 * @return Language object
	 */
	Language updateLanguage(Long id, String name, Long defaultCharsetId);

	/**
	 * Delete languages.
	 * Note: Languages used by any of app dict or context dict cannot be deleted.
	 * @param idList
	 */
	void deleteLanguages(Collection<Long> idList);

	/**
	 * Create a charset.
	 * @param name charset name
	 * @return new Charset object
	 */
	Charset createCharset(String name);

	/**
	 * Update a charset.
	 * @param id charset id
	 * @param name new charset name
	 * @return Charset object
	 */
	Charset updateCharset(Long id, String name);

	/**
	 * Delete charsets.
	 * Charset being used by any app dictionary cannot be deleted.
	 * @param idList
	 */
	void deleteCharset(Collection<Long> idList);
}
