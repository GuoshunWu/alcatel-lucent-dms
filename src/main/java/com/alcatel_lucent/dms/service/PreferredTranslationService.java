package com.alcatel_lucent.dms.service;

import com.alcatel_lucent.dms.model.PreferredReference;
import com.alcatel_lucent.dms.model.PreferredTranslation;

/**
 * Created with IntelliJ IDEA.
 * User: guoshunw
 * Date: 13-7-25
 * Time: 下午2:53
 * To change this template use File | Settings | File Templates.
 */
public interface PreferredTranslationService {

    /**
     * Update a PreferredTranslation.
     *
     * @param id preferredTranslation id
     * @return PreferredTranslation object
     */
    PreferredTranslation updatePreferredTranslation(Long preferredReferenceId, Long id, Long languageId, String translation, String comment);

}
