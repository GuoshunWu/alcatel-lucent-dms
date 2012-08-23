package com.alcatel_lucent.dms.model;

import com.alcatel_lucent.dms.util.CharsetUtil;

public class Translation extends BaseEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4338889575255363014L;
	private Text text;
	private Language language;
	private String translation;
    private String memo;

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }


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
	
}
