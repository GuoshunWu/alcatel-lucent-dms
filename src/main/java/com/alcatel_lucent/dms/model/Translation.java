package com.alcatel_lucent.dms.model;

public class Translation extends BaseEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4338889575255363014L;
	private Text text;
	private Language language;
	private String translation;
	
	
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
	
	
}
