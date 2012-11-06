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
}
