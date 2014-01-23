package com.alcatel_lucent.dms.service;

import com.alcatel_lucent.dms.model.PreferredReference;

/**
 * User: guoshunw
 * Date: 13-7-25
 * Time: 下午2:53
 */
public interface PreferredReferenceService {
    /**
     * Create a PreferredTranslation.
     *
     * @param reference reference
     * @return new PreferredReference object or null if create failed
     */
    PreferredReference createPreferredReference(String reference,String comment);

}
