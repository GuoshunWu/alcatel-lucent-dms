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
	private int sortNo;
	private String annotation1;
	private String annotation2;
	private String annotation3;
	private String annotation4;
	
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
	public void setSortNo(int sortNo) {
		this.sortNo = sortNo;
	}
	public int getSortNo() {
		return sortNo;
	}
	public String getAnnotation1() {
		return annotation1;
	}
	public void setAnnotation1(String annotation1) {
		this.annotation1 = annotation1;
	}
	public String getAnnotation2() {
		return annotation2;
	}
	public void setAnnotation2(String annotation2) {
		this.annotation2 = annotation2;
	}
	public String getAnnotation3() {
		return annotation3;
	}
	public void setAnnotation3(String annotation3) {
		this.annotation3 = annotation3;
	}
	public String getAnnotation4() {
		return annotation4;
	}
	public void setAnnotation4(String annotation4) {
		this.annotation4 = annotation4;
	}

}
