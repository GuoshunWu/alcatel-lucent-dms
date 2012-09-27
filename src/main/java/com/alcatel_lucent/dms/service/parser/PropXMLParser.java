package com.alcatel_lucent.dms.service.parser;

import org.springframework.stereotype.Component;

@Component("propXMLParser")
public class PropXMLParser extends LabelXMLParser {
	
	protected String getRootName() {
		return "properties";
	}
	
	protected String getFormat() {
		return "XML properties";
	}
	
	protected String getXPath() {
		return "/properties/entry";
	}
	
	protected String getKeyAttributeName() {
		return "key";
	}


}
