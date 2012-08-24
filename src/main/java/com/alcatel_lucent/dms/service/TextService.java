package com.alcatel_lucent.dms.service;

import java.util.Collection;
import java.util.Map;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.model.Context;
import com.alcatel_lucent.dms.model.Text;
import com.alcatel_lucent.dms.model.Translation;

public interface TextService {
	
	/**
	 * Find context by name.
	 * @param name context name
	 * @return persistent Context object
	 */
	Context getContextByName(String name);
	
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
	 * @param ctxId context id
	 * @param texts texts with their translations to update in the context
	 * @return map of persistent text objects indexed by reference.
	 */
	Map<String, Text> updateTranslations(Long ctxId, Collection<Text> texts);
}
