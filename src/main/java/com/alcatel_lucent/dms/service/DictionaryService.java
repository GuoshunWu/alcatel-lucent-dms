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
     * @param appId id of application into which the dictionaries are delivered
     * @return transient Dictionary object 
     * @throws BusinessException
     */
    public Collection<Dictionary> previewDictionaries(String rootDir, File file, Long appId) throws BusinessException;
    
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
                         Collection<BusinessWarning> warnings, DeliveryReport report);
    
    /**
     * Import a batch dictionaries
     * @param appId application id
     * @param dictList collection of transient dictionaries which should contain all necessary information
     * @param mode importing mode
     * @return delivery report
     * @throws BusinessException
     */
    DeliveryReport importDictionaries(Long appId, Collection<Dictionary> dictList, int mode) throws BusinessException;

    /**
     * Generate dictionary files.
     * @param dir     root directory save dictionary files
     * @param dictIds the collection of the ids for the dictionary to be generated.
     */
    void generateDictFiles(String dir, Collection<Long> dictIds);
    
    /**
     * Get the latest version of a dictionary
     * @param dictionaryBaseId dictionaryBase id
     * @param beforeDictionaryId returns latest version before specified dictionary if specified
     * @return Dictionary object
     */
    Dictionary getLatestDictionary(Long dictionaryBaseId, Long beforeDictionaryId);
    
    /**
     * Find lastest dictionary by name in scope of application base
     * @param appId application id
     * @param dictionaryName dictionary name
     * @return null if not found
     */
    Dictionary findLatestDictionaryInApp(Long appId, String dictionaryName);
    
    /**
     * Remove a dictionary from an application.
     * If it isn't referred by any app, then delete it.
     * @param appId application id
     * @param dictId dictionary id
     */
    void removeDictionaryFromApplication(Long appId, Long dictId);
    
    /**
     * Remove dictionaries from an application.
     * If a dict isn't referred by any app, then delete it.
     * @param appId application id
     * @param idList list of dictionary id
     */
    void removeDictionaryFromApplication(Long appId, Collection<Long> idList);
    
    /**
     * Remove a dictionary from all applications, and delete it.
     * @param id dictionary id
     * @return DictionaryBase id if the dictionary base was deleted or null if the dictionary base was not deleted. 
     */
    Long deleteDictionary(Long id);
    
    /**
     * Remove dictionaries from all applications, and delete them.
     * @param idList list of dictionary id
     * @return
     */
    void deleteDictionary(Collection<Long> idList);
    
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
	 * @param newDictId new dictionary version, must have same base with the old one
	 */
	void changeDictionaryInApp(Long appId, Long newDictId) throws BusinessException;

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

	/**
	 * Update labels
	 * @param idList list of label id
	 * @param maxLength new max length, null if no change required
	 * @param description new description, null if no change required
	 * @param context new context, null if no change required
	 */
	void updateLabels(Collection<Long> idList, String maxLength,
			String description, String context);
	
	/**
	 * Update a label key
	 * @param labelId label id
	 * @param key new key
	 * @throws BusinessException throws exception when key is duplicate
	 */
	void updateLabelKey(Long labelId, String key) throws BusinessException;
	
	/**
	 * Update a label reference.
	 * @param labelId label id
	 * @param reference new reference text
	 */
	void updateLabelReference(Long labelId, String reference);

    //Dictionary previewProp(String dictionaryName, Map<String, Collection<Properties>> propMap, Collection<BusinessWarning> warnings) throws BusinessException;
}