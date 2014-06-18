package com.alcatel_lucent.dms.service;

/**
 * Created by guoshunw on 2014/6/11.
 */
public interface ContextService {

    /**
     * Merge context A, context B into a context which id is mergedToId
     * @param ctxAId context A id
     * @param ctxBId context B id
     * @param mergedToId the context id to merged to.
     * */
    void mergeContext(Long ctxAId, Long ctxBId, Long mergedToId);
}
