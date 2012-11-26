package com.alcatel_lucent.dms.service;

import java.util.Collection;
import java.util.Map;

import com.alcatel_lucent.dms.BusinessWarning;

public class DeliveryReport {
	
	private int dictNum;	// number of dictionaries
	private int labelNum;	// number of labels
	private int translationNum;	// number of total translations
	private int translationWC;	// word count of total translations
	private int distinctTranslationNum;	// number of total distinct translations
	private int distinctTranslationWC;	// word count of total distinct translations
	private int untranslatedNum;	// number of untranslated strings
	private int untranslatedWC;		// word count of untranslated strings
	private int translatedNum;		// number of translated strings
	private int translatedWC;		// word count of translated strings
	private int matchedNum;			// number of auto-matched strings
	private int matchedWC;			// word count of auto-matched strings
	
	private Map<String, Collection<BusinessWarning>> warningMap;

	public Map<String, Collection<BusinessWarning>> getWarningMap() {
		return warningMap;
	}

	public void setWarningMap(Map<String, Collection<BusinessWarning>> warningMap) {
		this.warningMap = warningMap;
	}

	public int getDictNum() {
		return dictNum;
	}

	public void setDictNum(int dictNum) {
		this.dictNum = dictNum;
	}

	public int getLabelNum() {
		return labelNum;
	}

	public void setLabelNum(int labelNum) {
		this.labelNum = labelNum;
	}

	public int getTranslationNum() {
		return translationNum;
	}

	public void setTranslationNum(int translationNum) {
		this.translationNum = translationNum;
	}

	public int getDistinctTranslationNum() {
		return distinctTranslationNum;
	}

	public void setDistinctTranslationNum(int distinctTranslationNum) {
		this.distinctTranslationNum = distinctTranslationNum;
	}

	public int getTranslationWC() {
		return translationWC;
	}

	public void setTranslationWC(int translationWC) {
		this.translationWC = translationWC;
	}

	public int getDistinctTranslationWC() {
		return distinctTranslationWC;
	}

	public void setDistinctTranslationWC(int distinctTranslationWC) {
		this.distinctTranslationWC = distinctTranslationWC;
	}

	public int getUntranslatedWC() {
		return untranslatedWC;
	}

	public void setUntranslatedWC(int untranslatedWC) {
		this.untranslatedWC = untranslatedWC;
	}

	public int getUntranslatedNum() {
		return untranslatedNum;
	}

	public void setUntranslatedNum(int untranslatedNum) {
		this.untranslatedNum = untranslatedNum;
	}

	public int getTranslatedNum() {
		return translatedNum;
	}

	public void setTranslatedNum(int translatedNum) {
		this.translatedNum = translatedNum;
	}

	public int getTranslatedWC() {
		return translatedWC;
	}

	public void setTranslatedWC(int translatedWC) {
		this.translatedWC = translatedWC;
	}

	public int getMatchedNum() {
		return matchedNum;
	}

	public void setMatchedNum(int matchedNum) {
		this.matchedNum = matchedNum;
	}

	public int getMatchedWC() {
		return matchedWC;
	}

	public void setMatchedWC(int matchedWC) {
		this.matchedWC = matchedWC;
	}

}
