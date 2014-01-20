package com.alcatel_lucent.dms.service;

import com.alcatel_lucent.dms.model.*;

import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: guoshunw
 * Date: 13-7-25
 * Time: 下午2:53
 * To change this template use File | Settings | File Templates.
 */
public interface PreferredTranslationService {

    /**
     * Create a PreferredTranslation.
     *
     * @param ref reference
     * @return new PreferredTranslation object
     */
    PreferredTranslation createPreferredTranslation(String ref, String trans, String comment, Long languageId);


    /**
     * Update a PreferredTranslation.
     *
     * @param id preferredTranslation id
     * @return PreferredTranslation object
     */
    PreferredTranslation updatePreferredTranslation(Long id, String ref, String trans, String comment);


    /**
     * Delete PreferredTranslations.
     *
     * @param ids ids of the PreferredTranslations to be deleted.
     */
    void deletePreferredTranslations(Collection<Long> ids);

}
