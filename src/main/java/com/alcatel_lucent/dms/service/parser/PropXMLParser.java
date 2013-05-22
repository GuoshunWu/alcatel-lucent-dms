package com.alcatel_lucent.dms.service.parser;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.BusinessWarning;
import com.alcatel_lucent.dms.Constants;
import com.alcatel_lucent.dms.Constants.DictionaryFormat;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.DictionaryLanguage;
import com.alcatel_lucent.dms.model.Label;

@Component("propXMLParser")
public class PropXMLParser extends LabelXMLParser {
	
	@Override
	public DictionaryFormat getFormat() {
		return Constants.DictionaryFormat.XML_PROP;
	}
	
	protected String getRootName() {
		return "properties";
	}
	
	protected String getSecondNodeName() {
		return "entry";
	}
	
	protected String getXPath() {
		return "/properties/entry";
	}
	
	protected String getKeyAttributeName() {
		return "key";
	}

	@Override
	protected Collection<Label> readLabels(File file, Dictionary dict, DictionaryLanguage dl, Collection<BusinessWarning> warnings, BusinessException exceptions) {
		Collection<Label> labels = super.readLabels(file, dict, dl, warnings, exceptions);
		
		// proceed CMS specific case
		// if a label key ends with "$CONTEXTLABEL" or "$CONTEXTHELP", 
		// it is regarded as a description of corresponding label "$LABEL" or "$HELP"
		Collection<Label> result = new ArrayList<Label>();
		Map<String, Label> labelMap = new HashMap<String, Label>();
		for (Label label : labels) {
			labelMap.put(label.getKey(), label);
		}
		for (Label label : labels) {
			if (label.getKey().endsWith("$CONTEXTLABEL")) {
				String key = label.getKey().substring(0, label.getKey().length() - 13) + "$LABEL";
				Label baseLabel = labelMap.get(key);
				if (baseLabel != null) {
					baseLabel.setDescription(label.getReference());
				} else {	// if base label is not found, proceed the label in normal case
					result.add(label);
				}
			} else if (label.getKey().endsWith("$CONTEXTHELP")) {
				String key = label.getKey().substring(0, label.getKey().length() - 12) + "$HELP";
				Label baseLabel = labelMap.get(key);
				if (baseLabel != null) {
					baseLabel.setDescription(label.getReference());
				} else {	// if base label is not found, proceed the label in normal case
					result.add(label);
				}
			} else {
				result.add(label);
			}
		}
		return result;
	}
}
