package com.alcatel_lucent.dms.service;

import java.util.Map;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.model.Dictionary;

public interface DictionaryService {
	/**
	 * Deliver a DCT dictionary
	 * @param filename 
	 * @param appId application id
	 * @param encoding encoding of source file, null if auto-detected (ANSI/UTF8/UTF16)
	 * @param langCodes Alcatel code of languages to import, null if all languages should be imported
	 * @param langCharset mapping of language code and its source charset name
	 * @return Dictionary object created
	 * @throws BusinessException
	 */
	Dictionary deliverDCT(String filename, Long appId, String encoding, String[] langCodes, Map<String, String> langCharset) throws BusinessException;
	
	/**
	 * Generate a DCT dictionary
	 * @param filename output file name
	 * @param dctId Dictionary object id
	 * @param encoding encoding of output file, null if dictionary settings is used
	 * @param langCodes Alcatel code of languages to generate, null if all languages should be exported
	 * @param langCharset mapping of language code and its source charset name
	 * @throws BusinessException
	 */
	void generateDCT(String filename, Long dctId, String encoding, String[] langCodes, Map<String, String> langCharset) throws BusinessException;
}