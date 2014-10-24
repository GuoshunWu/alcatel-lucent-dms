package com.alcatel_lucent.dms;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class BusinessWarning implements ValidationInfo{
    public static final int UNCLOSED_QUOTA = 200;
    public static final int UNCLOSED_LABEL = 201;
    public static final int UNCLOSED_LABEL_ENTRY = 202;
    public static final int EXCEED_MAX_LENGTH = 203;
    public static final int INVALID_TEXT = 204;
    public static final int SUSPICIOUS_CHARACTER = 205;
    static public final int DUPLICATE_LABEL_KEY = 206;
    static public final int DUPLICATE_LANG_CODE = 207;
    public static final int DUPLICATE_REFERENCE = 208;
    public static final int LABEL_KEY_BLANK = 209;
    public static final int EXCEL_CELL_EVALUATION_FAIL = 210;
    public static final int LABEL_TRANS_BLANK = 211;
    public static final int INVALID_LINE_IN_VITAL_SUITE_FILE = 212;

    public static final int ANDROID_LABEL_KEY_BLANK = 213;

    public static final int OTC_WEB_DEFINE_NOT_FOUND = 223;

    public static final int TS_REFERENCE_UNFINISHED = 301;

    public static final int PARAMETERS_INCORRECT = 401;
    public static final int BR_INCONSISTENT = 402;

    public static final int MORE = 999;


    private int warningCode;
    private Object[] parameters;

    public BusinessWarning(int code, Object... parameters) {
        this.warningCode = code;
        this.parameters = parameters;
    }

    public String toString(Locale locale) {
        ResourceBundle bundle = ResourceBundle.getBundle("warnings", locale);
        String localizedString = bundle.getString("business_warning_"
                + warningCode);
        MessageFormat msgFmt = new MessageFormat(localizedString, locale);
        return msgFmt.format(parameters);
    }

    @JsonProperty("code")
    public int getCode(){
        return warningCode;
    }

    @Override
    public Long getId() {
        return toString().hashCode() + System.currentTimeMillis();
    }

    public String getMessage(){
        return toString();
    }

    public String toString() {
        UserContext context = UserContext.getInstance();
        Locale locale = null != context ? context.getLocale() : Locale.getDefault();
        return toString(locale);
    }
}
