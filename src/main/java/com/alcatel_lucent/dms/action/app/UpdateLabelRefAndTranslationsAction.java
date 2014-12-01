package com.alcatel_lucent.dms.action.app;

import com.alcatel_lucent.dms.action.JSONAction;
import com.alcatel_lucent.dms.service.DictionaryService;
import com.opensymphony.xwork2.util.Key;

import java.util.Map;

@SuppressWarnings("serial")
public class UpdateLabelRefAndTranslationsAction extends JSONAction {

    private DictionaryService dictionaryService;

    private Long id;
    private String reference;
    private Map<Long, String> newTranslations;

    protected String performAction() throws Exception {
        log.info("UpdateLabelAction: id={}, reference={}, newTranslations={}, mapNewTrans={}", id, reference, newTranslations);

        dictionaryService.updateLabelReferenceAndTranslations(id, reference, newTranslations);

        setStatus(0);
        setMessage(getText("message.success"));
        return SUCCESS;
    }

    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public Map<Long, String> getNewTranslations() {
        return newTranslations;
    }

    @Key(value = java.lang.Long.class)
    public void setNewTranslations(Map<Long, String> newTranslations) {
        this.newTranslations = newTranslations;
    }
}


