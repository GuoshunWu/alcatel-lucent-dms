package com.alcatel_lucent.dms.service;

import com.alcatel_lucent.dms.model.Context;

/**
 * Created by guoshunw on 2014/6/11.
 */
public interface ContextService {

    /**
     * Merge context A, context B into a context which id is mergedToId
     * @param ctxAId context A id
     * @param ctxATextId the id of text which in context a
     * @param ctxBId context B id
     * @param ctxBTextId the id of text which in context b
     * @param mergedToId the context id to merged to.
     * @return context deleted due to the merge
     * */
    Context mergeContext(Long ctxAId, Long ctxATextId,  Long ctxBId, Long ctxBTextId, Long mergedToId);
}
