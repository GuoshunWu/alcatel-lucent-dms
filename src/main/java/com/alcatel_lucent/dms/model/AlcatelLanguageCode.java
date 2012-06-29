package com.alcatel_lucent.dms.model;

import java.io.Serializable;

public class AlcatelLanguageCode implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7050518854634982880L;

	private String code;
	private Language language;
	private boolean defaultCode;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Language getLanguage() {
		return language;
	}

	public void setLanguage(Language language) {
		this.language = language;
	}

	public boolean isDefaultCode() {
		return defaultCode;
	}

	public void setDefaultCode(boolean defaultCode) {
		this.defaultCode = defaultCode;
	}

}
