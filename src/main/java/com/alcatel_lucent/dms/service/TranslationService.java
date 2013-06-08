package com.alcatel_lucent.dms.service;

import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.alcatel_lucent.dms.model.Label;
import com.alcatel_lucent.dms.model.LabelTranslation;

public interface TranslationService {

    /**
     * Calculate translation summary in dictionary level
     * @param prodId product id
     * @return composite map with dictionary id as 1st-level key and language id as 2nd-level key
     * 			value is {num_of_translated_labels, num_of_not_translated_labels, num_of_in_progress_labels }
     */
    Map<Long, Map<Long, int[]>> getDictTranslationSummaryByProd(Long prodId);
    
    /**
     * Calculate translation summary in dictionary level
     * @param appId application id
     * @return composite map with dictionary id as 1st-level key and language id as 2nd-level key
     * 			value is {num_of_translated_labels, num_of_not_translated_labels, num_of_in_progress_labels }
     */
    Map<Long, Map<Long, int[]>> getDictTranslationSummaryByApp(Long appId);

    /**
     * Calculate translation summary in application level
     * @param prodId product id
     * @return composite map with application id as 1st-level key and language id as 2nd-level key
     * 			value is {num_of_translated_labels, num_of_not_translated_labels, num_of_in_progress_labels }
     */
    Map<Long, Map<Long, int[]>> getAppTranslationSummaryByProd(Long prodId);
    
    /**
     * Calculate translation summary of application
     * @param appId application id
     * @return a map with language id as key 
     * and the value is {num_of_translated_labels, num_of_not_translated_labels, num_of_in_progress_labels }
     */
    Map<Long, Map<Long, int[]>> getAppTranslationSummaryByApp(Long appId);
	
    /**
     * Calculate translation summary in label level
     * @param dictId dictionary id
     * @return map data map, key is label is, value is {num_of_translated_languages, num_of_not_translated_languages, num_of_in_progress_languages}
     */
	Map<Long, int[]> getLabelTranslationSummary(Long dictId);
	
	/**
	 * Calculate translation summary for a single label
	 * @param labelId label id
	 * @return array of {num_of_translated_languages, num_of_not_translated_languages, num_of_in_progress_languages}
	 */
	int[] getLabelTranslationSummaryByLabel(Long labelId);

	/**
     * Generate excel report of translation summary in dictionary level
     * @param prodId product id
     * @param langIds language filters, null in case of all languages
     * @param output output stream
     */
    void generateDictTranslationReportByProd(Long prodId, Collection<Long> langIds, OutputStream output);
    
    /**
     * Generate excel report of translation summary in application level
     * @param prodId product id
     * @param langIds language filters, null in case of all languages
     * @param output output stream
     */
    void generateAppTranslationReportByProd(Long prodId, Collection<Long> langIds, OutputStream output);

	/**
     * Generate excel report of translation summary in dictionary level
     * @param appId application id
     * @param langIds language filters, null in case of all languages
     * @param output output stream
     */
    void generateDictTranslationReportByApp(Long appId, Collection<Long> langIds, OutputStream output);
    
    /**
     * Generate excel report of translation summary in application level
     * @param appId application id
     * @param langIds language filters, null in case of all languages
     * @param output output stream
     */
    void generateAppTranslationReportByApp(Long appId, Collection<Long> langIds, OutputStream output);

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
	
	/**
	 * Retrieve label information with translation of specific language by search of text 
	 * @param prodId product id, ignored if appId or dictId is specified
	 * @param appId application id, ignored if dictId is specified
	 * @param dictId dictionary id
	 * @param langId language id
	 * @param text search text (case insensitave)
	 * @return collection of labels, translation information is contained in ct and ot properties
	 */
	Collection<Label> searchLabelsWithTranslation(Long prodId,
			Long appId, Long dictId, Long langId, String text);



	/**
	 * Retrieve label translations data for specified label.
	 * @param labelId label id
	 * @param status translation status filter, null indicating all status
	 * @return collection of LabelTranslation objects
	 */
	List<LabelTranslation> getLabelTranslations(Long labelId, Integer status);

}
