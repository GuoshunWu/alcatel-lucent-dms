package com.alcatel_lucent.dms.action.app;

import com.alcatel_lucent.dms.action.ProgressAction;
import com.alcatel_lucent.dms.service.DictionaryService;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collection;

@SuppressWarnings("serial")
public class CapitalizeAction extends ProgressAction {

    private DictionaryService dictionaryService;

    private String dict;    // dict id list
    private String label;    // label id list, one of "dict" and "label" must be provided
    private String lang;    // language id list, empty for reference only
    private int style;        // capitalization style, see constant definition above


    @Override
    protected String performAction() throws Exception {
        Collection<Long> languages = lang == null ? null : toIdList(lang);
        if (StringUtils.isNotBlank(label)) {
            dictionaryService.changeLabelCapitalization(toIdList(label), languages, style);
        } else {
            dictionaryService.changeDictCapitalization(toIdList(dict), languages, style);
        }
        setStatus(0);
        setMessage(getText("message.operationSuccess", Arrays.asList(getText("message.capitalization"))));
        return SUCCESS;
    }


    public String getDict() {
        return dict;
    }


    public void setDict(String dict) {
        this.dict = dict;
    }


    public String getLabel() {
        return label;
    }


    public void setLabel(String label) {
        this.label = label;
    }


    public String getLang() {
        return lang;
    }


    public void setLang(String lang) {
        this.lang = lang;
    }


    public int getStyle() {
        return style;
    }


    public void setStyle(int style) {
        this.style = style;
    }


    public DictionaryService getDictionaryService() {
        return dictionaryService;
    }


    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

}
