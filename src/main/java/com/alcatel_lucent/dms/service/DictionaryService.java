package com.alcatel_lucent.dms.service;

import java.io.File;
import java.util.Collection;
import java.util.Map;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.BusinessWarning;
import com.alcatel_lucent.dms.model.Dictionary;

public interface DictionaryService {
	/**
	 * Deliver a DCT dictionary
	 * 
	 * @param filename
	 *            DCT filename with full path
	 * @param appId
	 *            application id
	 * @param encoding
	 *            encoding of source file, null if auto-detected
	 *            (ANSI/UTF8/UTF16)
	 * @param langCodes
	 *            Alcatel code of languages to import, null if all languages
	 *            should be imported
	 * @param langCharset
	 *            mapping of language code and its source charset name
	 * @param warnings
	 *            a collection to hold output warnings
	 * @return persistent Dictionary object created
	 * @throws BusinessException
	 */
	Dictionary deliverDCT(String dictionaryName, String filename, Long appId,
			String encoding, String[] langCodes,
			Map<String, String> langCharset,
			Collection<BusinessWarning> warnings) throws BusinessException;

	/**
	 * Parse and preview a DCT dictionary
	 * 
	 * @param filename
	 *            DCT filename with full path
	 * @param appId
	 *            application id
	 * @param encoding
	 *            encoding of source file, null if auto-detected
	 *            (ANSI/UTF8/UTF16)
	 * @param warnings
	 *            a collection to hold output warnings
	 * @return transient Dictionary object
	 * @throws BusinessException
	 */
	Dictionary previewDCT(String dictionaryName, String filename, Long appId,
			String encoding, Collection<BusinessWarning> warnings)
			throws BusinessException;

	/**
	 * Import a DCT dictionary
	 * 
	 * @param dict
	 *            transient Dictionary object
	 * @param langCodes
	 *            Alcatel code of languages to import, null if all languages
	 *            should be imported
	 * @param langCharset
	 *            mapping of language code and its source charset name
	 * @param warnings
	 *            a collection to hold output warnings
	 * @return persistent Dictionary object created
	 */
	Dictionary importDCT(Dictionary dict, String[] langCodes,
			Map<String, String> langCharset,
			Collection<BusinessWarning> warnings);

	/**
	 * Generate a DCT dictionary
	 * 
	 * @param filename
	 *            output file name
	 * @param dctId
	 *            Dictionary object id
	 * @param encoding
	 *            encoding of output file, null if dictionary settings is used
	 * @param langCodes
	 *            Alcatel code of languages to generate, null if all languages
	 *            should be exported
	 * @param langCharset
	 *            mapping of language code and its source charset name
	 * @throws BusinessException
	 */
	void generateDCT(String filename, Long dctId, String encoding,
			String[] langCodes, Map<String, String> langCharset)
			throws BusinessException;

	/**
	 * Delete a DCT dictionary
	 * 
	 * @param dctName
	 *            to be deleted dictionary name
	 * @return the number of the deleted dictionary
	 * */
	int deleteDCT(String dctName);

	/**
	 * Parse and preview DCT dictionaries, if file is a dct file, it will be delivered and
	 * if it is a zip file or a dictionary, the dct file in which will be delivered recursively. 
	 * 
	 * TODO: these parameters need to be rethought.
	 * @param 
	 * */
	Collection<Dictionary> deliverDCTFiles(String rootDir, File file, Long appId,
			String encoding, String[] langCodes,
			Map<String, String> langCharset,
			Collection<BusinessWarning> warnings) throws BusinessException;
}