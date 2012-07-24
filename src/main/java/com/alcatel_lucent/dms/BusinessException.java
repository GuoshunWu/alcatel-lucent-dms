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
	private int errorCode;
	private String errorMessage;
	private Object[] parameters;
	private Collection<BusinessException> nested;

	static public final int ACCESS_DENIED = 100;
	static public final int INVALID_DCT_FILE = 200;
	static public final int DUPLICATE_DCT_NAME = 201;
	static public final int DCT_FILE_NOT_FOUND = 202;
	static public final int UNDEFINED_LANG_CODE = 203;
	static public final int NO_REFERENCE_TEXT = 204;
	static public final int UNKNOWN_LANG_CODE = 205;
	static public final int DUPLICATE_LANG_CODE = 207;
	static public final int CHARSET_NOT_DEFINED = 208;
	static public final int NESTED_DCT_PARSE_ERROR = 209;
	static public final int NESTED_LABEL_ERROR = 210;
	static public final int CHARSET_NOT_FOUND = 301;
	static public final int LANGUAGE_NOT_FOUND = 401;
	static public final int APPLICATION_NOT_FOUND = 501;
	static public final int DICTIONARY_NOT_FOUND = 502;

	public BusinessException(int errorCode, Object... params) {
		this.errorCode = errorCode;
		this.parameters = params;
	}

	public Object[] getParameters() {
		return parameters;
	}

	public void setParameters(Object[] parameters) {
		this.parameters = parameters;
	}

	public BusinessException(String errorMessage) {
		this.errorMessage = errorMessage;
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
		return toString(UserContext.getInstance().getLocale());
	}

	public void setNested(Collection<BusinessException> nested) {
		this.nested = nested;
	}

	public Collection<BusinessException> getNested() {
		return nested;
	}
}
