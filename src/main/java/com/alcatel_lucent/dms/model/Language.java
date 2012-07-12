package com.alcatel_lucent.dms.model;

import java.util.Collection;

public class Language extends BaseEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5141417262905873634L;
	
	private String name;
	private Collection <ISOLanguageCode> isoCodes;
	private Collection<Charset> charsets;
	
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	
	public Collection<ISOLanguageCode> getIsoCodes() {
		return isoCodes;
	}
	
	public void setIsoCodes(Collection<ISOLanguageCode> isoCodes) {
		this.isoCodes = isoCodes;
	}
	public Collection<Charset> getCharsets() {
		return charsets;
	}
	
	public void setCharsets(Collection<Charset> charsets) {
		this.charsets = charsets;
	}
	@Override
	public String toString() {
		return String.format("Language [name=%s, isoCode=%s]",
				name, isoCodes);
	}
	
	
}
