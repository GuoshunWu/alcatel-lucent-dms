package com.alcatel_lucent.dms;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class BusinessWarning {
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

    public String toString() {
        return toString(UserContext.getInstance().getLocale());
    }
}
