package com.alcatel_lucent.dms;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class BusinessWarning {
	private int warningCode;
	private Object[] parameters;
	
	public static final int UNCLOSED_QUOTA = 200;
	public static final int EXCEED_MAX_LENGTH = 201;
	public static final int INVALID_TEXT = 202;
	static public final int DUPLICATE_LABEL_KEY = 206;
	
	public BusinessWarning(int code, Object...parameters) {
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
