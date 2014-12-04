package com.alcatel_lucent.dms.action.context;

import com.alcatel_lucent.dms.action.JSONAction;
import com.alcatel_lucent.dms.model.Context;
import com.alcatel_lucent.dms.service.ContextService;

import java.util.Arrays;

public class MergeContextAction extends JSONAction {

    private ContextService contextService;

    private Long contextAId;
    private Long contextBId;

    private Long contextATextId;
    private Long contextBTextId;


    private Long mergedToContextId;
    private String reference;

    @Override
    protected String performAction() throws Exception {
        log.info("contextAId={}, contextATextId={}, contextBTextId={}, contextBId={}, mergedToContextId={}, reference={}",
                contextAId, contextATextId, contextBId, contextBTextId, mergedToContextId, reference);
        Context ctx = contextService.mergeContext(contextAId, contextATextId, contextBId, contextBTextId, mergedToContextId);

        if (null != ctx) {
            setStatus(1);
            setMessage(getText("message.context_delete", Arrays.asList(ctx.getKey())));
            return SUCCESS;
        }

        setMessage(getText("message.success") );
        return SUCCESS;
    }

    public Long getContextATextId() {
        return contextATextId;
    }

    public void setContextATextId(Long contextATextId) {
        this.contextATextId = contextATextId;
    }

    public Long getContextBTextId() {
        return contextBTextId;
    }

    public void setContextBTextId(Long contextBTextId) {
        this.contextBTextId = contextBTextId;
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
