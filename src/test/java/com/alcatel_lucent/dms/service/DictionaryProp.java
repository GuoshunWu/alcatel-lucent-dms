package com.alcatel_lucent.dms.service;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import com.alcatel_lucent.dms.SpringContext;
import com.alcatel_lucent.dms.model.AlcatelLanguageCode;
import com.alcatel_lucent.dms.model.ISOLanguageCode;

/**
 * Used for manually merge ICS R6.6 dictionaries
 * config charset settings for each DCT file in property file
 * @author allany
 *
 */
public class DictionaryProp {
	
	private static ResourceBundle bundle = ResourceBundle.getBundle("dict");
	
	/**
	 * Get encoding for a dictionary.
	 * @param dictionary
	 * @return
	 */
	public static String getDictionaryEncoding(String dictionary) {
		String property = bundle.getString(dictionary);
		return property.split(",")[0];
	}
	
	/**
	 * Get langCharset parameter for a dictionary.
	 * @param dictionary
	 * @return
	 */
	public static Map<String, String> getDictionaryCharsets(String dictionary) {
		String property = bundle.getString(dictionary);
		String charsetMode = property.split(",")[1];
		LanguageService service = (LanguageService) SpringContext.getService(LanguageService.class);
		Map<String, String> result = new HashMap<String, String>();
		Map<String, AlcatelLanguageCode> alCodeMap = service.getAlcatelLanguageCodes();
		for (String code : alCodeMap.keySet()) {
			if (charsetMode.equals("ANSI")) {
				result.put(code, alCodeMap.get(code).getLanguage().getDefaultCharset());
			} else {
				result.put(code, charsetMode);
			}
		}
		Map<String, ISOLanguageCode> isoCodeMap = service.getISOLanguageCodes();
		for (String code : isoCodeMap.keySet()) {
			if (charsetMode.equals("ANSI")) {
				result.put(code, isoCodeMap.get(code).getLanguage().getDefaultCharset());
			} else {
				result.put(code, charsetMode);
			}
		}
		return result;
	}
	
}
