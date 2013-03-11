package com.alcatel_lucent.dms.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.alcatel_lucent.dms.BusinessWarning;
import com.alcatel_lucent.dms.model.Context;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.DictionaryLanguage;
import com.alcatel_lucent.dms.model.Label;
import com.alcatel_lucent.dms.model.LabelTranslation;
import com.alcatel_lucent.dms.model.Text;
import com.alcatel_lucent.dms.model.Translation;
import com.alcatel_lucent.dms.util.Util;

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
	
	private HashSet<String> dictNames = new HashSet<String>();
	// key=<context_name>~~<langId>~~<reference>
	private HashSet<String> translatedSet = new HashSet<String>();
	private HashSet<String> untranslatedSet = new HashSet<String>();
	
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

	public void addData(Context context, Dictionary dict,
			Collection<Label> labels, Map<String, Text> textMap) {
		if (!dictNames.contains(dict.getName())) {
			dictNum++;
			dictNames.add(dict.getName());
		}
		labelNum += labels.size();
		if (dict.getDictLanguages() == null || dict.getDictLanguages().isEmpty()) return;
		int languageNum = 0;
		for (DictionaryLanguage dl : dict.getDictLanguages()) {
			if (dl.getLanguage().getId() != 1) languageNum++;
		}
		translationNum += labels.size() * languageNum;
		for (Label label : labels) {
			Text text = textMap.get(label.getReference());
			int labelWC = Util.countWords(label.getReference());
			translationWC += labelWC * languageNum;
			for (DictionaryLanguage dl : dict.getDictLanguages()) {
				if (dl.getLanguage().getId() == 1) continue;	// ignore English
				LabelTranslation lt = label.getOrigTranslation(dl.getLanguageCode());
				Translation trans = text.getTranslation(dl.getLanguage().getId());
				if (!label.getContext().getName().equals(Context.EXCLUSION) &&
						(lt == null || lt.getOrigTranslation().equals(label.getReference()) && lt.isNeedTranslation()) && 
						trans != null && trans.getStatus() == Translation.STATUS_TRANSLATED) {
					matchedNum++;
					matchedWC += labelWC;
				}
				if (lt != null && !lt.isNeedTranslation()) {
					translatedNum++;
					translatedWC += labelWC;
				} else {
					String key = label.getContext().getKey() + "~~" + dl.getLanguage().getId() + "~~" + label.getReference();
					boolean translated = !label.getContext().getName().equals(Context.EXCLUSION) &&
							trans != null && trans.getStatus() == Translation.STATUS_TRANSLATED;
					if (translated && !translatedSet.contains(key)) {
						translatedSet.add(key);
						translatedNum++;
						translatedWC += labelWC;
						if (untranslatedSet.contains(key)) {	// if already in untranslatedSet, remove it
							untranslatedSet.remove(key);
							untranslatedNum--;
							untranslatedWC -= labelWC;
						}
					} else if (!translated && !translatedSet.contains(key) && !untranslatedSet.contains(key)) {
						untranslatedSet.add(key);
						untranslatedNum++;
						untranslatedWC += labelWC;
					}
				}
				distinctTranslationNum = translatedNum + untranslatedNum;
				distinctTranslationWC = translatedWC + untranslatedWC;
			}
		}
	}
	
	public String toString() {
		StringBuffer s = new StringBuffer("DELIVERY REPORT\n\n");
		s.append("Dictionaries: ").append(dictNum).append("\n");
		s.append("Labels: ").append(labelNum).append("\n");
		s.append("Total translations: ").append(translationNum).append(" (").append(translationWC).append(" in words)\n");
		s.append("Distinct translations: ").append(distinctTranslationNum).append(" (").append(distinctTranslationWC).append(" in words)\n");
		if (translationNum > 0 && translationWC > 0) {
			s.append("Merge rate: ")
					.append(Math.round((translationNum - distinctTranslationNum) * 10000.0 / translationNum) / 100.0)
					.append("% (")
					.append(Math.round((translationWC - distinctTranslationWC) * 10000.0 / translationWC) / 100.0)
					.append("% in words)\n");
		}
		s.append("\n");
		s.append("Translated: ").append(translatedNum).append(" (").append(translatedWC).append(" in words)\n");
		s.append("Not translated: ").append(untranslatedNum).append(" (").append(untranslatedWC).append(" in words)\n");
		if (distinctTranslationNum > 0 && distinctTranslationWC > 0) {
			s.append("Translated rate: ")
			.append(Math.round(translatedNum * 10000.0 / distinctTranslationNum) / 100.0)
			.append("% (")
			.append(Math.round(translatedWC * 10000.0 / distinctTranslationWC) / 100.0)
			.append("% in words)\n");
		}
		s.append("\n");
		s.append("Matched translation: ").append(matchedNum).append(" (").append(matchedWC).append(" in words)\n");
		if (translationNum > 0 && translationWC > 0) {
			s.append("Match rate: ")
					.append(Math.round(matchedNum * 10000.0 / translationNum) / 100.0)
					.append("% (")
					.append(Math.round(matchedWC * 10000.0 / translationWC) / 100.0)
					.append("% in words)\n");
		}
		return s.toString();
	}
	
	public String toHTML() {
		return "<div style='text-align:left'>" + toString().replace("\n", "<br/>") + "</div>";
	}
	
}
