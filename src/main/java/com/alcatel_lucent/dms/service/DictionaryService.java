package com.alcatel_lucent.dms.service;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.BusinessWarning;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.DictionaryLanguage;

import java.io.File;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.collections.map.MultiKeyMap;

public interface DictionaryService {
    /**
     * Parse and preview application dictionaries in a folder
     * @param rootDir the part of absolute part to be removed from dictionary name
     * @param file A directory or a dictionary file
     * @param warnings a collection to hold output warnings
     * @return transient Dictionary object 
     * @throws BusinessException
     */
    public Collection<Dictionary> previewDictionaries(String rootDir, File file, Collection<BusinessWarning> warnings) throws BusinessException;
    
    /**
     * Import an application dictionary
     * @param appId application id
     * @param dict        transient Dictionary object
     * @param version	  dictionary version
     * @param mode		  importing mode
     * DELIVERY_MODE: in case application owner delivers a dictionary file
     *   overwrite label attributes
     *   insert new context translations only, not overwriting existing context translation
     *   for new labels or changed reference texts:
     *       the translation will use context dictionary
     *       set translation status to UNTRANSLATED if reference text = translation
     *       or TRANSLATED if reference text <> translation 
     *   for existing reference texts:
     *       if translation is not changed since last version,
     *           copy translation from last version
     *   	 if translation has been changed since last version,
     *           keep the translation in delivered file, the label will not use context dictionary
     *      
     * TRANSLATION_MODE: in case translation manager imports a translated dictionary
     *   update translations in context dictionary only
     *   no update to existing label attributes
     *   set translation status to UNTRANSLATED if reference text = translation
     *   or TRANSLATED if reference text <> translation
     * @param langCodes   Alcatel code of languages to import, null if all languages
     *                    should be imported
     * @param langCharset mapping of language code and its source charset name
     * 					  There might be a 'DEFAULT' language code indicating default charset name
     * @param warnings    a collection to hold output warnings
     * @return persistent Dictionary object created
     */
    Dictionary importDictionary(Long appId, Dictionary dict, String version, int mode, String[] langCodes,
                         Map<String, String> langCharset,
                         Collection<BusinessWarning> warnings);

    /**
     * Generate a DCT dictionary
     *
     * @param filename  output file name
     * @param dctId     Dictionary object id
     * @param encoding  encoding of output file, null if dictionary settings is used
     * @param langCodes Alcatel code of languages to generate, null if all languages
     *                  should be exported
     * @throws com.alcatel_lucent.dms.BusinessException
     */
    void generateDCT(String filename, Long dctId, String encoding,
                     String[] langCodes)
            throws BusinessException;

    /**
     * Generate a DCT dictionary
     *
     * @param filename  output file name
     * @param dctId     Dictionary object id
     * @param langCodes Alcatel code of languages to generate, null if all languages
     *                  should be exported
     * @throws com.alcatel_lucent.dms.BusinessException
     */
    void generateMDC(String filename, Long dctId, String[] langCodes)
            throws BusinessException;

    /**
     * Generate dct file of specific dictionary in the dicts collections
     *
     * @param dir     root directory save dct files
     * @param dictIds the collection of the ids for the dictionary to be generated.
     */
    void generateDCTFiles(String dir, Collection<Long> dictIds, String[] langCodes);


  
    /**
     * Get the latest version of a dictionary
     * @param dictionaryBaseId dictionaryBase id
     * @param beforeDictionaryId returns latest version before specified dictionary if specified
     * @return Dictionary object
     */
    Dictionary getLatestDictionary(Long dictionaryBaseId, Long beforeDictionaryId);
    
    /**
     * Remove a dictionary from an application, without deleting it.
     * @param appId application id
     * @param dictId dictionary id
     */
    void removeDictionaryFromApplication(Long appId, Long dictId);
    
    /**
     * Remove a dictionary from all applications, and delete it.
     * @param id dictionary id
     * @return DictionaryBase id if the dictionary base was deleted or null if the dictionary base was not deleted. 
     */
    Long deleteDictionary(Long id);
    
    /**
     * Calculate translation summary in dictionary level
     * @param prodId product id
     * @return composite map with dictionary id as 1st-level key and language id as 2nd-level key
     * 			value is {num_of_translated_labels, num_of_not_translated_labels, num_of_in_progress_labels }
     */
    Map<Long, Map<Long, int[]>> getDictTranslationSummary(Long prodId);

    /**
     * Calculate translation summary in application level
     * @param prodId product id
     * @return composite map with application id as 1st-level key and language id as 2nd-level key
     * 			value is {num_of_translated_labels, num_of_not_translated_labels, num_of_in_progress_labels }
     */
    Map<Long, Map<Long, int[]>> getAppTranslationSummary(Long prodId);

    /**
     * Count total number of labels in an application
     * @param appId application id
     * @return total number of labels
     */
	public int getLabelNumByApp(Long appId);
	
	/**
	 * Change format of dictionary
	 * @param id dictionary id
	 * @param format format
	 * @throws BusinessException when format is invalid
	 */
	void updateDictionaryFormat(Long id, String format) throws BusinessException;
	
	/**
	 * Change encoding of dictionary
	 * @param id dictionary id
	 * @param encoding encoding (valid encodings: ISO-8859-1, UTF-8, UTF-16LE)
	 * @throws BusinessException when encoding is invalid
	 */
	void updateDictionaryEncoding(Long id, String encoding) throws BusinessException;
	
	/**
	 * Change dictionary version in application
	 * @param appId application id
	 * @param oldDictId old dictionary version, must exist in the app
	 * @param newDictId new dictionary version, must have same base with the old one
	 */
	void changeDictionaryInApp(Long appId, Long oldDictId, Long newDictId) throws BusinessException;

	/**
	 * Add language to a dictionary
	 * @param dictId dictionary id
	 * @param code language code
	 * @param languageId language id
	 * @param charsetId charset id
	 * @return DictionaryLanguage object
	 * @throws BusinessException in case duplicate language code
	 */
	DictionaryLanguage addLanguage(Long dictId, String code, Long languageId, Long charsetId) throws BusinessException;

	/**
	 * Update dictionary language attributes
	 * @param id DictionaryLanguage object id
	 * @param code language code, null if no change required
	 * @param languageId language id, null if no change required
	 * @param charsetId charset id, null if no change required
	 * @return DictionaryLanguage object
	 */
	DictionaryLanguage updateDictionaryLanguage(Long id, String code, Long languageId, Long charsetId);

	/**
	 * Remove language from a dictionary
	 * @param ids list of id to be removed
	 */
	void removeDictionaryLanguage(Collection<Long> ids);
	
    
    //Dictionary previewProp(String dictionaryName, Map<String, Collection<Properties>> propMap, Collection<BusinessWarning> warnings) throws BusinessException;
}