package com.alcatel_lucent.dms.action.context;

import com.alcatel_lucent.dms.action.JSONAction;
import com.alcatel_lucent.dms.service.ContextService;

public class MergeContextAction extends JSONAction {

    private ContextService contextService;

    private Long contextAId;
    private Long contextBId;
    private Long mergedToContextId;
    private String reference;

    @Override
    protected String performAction() throws Exception {
        log.info("contextAId={}, contextBId={}, mergedToContextId={}, reference={}",
                contextAId, contextBId, mergedToContextId, reference);
        contextService.mergeContext(contextAId, contextBId, mergedToContextId);
        return SUCCESS;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public ContextService getContextService() {
        return contextService;
    }

    public void setContextService(ContextService contextService) {
        this.contextService = contextService;
    }

    public Long getContextAId() {
        return contextAId;
    }

    public void setContextAId(Long contextAId) {
        this.contextAId = contextAId;
    }

    public Long getContextBId() {
        return contextBId;
    }

    public void setContextBId(Long contextBId) {
        this.contextBId = contextBId;
    }

    public Long getMergedToContextId() {
        return mergedToContextId;
    }

    public void setMergedToContextId(Long mergedToContextId) {
        this.mergedToContextId = mergedToContextId;
    }
}
