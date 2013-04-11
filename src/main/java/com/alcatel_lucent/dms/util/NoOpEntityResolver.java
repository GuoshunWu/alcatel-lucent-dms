package com.alcatel_lucent.dms.util;

import java.io.StringReader;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

public class NoOpEntityResolver implements EntityResolver {

	public InputSource resolveEntity(String publicId, String systemId) {
		return new InputSource(new StringReader(""));
	}
}