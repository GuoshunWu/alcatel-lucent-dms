package com.alcatel_lucent.dms.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

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
				.format("Dictionary [name=%s, format=%s, encoding=%s, path=%s, application=%s, dictLanguagesSize=%d, labelsSize=%d, locked=%s]",
						name, format, encoding, path, application,
						dictLanguages == null ? 0 : dictLanguages.size(), 
						labels == null ? 0 : labels.size(), locked);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((application == null) ? 0 : application.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Dictionary other = (Dictionary) obj;
		if (application == null) {
			if (other.application != null)
				return false;
		} else if (!application.equals(other.application))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
	public Language getLanguageByCode(String langCode) {
		if (dictLanguages != null) {
			for (DictionaryLanguage dl : dictLanguages) {
				if (dl.getLanguageCode().equals(langCode)) {
					return dl.getLanguage();
				}
			}
		}
		return null;
	}
	
	public String getLanguageCode(Long languageId) {
		if (dictLanguages != null) {
			for (DictionaryLanguage dl : dictLanguages) {
				if (dl.getLanguage().getId().equals(languageId)) {
					return dl.getLanguageCode();
				}
			}
		}
		return null;
	}
	
	public HashSet<String> getAllLanguageCodes() {
		HashSet<String> result = new HashSet<String>();
		if (dictLanguages != null) {
			for (DictionaryLanguage dl : dictLanguages) {
				result.add(dl.getLanguageCode());
			}
		}
		return result;
	}
	
	public ArrayList<Language> getAllLanguages() {
		ArrayList<Language> result = new ArrayList<Language>();
		if (dictLanguages != null) {
			for (DictionaryLanguage dl : dictLanguages) {
				result.add(dl.getLanguage());
			}
		}
		return result;
	}

	public DictionaryLanguage getDictLanguage(Long languageId) {
		if (dictLanguages != null) {
			for (DictionaryLanguage dl : dictLanguages) {
				if (dl.getLanguage().getId().equals(languageId)) {
					return dl;
				}
			}
		}
		return null;
	}

	public Label getLabel(String key) {
		if (labels != null) {
			for (Label label : labels) {
				if (label.getKey().equals(key)) {
					return label;
				}
			}
		}
		return null;
	}
	
}
