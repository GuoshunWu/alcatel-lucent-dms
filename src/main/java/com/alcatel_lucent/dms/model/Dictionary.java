package com.alcatel_lucent.dms.model;

import java.util.Collection;

public class Dictionary extends BaseEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4926531636839152201L;
	private String name;
	private String format;
	private String encoding;
	private String path;
	
	private Application application;
	private Collection<DictionaryLanguage> dictLanguages;
	private Collection<Label> labels;
	private boolean locked;
	
	
	
	public Dictionary() {
		super();
	}
	
	public Dictionary(String name, String format, String encoding, String path,
			Application application,
			Collection<DictionaryLanguage> dictLanguages,
			Collection<Label> labels, boolean locked) {
		this.name = name;
		this.format = format;
		this.encoding = encoding;
		this.path = path;
		this.application = application;
		this.dictLanguages = dictLanguages;
		this.labels = labels;
		this.locked = locked;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getFormat() {
		return format;
	}
	public void setFormat(String format) {
		this.format = format;
	}
	public String getEncoding() {
		return encoding;
	}
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public Application getApplication() {
		return application;
	}
	public void setApplication(Application application) {
		this.application = application;
	}
	public Collection<DictionaryLanguage> getDictLanguages() {
		return dictLanguages;
	}
	public void setDictLanguages(Collection<DictionaryLanguage> dictLanguages) {
		this.dictLanguages = dictLanguages;
	}
	public Collection<Label> getLabels() {
		return labels;
	}
	public void setLabels(Collection<Label> labels) {
		this.labels = labels;
	}
	public boolean isLocked() {
		return locked;
	}
	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	@Override
	public String toString() {
		return String
				.format("Dictionary [name=%s, format=%s, encoding=%s, path=%s, application=%s, dictLanguages=%s, labels=%s, locked=%s]",
						name, format, encoding, path, application,
						dictLanguages, labels, locked);
	}
	
}
