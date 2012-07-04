package com.alcatel_lucent.dms.model;

import java.util.Collection;

public class Text extends BaseEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4205533860022959713L;
	
	private Context context;
	private String reference;
	private Collection<Translation> translations;

	private int status;
	
	
	public Context getContext() {
		return context;
	}
	public void setContext(Context context) {
		this.context = context;
	}
	public String getReference() {
		return reference;
	}
	public void setReference(String reference) {
		this.reference = reference;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public Collection<Translation> getTranslations() {
		return translations;
	}
	public void setTranslations(Collection<Translation> translations) {
		this.translations = translations;
	}
	
}
