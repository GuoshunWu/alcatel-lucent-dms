package com.alcatel_lucent.dms.model;

import java.util.Collection;

public class Dictionary extends BaseEntity {
	private String name;
	private String format;
	private String encoding;
	private String path;
	private Application application;
	private Collection<DictionaryLanguage> dictLanguages;
	private Collection<Label> labels;
	private boolean locked;
}
