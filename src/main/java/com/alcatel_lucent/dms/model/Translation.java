package com.alcatel_lucent.dms.model;

import com.alcatel_lucent.dms.util.CharsetUtil;

public class Translation extends BaseEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4338889575255363014L;
	
	public static final int STATUS_UNTRANSLATED = 0;
	public static final int STATUS_IN_PROGRESS = 1;
	public static final int STATUS_TRANSLATED = 2;
	
	private Text text;
	private Language language;
	private String translation;
	private String warnings;
    private int status;

    public Text getText() {
		return text;
	}
	public void setText(Text text) {
		this.text = text;
	}
	public Language getLanguage() {
		return language;
	}
	public void setLanguage(Language language) {
		this.language = language;
	}
	public String getTranslation() {
		return translation;
	}
	public void setTranslation(String translation) {
		this.translation = translation;
	}
	@Override
	public String toString() {
		return String.format(
				"Translation [text=%s, language=%s, translation=%s]", text,
				language, translation);
	}
	
	public boolean isValidText() {
		if (translation != null) {
			return CharsetUtil.isValid(translation, language.getName());
		}
		return true;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getStatus() {
		return status;
	}
	public void setWarnings(String warnings) {
		this.warnings = warnings;
	}
	public String getWarnings() {
		return warnings;
	}
	
}
