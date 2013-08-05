package com.alcatel_lucent.dms;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Author: Allan YANG Date: 2008-11-13
 */
public class BusinessException extends RuntimeException {
    // general
    static public final int ACCESS_DENIED = 100;
    static public final int NESTED_ERROR = 101;
    // dict error
    static public final int INVALID_DCT_FILE = 200;
    static public final int DUPLICATE_DCT_NAME = 201;
    static public final int DCT_FILE_NOT_FOUND = 202;
    static public final int UNDEFINED_LANG_CODE = 203;
    static public final int NO_REFERENCE_TEXT = 204;
    static public final int UNKNOWN_LANG_CODE = 205;
    static public final int CHARSET_NOT_DEFINED = 208;
    static public final int NESTED_DCT_PARSE_ERROR = 209;
    static public final int NESTED_LABEL_ERROR = 210;
    static public final int INVALID_MDC_FILE = 211;
    static public final int INVALID_EXCEL_FILE = 212;
    static public final int FILE_TOO_LARGE = 213;
    static public final int NESTED_PROP_ERROR = 214;
    static public final int NESTED_PROP_FILE_ERROR = 215;
    static public final int INVALID_PROP_LINE = 216;
    static public final int NO_REFERENCE_LANGUAGE = 217;
    static public final int NESTED_LABEL_XML_ERROR = 219;
    static public final int NESTED_LABEL_XML_FILE_ERROR = 220;
    static public final int INVALID_XML_FILE = 221;
    static public final int UNKNOWN_XML_LANG_CODE = 222;
    static public final int TARGET_IS_NOT_DIRECTORY = 223;
    static public final int FAILED_TO_MKDIRS = 224;
    static public final int PREVIEW_DICT_ERRORS = 225;
    static public final int UNRECOGNIZED_DICT_FILE = 226;
    static public final int NESTED_VLE_LANG_FILE_ERROR = 227;
    static public final int VITAL_SUITE_REF_FILE_NOT_FOUND = 228;

    // VLEExcel error
    static public final int INVALID_VLE_DICT_FILE = 240;

    // OTC PC  error
    static public final int INVALID_OTC_EXCEL_DICT_FILE = 241;
    // OTC　Web error
    static public final int INVALID_OTC_WEB_DICT_FILE = 242;
    static public final int INVALID_PO_SYNTAX = 243;

    // application management
    static public final int CHARSET_NOT_FOUND = 301;
    static public final int INVALID_DICT_ENCODING = 302;
    static public final int INVALID_DICT_FORMAT = 303;
    static public final int DICTIONARIES_NOT_SAME_BASE = 304;
    static public final int DICTIONARY_NOT_IN_APP = 305;
    static public final int DUPLICATE_LANG_CODE = 306;
    static public final int DELIVERY_TIMEOUT = 307;
    static public final int APPLICATION_BASE_NOT_EMPTY = 308;
    static public final int PRODUCT_BASE_NOT_EMPTY = 309;
    static public final int PRODUCT_BASE_ALREADY_EXISTS = 310;
    static public final int APPLICATION_BASE_ALREADY_EXISTS = 311;
    static public final int APPLICATION_ALREADY_EXIST = 312;
    static public final int PRODUCT_NOT_EMPTY = 313;
    static public final int PRODUCT_ALREADY_EXISTS = 314;
    static public final int APPLICATION_ALREADY_IN_PRODUCT = 315;
    static public final int APPLICATION_NOT_EMPTY = 316;
    static public final int PRODUCT_CONTAINS_TASK = 317;
    static public final int LACK_DICT_NAME = 318;
    static public final int LACK_DICT_VERSION = 319;
    static public final int DUPLICATE_LABEL_KEY = 320;
    // task management
    static public final int INVALID_TASK_STATUS = 401;
    static public final int UNKNOWN_LANG_NAME = 402;
    static public final int INVALID_TASK_FILE = 403;
    static public final int EMPTY_TASK = 404;
    static public final int CANNOT_UPDATE_EXCLUSION = 405;
    static public final int INCONSISTENT_DATA = 406;
    static public final int APPLICATION_NOT_FOUND = 501;
    static public final int DICTIONARY_NOT_FOUND = 502;
    static public final int CONTEXT_NOT_FOUND = 503;
    static public final int FILE_NOT_FOUND = 504;
    public static final int TEXT_NOT_FOUND = 505;
    static public final int INVALID_CONTEXT_KEY = 506;
    static public final int INVALID_TRANSLATION_FILE = 507;
    
    // administration
    static public final int LANGUAGE_ALREADY_EXISTS = 601;
    static public final int LANGUAGE_IS_IN_USE = 602;
    static public final int CHARSET_ALREADY_EXISTS = 603;
    static public final int CHARSET_IS_IN_USE = 604;
    public static final int USER_ALREADY_EXISTS = 605;
    public static final int USER_NOT_FOUND = 606;

    public static final int GLOSSARY_ALREADY_EXISTS = 607;
    
    // context management
    public static final int TEXT_HAS_REFS = 701;
    
    
    private int errorCode;
    private String errorMessage;
    private Object[] parameters;
    private Collection<BusinessException> nested;


    public BusinessException(int errorCode, Object... params) {
        this.errorCode = errorCode;
        this.parameters = params;
    }

    public BusinessException(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }

    public void addNestedException(BusinessException exception) {
        if (nested == null) {
            nested = new ArrayList<BusinessException>();
        }
        nested.add(exception);
    }

    public boolean hasNestedException() {
        return nested != null && !nested.isEmpty();
    }

    /**
     * Get error message in specified locale.
     *
     * @param locale locale
     * @return error message
     */
    public String toString(Locale locale) {
        StringBuffer result = new StringBuffer();
        if (null != errorMessage) {
            result.append(errorMessage);
        } else {
            ResourceBundle bundle = ResourceBundle.getBundle("errors", locale);
            String localizedString = bundle.getString("business_exception_"
                    + errorCode);
            MessageFormat msgFmt = new MessageFormat(localizedString, locale);
            result.append(msgFmt.format(parameters));
        }
        if (nested != null) {
            for (BusinessException ne : nested) {
                result.append("\n").append(ne.toString(locale));
            }
        }
        return result.toString();
    }

    /**
     * Display error message including nested exceptions.
     */
    public String toString() {
        UserContext context = UserContext.getInstance();
        Locale locale = null != context ? context.getLocale() : Locale.getDefault();
        return toString(locale);
    }

    public Collection<BusinessException> getNested() {
        return nested;
    }

    public void setNested(Collection<BusinessException> nested) {
        this.nested = nested;
    }
    
    @Override
    public String getMessage() {
    	return toString();
    }
}
