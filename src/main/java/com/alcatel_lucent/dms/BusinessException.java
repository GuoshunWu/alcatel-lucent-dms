package com.alcatel_lucent.dms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Author: Allan YANG
 * Date: 2008-11-13
 */
public class BusinessException extends RuntimeException {
    int errorCode;
    String errorMessage;
    ArrayList<String> parameters = new ArrayList<String>();
    
    static public final BusinessException ACCESS_DENIED = new BusinessException(100);
    static public final BusinessException INVALID_DCT_FILE = new BusinessException(200);
    static public final BusinessException DUPLICATE_DCT_NAME = new BusinessException(201);
    static public final BusinessException DCT_FILE_NOT_FOUND = new BusinessException(202);
    static public final BusinessException UNDEFINED_LANG_CODE = new BusinessException(203);
    static public final BusinessException NO_REFERENCE_TEXT = new BusinessException(204);
    static public final BusinessException UNKNOWN_LANG_CODE = new BusinessException(205);
    static public final BusinessException DUPLICATE_LABEL_KEY = new BusinessException(206);
    static public final BusinessException DUPLICATE_LANG_CODE = new BusinessException(207);
    static public final BusinessException CHARSET_NOT_DEFINED = new BusinessException(208);
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
        StringBuffer result = new StringBuffer();
        if (errorCode != 0) {
            ResourceBundle bundle = ResourceBundle.getBundle("errors", locale);
            result.append(bundle.getString("business_exception_" + errorCode));
        }
        if (errorMessage != null) {
            result.append(" ").append(errorMessage);
        }
        int pos = 0;

        for (String param : parameters) {
        	pos = result.indexOf("%s", pos);
        	if (pos == -1) break;
        	result.replace(pos, pos + 2, param);
        	pos += param.length();
        }
        return result.toString();
    }

    public String toString() {
        return toString(UserContext.getInstance().getLocale());
    }
    
    public BusinessException param(String...strings) {
    	parameters.addAll(Arrays.asList(strings));
    	return this;
    }
}
