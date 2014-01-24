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
    private String id;
    private Long languageId;

    private String reference;
    private String comment;
    private String translation;

    private PreferredTranslationService preferredTranslationService;
    private PreferredReferenceService preferredReferenceService;


    @Action("create-preferred-reference")
    public String createPreferredReference() throws Exception {
        PreferredReference preferredReference = preferredReferenceService.createPreferredReference(reference, comment);
        if (null == preferredReference) {
            setStatus(-1);
            setMessage(getText("message.preferredreference_exists", new String[]{reference}));
        } else {
            setStatus(0);
            setMessage(getText("message.success"));
        }
        return SUCCESS;
    }

    @Override
    protected String performAction() throws Exception {
        log.info(this.getClass().getSimpleName() + ": oper=" + oper + ", id=" + id);

        if (oper.equals("add")) {
            preferredTranslationService.createPreferredTranslation(reference, translation, comment, languageId);
        } else if (oper.equals("edit")) {
            preferredTranslationService.updatePreferredTranslation(Long.valueOf(id), reference, translation, comment);
        } else if (oper.equals("del")) {
            Collection<Long> ids = new ArrayList<Long>();
            for (String sid : id.split(",")) {
                ids.add(Long.valueOf(sid.trim()));
            }
            preferredTranslationService.deletePreferredTranslations(ids);
        } else {
            throw new SystemError("Unknown oper: " + oper);
        }

        setStatus(0);
        setMessage(getText("message.success"));
        return SUCCESS;
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
