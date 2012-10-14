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
		return Constants.DICT_FORMAT_XML_PROP;
	}
	
	protected String getXPath() {
		return "/properties/entry";
	}
	
	protected String getKeyAttributeName() {
		return "key";
	}


}
