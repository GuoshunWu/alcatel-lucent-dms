package com.alcatel_lucent.dms.model;

public class DictionaryLanguage extends BaseEntity {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 9194788138624147936L;
	private Dictionary dictionary;
	private Language language;
	private String languageCode;
	private Charset charset;
	
	public Dictionary getDictionary() {
		return dictionary;
	}
	public void setDictionary(Dictionary dictionary) {
		this.dictionary = dictionary;
	}
	public Language getLanguage() {
		return language;
	}
	public void setLanguage(Language language) {
		this.language = language;
	}
	public String getLanguageCode() {
		return languageCode;
	}
	public void setLanguageCode(String languageCode) {
		this.languageCode = languageCode;
	}
	public Charset getCharset() {
		return charset;
	}
	public void setCharset(Charset charset) {
		this.charset = charset;
	}
	
	@Override
	public String toString() {
		return String
				.format("DictionaryLanguage [dictionary=%s, language=%s, languageCode=%s, charset=%s]",
						dictionary, language, languageCode, charset);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		DictionaryLanguage other = (DictionaryLanguage) obj;
		if (dictionary == null) {
			if (other.dictionary != null)
				return false;
		} else if (!dictionary.equals(other.dictionary))
			return false;
		if (languageCode == null) {
			if (other.languageCode != null)
				return false;
		} else if (!languageCode.equals(other.languageCode))
			return false;
		return true;
	}

	
	
}
