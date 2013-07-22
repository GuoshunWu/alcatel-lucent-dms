package com.alcatel_lucent.dms.service;

import java.util.Collection;
import java.util.Map;

import com.alcatel_lucent.dms.Constants;
import com.alcatel_lucent.dms.model.Context;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.Label;
import com.alcatel_lucent.dms.model.Text;
import com.alcatel_lucent.dms.model.Translation;

public interface TextService {
	
	/**
	 * Find context by name.
	 * @param name context name
	 * @return persistent Context object
	 */
	Context getContextByKey(String key);
	
	/**
	 * Find text object by context and reference.
	 * @param ctxId context id
	 * @param reference reference text
	 * @return Text object
	 */
	Text getText(Long ctxId, String reference);
	
	/**
	 * Create text object.
	 * The method will NOT check if text with same context and reference exists.
	 * @param ctxId context id
	 * @param reference reference text
	 * @return persistent Text object
	 * @throws com.alcatel_lucent.dms.BusinessException
	 */
	Text addText(Long ctxId, String reference);
	
	/**
	 * Find translation.
	 * @param ctxId context id
	 * @param reference reference text
	 * @param languageId language id
	 * @return Translation object
	 */
	Translation getTranslation(Long ctxId, String reference, Long languageId);
	
	/**
	 * Add translations for text.
	 * The method will create Text object if it doesn't exist,
	 * and create or update Translation objects provided in map.
	 * @param ctxId context id
	 * @param reference reference text
	 * @param translations map of translations which takes language id as key,
	 *        translated text as value
	 * @return Text object
	 */
	Text addTranslations(Long ctxId, String reference, Map<Long, String> translations);
	
	/**
	 * Add translations for text.
	 * @param textId text id
	 * @param translations map of translations which takes language id as key,
	 *        translated text as value
	 * @return Text object
	 */
	Text addTranslations(Long textId, Map<Long, String> translations);
	
	/**
	 * Update text and translation in a context.
	 * Insert or update text and translation entities in a batch.
	 * The method is invoked for importing dictionaries, receiving tasks and updating reference of labels.
	 * @param ctxId context id
	 * @param texts texts with their translations to update in the context
	 * @param mode importing mode, see DictionaryService.importDictionary()
	 * @return map of persistent text objects indexed by reference.
	 */
	Map<String, Text> updateTranslations(Long ctxId, Collection<Text> texts, Constants.ImportingMode mode);

	/**
	 * Update translation for a new label.
	 * The method is invoked when a new label was added or reference text of a label is changed.
	 * An auto-match of translation action will be performed.
	 * @param label new label or the label whose reference text was changed
	 * @return text object linked to the label
	 */
	Text updateTranslations(Label label);

    /**
     *  Receive the completed translation in a excel file.
     *  Insert or update translation for a given  languageId in excel file,
     *  which name is dictionary name.
     *  @param fileName the file name to be updated
     *  @param languageId language ID for insert or update Translation
     */
    int receiveTranslation(String fileName, Long languageId);
    
    /**
     * Update translation status
     * @param tarnsIds Translation object id list
     * @param transStatus new translation status
     */
    void updateTranslationStatus(Collection<Long> transIds, int transStatus);
    
    /**
     * Update translation status by dictionaries
     * @param dictIds dictionary id list
     * @param langIds language id list, a null value identifies all languages
     * @param transStatus new translation status
     */
    void updateTranslationStatusByDict(Collection<Long> dictIds, Collection<Long> langIds, int transStatus);
    
    /**
     * Update translation status by applications
     * @param appIds application id list
     * @param langIds language id list, a null value identifies all languages
     * @param transStatus new translation status
     */
    void updateTranslationStatusByApp(Collection<Long> appIds, Collection<Long> langIds, int transStatus);

    /**
     * Update translation status by labels
     * @param labelIds label id list
     * @param transStatus new translation status
     */
	void updateTranslationStatusByLabel(Collection<Long> labelIds, int transStatus);

    /**
     * Get translation details by context
     * @param ctxId context id
     * @return map of text objects indexed by reference.
     */
    Map<String, Text> getTextsAsMap(Long ctxId);

    /**
     * Populate context name and get the Context instance, create one if not exists.
     * @param contextExp context expression, supported variables: [DICT], [APP], [PROD]
     * @param dict Dictionary object, used to populate context value
     * @return Context instance
     */
	Context getContextByExpression(String contextExp, Dictionary dict);

	/**
	 * Update one translation
	 * Update translation result of a label.
	 * If the translation is referred by other dictionaries:
	 *   do nothing and return name of the dictionaries if confirmAll is empty
	 *   update translation result if confirmAll is true
	 *   change context of the label to [DICT] and update translation result if confirmAll is false
	 * @param labelId label id
	 * @param translationId translation id
	 * @param translation translation result
	 * @param confirmAll whether to apply same change to other dictionaries
	 * @return other dictionaries in which the same translation is referred
	 */
	Collection<String> updateTranslation(Long labelId, Long translationId,
			String translation, Boolean confirmAll);

	/**
	 * Populate T/N/I summary of texts.
	 * @param texts
	 */
	void populateTranslationSummary(Collection<Text> texts);

	/**
	 * Populate Refs of texts.
	 * @param texts
	 */
	void populateRefs(Collection<Text> texts);

	/**
	 * Delete a text when there is no refs.
	 * @param id text id
	 */
	void deleteText(Long id);

}
