package com.alcatel_lucent.dms.action.admin;

import com.alcatel_lucent.dms.SystemError;
import com.alcatel_lucent.dms.action.JSONAction;
import com.alcatel_lucent.dms.model.PreferredReference;
import com.alcatel_lucent.dms.service.GlossaryService;
import com.alcatel_lucent.dms.service.PreferredReferenceService;
import com.alcatel_lucent.dms.service.PreferredTranslationService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.poi.util.ArrayUtil;
import org.apache.struts2.convention.annotation.Action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

@SuppressWarnings("serial")
public class PreferredTranslationAction extends JSONAction {

    private String oper;
    private String id; // this is PreferredReference id

    private Long languageId;  //PreferredTranslation

    private String reference;   //PreferredReference
    private String comment;     //PreferredReference

    private Long ptId;  //PreferredTranslation id
    private String translation;  //PreferredTranslation


    private PreferredTranslationService preferredTranslationService;
    private PreferredReferenceService preferredReferenceService;


    @Action("create-preferred-reference")
    public String createPreferredReference() throws Exception {
        PreferredReference preferredReference = preferredReferenceService.createPreferredReference(reference, comment);
        boolean isCreateSuccess = null != preferredReference;
        setStatus(isCreateSuccess ? 0 : -1);
        setMessage(isCreateSuccess ? getText("message.success") :
                getText("message.preferredreference_exists", new String[]{reference}));
        return SUCCESS;
    }

    @Override
    protected String performAction() throws Exception {
        log.info(this.getClass().getSimpleName() + ": oper=" + oper + ", id=" + id);

        if (oper.equals("edit")) {
            if (null != reference) {
                preferredReferenceService.updatePreferredReference(Long.valueOf(id), reference, comment);
            } else if(null!= translation ){
                preferredTranslationService.updatePreferredTranslation(Long.valueOf(id), ptId, languageId, translation, comment);
            }
        } else if (oper.equals("del")) {
            Collection<Long> ids = new ArrayList<Long>();
            for (String sid : id.split(",")) {
                ids.add(Long.valueOf(sid.trim()));
            }
            preferredReferenceService.deletePreferredReferences(ids);
        } else {
            throw new SystemError("Unknown oper: " + oper);
        }

        setStatus(0);
        setMessage(getText("message.success"));
        return SUCCESS;
    }

    public Long getPtId() {
        return ptId;
    }

    public void setPtId(Long ptId) {
        this.ptId = ptId;
    }

    public Long getLanguageId() {
        return languageId;
    }

    public void setLanguageId(Long languageId) {
        this.languageId = languageId;
    }

    public void setPreferredTranslationService(PreferredTranslationService preferredTranslationService) {
        this.preferredTranslationService = preferredTranslationService;
    }

    public void setPreferredReferenceService(PreferredReferenceService preferredReferenceService) {
        this.preferredReferenceService = preferredReferenceService;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOper() {
        return oper;
    }

    public void setOper(String oper) {
        this.oper = oper;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getTranslation() {
        return translation;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }
}
