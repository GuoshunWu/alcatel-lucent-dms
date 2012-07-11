package com.alcatel_lucent.dms;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Author: Allan YANG
 * Date: 2008-11-13
 */
public class BusinessException extends RuntimeException {
    int errorCode;
    String errorMessage;
    
    static public final BusinessException ACCESS_DENIED = new BusinessException(100);
    static public final BusinessException INVALID_DCT_FILE = new BusinessException(200);
    static public final BusinessException DUPLICATE_DCT_NAME = new BusinessException(201);
    static public final BusinessException DCT_FILE_NOT_FOUND = new BusinessException(202);
    static public final BusinessException CHARSET_NOT_FOUND = new BusinessException(301);
    static public final BusinessException LANGUAGE_NOT_FOUND = new BusinessException(401);
    static public final BusinessException APPLICATION_NOT_FOUND = new BusinessException(501);
    static public final BusinessException DICTIONARY_NOT_FOUND = new BusinessException(502);
    
    public BusinessException(int errorCode) {
        this.errorCode = errorCode;
    }

    public BusinessException(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public BusinessException(int errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    /**
     * Get error message given by resource file.
     * @param locale locale
     * @return error message
     */
    public String toString(Locale locale) {
        String result = "";
        if (errorCode != 0) {
            ResourceBundle bundle = ResourceBundle.getBundle("errors", locale);
            result += bundle.getString("business_exception_" + errorCode);
        }
        if (errorMessage != null) {
            result += " " + errorMessage;
        }
        return result;
    }

    public String toString() {
        return toString(UserContext.getInstance().getLocale());
    }
}
