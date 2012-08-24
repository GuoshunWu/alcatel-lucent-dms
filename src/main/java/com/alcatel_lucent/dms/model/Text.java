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

	public static final int STATUS_NOT_TRANSLATED = 0;
	public static final int STATUS_IN_PROGRESS = 1;
	public static final int STATUS_TRANSLATED = 2;

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

	@Override
	public String toString() {
		return String.format("Text [context=%s, reference=%s, status=%s]",
				context, reference, status);
	}

	public Translation getTranslation(Long languageId) {
		if (translations != null) {
			for (Translation trans : translations) {
				if (trans.getLanguage().getId().equals(languageId)) {
					return trans;
				}
			}
		}
		return null;
	}

}
