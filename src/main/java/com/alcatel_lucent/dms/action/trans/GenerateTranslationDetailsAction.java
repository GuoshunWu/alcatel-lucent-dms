package com.alcatel_lucent.dms.action.trans;

import com.alcatel_lucent.dms.UserContext;
import com.alcatel_lucent.dms.action.JSONAction;
import com.alcatel_lucent.dms.action.ProgressAction;
import com.alcatel_lucent.dms.action.ProgressQueue;
import com.alcatel_lucent.dms.service.TranslationService;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.SessionAware;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: guoshunw
 * Date: 13-6-27
 * Time: 上午10:41
 */

public class GenerateTranslationDetailsAction extends ProgressAction{

    private TranslationService translationService;

    public void setTranslationService(TranslationService translationService) {
        this.translationService = translationService;
    }

    private String dict;
    private String lang;

    public String getTranslationDetailId() {
        return translationDetailId;
    }

    public void setTranslationDetailId(String translationDetailId) {
        this.translationDetailId = translationDetailId;
    }

    private String translationDetailId;

    public String getDict() {
        return dict;
    }

    public void setDict(String dict) {
        this.dict = dict;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    @Override
    protected String performAction() throws Exception {
        DateFormatUtils.format(new Date(), "yyyyMMdd_HHmmss");

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ProgressQueue.setProgress("Generating...", -1);
        translationService.exportTranslations(toIdList(dict), toIdList(lang), output);
        translationDetailId = UserContext.getInstance().getUser().getName() + System.currentTimeMillis() + "_TRANS_DETAIL_ID";
        getSession().put(translationDetailId, output);

        ProgressQueue.setProgress("Complete", 100);
        setStatus(0);
        setMessage(translationDetailId);
        return SUCCESS;
    }

}
