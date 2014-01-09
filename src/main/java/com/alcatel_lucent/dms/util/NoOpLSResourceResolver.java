package com.alcatel_lucent.dms.util;

import java.io.InputStream;
import java.io.Reader;

import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

/**
 * A dumb resolver for xml validator, in order to avoid external DTD retrieval
 * @author allany
 *
 */
public class NoOpLSResourceResolver implements LSResourceResolver {

	@Override
	public LSInput resolveResource(String type, String namespaceURI,
			String publicId, String systemId, String baseURI) {
		return new LSInput() {

			@Override
			public Reader getCharacterStream() {
				return null;
			}

			@Override
			public void setCharacterStream(Reader characterStream) {
				
			}

			@Override
			public InputStream getByteStream() {
				return null;
			}

			@Override
			public void setByteStream(InputStream byteStream) {
				
			}

			@Override
			public String getStringData() {
				return null;
			}

			@Override
			public void setStringData(String stringData) {
				
			}

			@Override
			public String getSystemId() {
				return null;
			}

			@Override
			public void setSystemId(String systemId) {
				
			}

			@Override
			public String getPublicId() {
				return null;
			}

			@Override
			public void setPublicId(String publicId) {
				
			}

			@Override
			public String getBaseURI() {
				return null;
			}

			@Override
			public void setBaseURI(String baseURI) {
				
			}

			@Override
			public String getEncoding() {
				return null;
			}

			@Override
			public void setEncoding(String encoding) {
				
			}

			@Override
			public boolean getCertifiedText() {
				return false;
			}

			@Override
			public void setCertifiedText(boolean certifiedText) {
				
			}
			
		};
	}

}
