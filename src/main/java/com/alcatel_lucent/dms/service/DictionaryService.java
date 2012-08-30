package com.alcatel_lucent.dms.service;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.BusinessWarning;
import com.alcatel_lucent.dms.model.Dictionary;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

public interface DictionaryService {
    /**
     * Deliver a DCT dictionary
     *
     * @param dictionaryName    dictionary name
     * @param version     dictionary version
     * @param filename	  DCT filename with full path
     * @param appId       application id
     * @param mode		  importing mode
     * @param encoding    encoding of source file, null if auto-detected
     *                    (ANSI/UTF8/UTF16)
     * @param langCodes   Alcatel code of languages to import, null if all languages
     *                    should be imported
     * @param langCharset mapping of language code and its source charset name
     * @param warnings    a collection to hold output warnings
     * @return persistent Dictionary object created
     * @throws com.alcatel_lucent.dms.BusinessException
     */
    Dictionary deliverDCT(String dictionaryName,String version, String filename, Long appId,
    					  int mode, String encoding, String[] langCodes,
                          Map<String, String> langCharset,
                          Collection<BusinessWarning> warnings) throws BusinessException;

    /**
     * Parse and preview a DCT dictionary
     *
     * @param path DCT file full path
     * @param appId    application id
     * @param encoding encoding of source file, null if auto-detected
     *                 (ANSI/UTF8/UTF16)
     * @param warnings a collection to hold output warnings
     * @return transient Dictionary object
     * @throws com.alcatel_lucent.dms.BusinessException
     */
    Dictionary previewDCT(String dictionaryName, String path, Long appId,
                          String encoding, Collection<BusinessWarning> warnings)
            throws BusinessException;

    /**
     * Import a DCT dictionary
     *
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
     * @param warnings    a collection to hold output warnings
     * @return persistent Dictionary object created
     */
    Dictionary importDCT(Dictionary dict, String version, int mode, String[] langCodes,
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
     * Parse and preview a Multilingual Dictionary Configuration(MDC) file
     *
     * @param dictionaryName dictionary name
     * @param is
     * @param path the file path if input stream from a file
     * @param appId          application Id
     * @param warnings       a collection to hold output warnings
     * @return transient Dictionary object
     * @throws com.alcatel_lucent.dms.BusinessException
     */
    Dictionary previewMDC(String dictionaryName, String path, InputStream is, Long appId,
                          Collection<BusinessWarning> warnings) throws BusinessException;

    /**
     * Generate dct file of specific dictionary in the dicts collections
     *
     * @param dir     root directory save dct files
     * @param dictIds the collection of the ids for the dictionary to be generated.
     */
    void generateDCTFiles(String dir, Collection<Long> dictIds, String[] langCodes);


    /**
     * Parse and preview DCT dictionaries, if file is a dct file, it will be delivered and
     * if it is a zip file or a dictionary, the dct file in which will be delivered recursively.
     * <p/>
     * TODO: these parameters need to be rethought.
     *
     * @param
     */
    Collection<Dictionary> deliverDCTFiles(String rootDir, File file, Long appId, int mode,
                                           String encoding, String[] langCodes,
                                           Map<String, String> langCharset,
                                           Collection<BusinessWarning> warnings) throws BusinessException;

    Collection<Dictionary> deliverMDCFiles(String rootDir, File file, Long appId, int mode, String[] langCodes,
                                           Map<String, String> langCharset,
                                           Collection<BusinessWarning> warnings) throws BusinessException;
    
    /**
     * Get the latest version of a dictionary
     * @param dictionaryBaseId dictionaryBase id
     * @param beforeDictionaryId returns latest version before specified dictionary if specified
     * @return Dictionary object
     */
    Dictionary getLatestDictionary(Long dictionaryBaseId, Long beforeDictionaryId);
}