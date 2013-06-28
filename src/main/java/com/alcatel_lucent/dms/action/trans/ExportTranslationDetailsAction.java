package com.alcatel_lucent.dms.action.trans;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;

import com.alcatel_lucent.dms.SystemError;
import com.alcatel_lucent.dms.action.BaseAction;
import com.alcatel_lucent.dms.service.TranslationService;
import org.apache.struts2.interceptor.SessionAware;

@SuppressWarnings("serial")
public class ExportTranslationDetailsAction extends BaseAction implements SessionAware {

    private InputStream inputStream;
    private Map<String, Object> session;

    private String translationDetailId;

    public void setTranslationDetailId(String translationDetailId) {
        this.translationDetailId = translationDetailId;
    }

    @Override
    @Action(results = {
            @Result(name = SUCCESS, type = "stream", params = {"contentType", "application/vnd.ms-excel", "contentDisposition", "attachment;filename=translation_details.xls"})
    })
    public String execute() {
        ByteArrayOutputStream output = (ByteArrayOutputStream) session.get(translationDetailId);
        session.remove(translationDetailId);
        inputStream = new ByteArrayInputStream(output.toByteArray());
        return SUCCESS;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public void setSession(Map<String, Object> stringObjectMap) {
        this.session = stringObjectMap;
    }
}
