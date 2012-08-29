package com.alcatel_lucent.dms.model;

import com.alcatel_lucent.dms.util.CharsetUtil;

public class LabelTranslation extends BaseEntity {

	private static final long serialVersionUID = 5749208472612761751L;

	private Label label;
	private Language language;
	private String origTranslation;
	private boolean needTranslation;
	private String warnings;
	
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

}
