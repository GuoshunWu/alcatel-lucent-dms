package com.alcatel_lucent.dms.service;

import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;

import com.alcatel_lucent.dms.model.Label;

public interface TranslationService {

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
     * Calculate translation summary in label level
     * @param dictId dictionary id
     * @return map data map, key is label is, value is {num_of_translated_languages, num_of_not_translated_languages, num_of_in_progress_languages}
     */
	Map<Long, int[]> getLabelTranslationSummary(Long dictId);

	/**
     * Generate excel report of translation summary in dictionary level
     * @param prodId product id
     * @param langIds language filters, null in case of all languages
     * @param output output stream
     */
    void generateDictTranslationReport(Long prodId, Collection<Long> langIds, OutputStream output);
    
    /**
     * Generate excel report of translation summary in application level
     * @param prodId product id
     * @param langIds language filters, null in case of all languages
     * @param output output stream
     */
    void generateAppTranslationReport(Long prodId, Collection<Long> langIds, OutputStream output);
    
    /**
     * Generat excel details of label translations
     * @param dictIds collection of dictionary id
     * @param langIds collection of language id
     * @param output output stream
     */
    void exportTranslations(Collection<Long> dictIds, Collection<Long> langIds, OutputStream output);

    /**
     * Retrieve label information with translation of specific language.
     * @param dictId dictionary id
     * @param langId language id
     * @return collection of labels, translation information is contained in ct and ot properties.
     */
	Collection<Label> getLabelsWithTranslation(Long dictId, Long langId);

}
