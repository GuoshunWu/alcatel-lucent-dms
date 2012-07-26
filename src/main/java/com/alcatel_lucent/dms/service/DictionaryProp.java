package com.alcatel_lucent.dms.service;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alcatel_lucent.dms.model.AlcatelLanguageCode;
import com.alcatel_lucent.dms.model.ISOLanguageCode;

/**
 * Used for manually merge ICS R6.6 dictionaries
 * config charset settings for each DCT file in property file
 * @author allany
 *
 */
@Component("dictionaryProp")
public class DictionaryProp {
	
	private static ResourceBundle bundle = ResourceBundle.getBundle("dict");
	
	@Autowired
	private LanguageService langService;
	
	/**
	 * Get encoding for a dictionary.
	 * @param dictionary
	 * @return
	 */
	public String getDictionaryEncoding(String dictionary) {
		String property = bundle.getString(dictionary);
		return property.split(",")[0];
	}
	
	/**
	 * Get langCharset parameter for a dictionary.
	 * @param dictionary
	 * @return
	 */
	public Map<String, String> getDictionaryCharsets(String dictionary) {
		String property = bundle.getString(dictionary);
		String charsetMode = property.split(",")[1];
		Map<String, String> result = new HashMap<String, String>();
		Map<String, AlcatelLanguageCode> alCodeMap = langService.getAlcatelLanguageCodes();
		for (String code : alCodeMap.keySet()) {
			if (charsetMode.equals("ANSI")) {
				result.put(code, alCodeMap.get(code).getLanguage().getDefaultCharset());
				result.put(code.replace("-", "_"), alCodeMap.get(code).getLanguage().getDefaultCharset());
			} else {
				result.put(code, charsetMode);
				result.put(code.replace("-", "_"), charsetMode);
			}
		}
		Map<String, ISOLanguageCode> isoCodeMap = langService.getISOLanguageCodes();
		for (String code : isoCodeMap.keySet()) {
			if (charsetMode.equals("ANSI")) {
				result.put(code, isoCodeMap.get(code).getLanguage().getDefaultCharset());
				result.put(code.replace("-", "_"), isoCodeMap.get(code).getLanguage().getDefaultCharset());
			} else {
				result.put(code, charsetMode);
				result.put(code.replace("-", "_"), charsetMode);
			}
		}
		return result;
	}
	
}
