package com.alcatel_lucent.dms.action.trans;

import com.alcatel_lucent.dms.UserContext;
import com.alcatel_lucent.dms.action.JSONAction;
import com.alcatel_lucent.dms.service.TranslationService;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.SessionAware;

import java.io.ByteArrayOutputStream;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: guoshunw
 * Date: 13-6-27
 * Time: 上午10:41
 */

@ParentPackage("dms-json")
@Result(type="json", params={"noCache","true","ignoreHierarchy","false","includeProperties","status, message, translationDetailId"})
public class GenerateTranslationDetailsAction extends JSONAction implements SessionAware {

    private TranslationService translationService;

    public void setTranslationService(TranslationService translationService) {
        this.translationService = translationService;
    }

    private Map<String, Object> session;

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
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        translationService.exportTranslations(toIdList(dict), toIdList(lang), output);
        translationDetailId = UserContext.getInstance().getUser().getName() + System.currentTimeMillis() + "_TRANS_DETAIL_ID";
        session.put(translationDetailId, output);

        setStatus(0);
        setMessage(getText("message.success"));
        return SUCCESS;
    }

    @Override
    public void setSession(Map<String, Object> stringObjectMap) {
        this.session = stringObjectMap;
    }
}
