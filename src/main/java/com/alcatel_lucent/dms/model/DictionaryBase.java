package com.alcatel_lucent.dms.model;

import java.util.Collection;

public class DictionaryBase extends BaseEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4926531636839152201L;
	private String name;
	private String format;
	private String encoding;
	private String path;
	
	private ApplicationBase applicationBase;
	private Collection<Dictionary> dictionaries;

	private boolean locked;


    public Collection<Dictionary> getDictionaries() {
        return dictionaries;
    }

    public void setDictionaries(Collection<Dictionary> dictionaries) {
        this.dictionaries = dictionaries;
    }

    public DictionaryBase() {
		super();
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
	public ApplicationBase getApplicationBase() {
		return applicationBase;
	}
	public void setApplicationBase(ApplicationBase applicationBase) {
		this.applicationBase = applicationBase;
	}


	public boolean isLocked() {
		return locked;
	}
	public void setLocked(boolean locked) {
		this.locked = locked;
	}

}
