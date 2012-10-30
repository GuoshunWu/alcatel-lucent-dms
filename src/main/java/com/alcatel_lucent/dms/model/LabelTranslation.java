package com.alcatel_lucent.dms.model;

import com.alcatel_lucent.dms.util.CharsetUtil;

public class LabelTranslation extends BaseEntity {

	private static final long serialVersionUID = 5749208472612761751L;

	private Label label;
	private Language language;
	private String origTranslation;
	private String annotation1;
	private String annotation2;
	
	private String warnings;
	private String languageCode;
	private int sortNo;

	/**
	 * Specify if translation is requested.
	 * Some dictionary can explicitly specify if translation is request
	 * If this flag is set, translation status would be forcibly set to "Not translated" or "Translated".
	 */
	private Boolean requestTranslation;

	/**
	 * Specify if the translation of this label should be retrieved from context dictionary.
	 * In some case, translation should not come from context dictionary.
	 */
	private boolean needTranslation;

	public void setLabel(Label label) {
		this.label = label;
	}
	public Label getLabel() {
		return label;
	}
	public void setLanguage(Language language) {
		this.language = language;
	}
	public Language getLanguage() {
		return language;
	}
	public void setOrigTranslation(String origTranslation) {
		this.origTranslation = origTranslation;
	}
	public String getOrigTranslation() {
		return origTranslation;
	}
	public void setNeedTranslation(boolean needTranslation) {
		this.needTranslation = needTranslation;
	}
	public boolean isNeedTranslation() {
		return needTranslation;
	}
	public void setWarnings(String warnings) {
		this.warnings = warnings;
	}
	public String getWarnings() {
		return warnings;
	}
	
	public boolean isValidText() {
		if (origTranslation != null) {
			return CharsetUtil.isValid(origTranslation, language.getName());
		}
		return true;
	}
	
	public boolean isValidText(String text) {
		if (text != null) {
			return CharsetUtil.isValid(text, language.getName());
		}
		return true;
	}

	public void setSortNo(int sortNo) {
		this.sortNo = sortNo;
	}
	public int getSortNo() {
		return sortNo;
	}
	public void setLanguageCode(String languageCode) {
		this.languageCode = languageCode;
	}
	public String getLanguageCode() {
		return languageCode;
	}
	public Boolean getRequestTranslation() {
		return requestTranslation;
	}
	public void setRequestTranslation(Boolean requestTranslation) {
		this.requestTranslation = requestTranslation;
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

}
