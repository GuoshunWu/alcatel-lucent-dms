package com.alcatel_lucent.dms.service.parser;

import org.springframework.stereotype.Component;

import com.alcatel_lucent.dms.Constants;

@Component("propXMLParser")
public class PropXMLParser extends LabelXMLParser {
	
	protected String getRootName() {
		return "properties";
	}
	
	protected String getSecondNodeName() {
		return "entry";
	}
	
	protected String getFormat() {
		return Constants.DictionaryFormat.XML_PROP.toString();
	}
	
	protected String getXPath() {
		return "/properties/entry";
	}
	
	protected String getKeyAttributeName() {
		return "key";
	}


}
