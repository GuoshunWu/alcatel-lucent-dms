package com.alcatel_lucent.dms.service;

import java.io.OutputStream;
import java.util.Map;

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
     * Generate excel report of translation summary in dictionary level
     * @param prodId product id
     * @param output output stream
     */
    void generateDictTranslationReport(Long prodId, OutputStream output);
    
    /**
     * Generate excel report of translation summary in application level
     * @param prodId product id
     * @param output output stream
     */
    void generateAppTranslationReport(Long prodId, OutputStream output);
}
