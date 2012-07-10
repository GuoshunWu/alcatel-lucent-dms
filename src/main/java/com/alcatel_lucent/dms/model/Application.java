package com.alcatel_lucent.dms.model;

import java.util.Collection;

public class Application extends BaseEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7168527218137875020L;

	private String name;
	private ProductVersion productVersion;
	private Collection<Dictionary> dictionaries;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ProductVersion getProductVersion() {
		return productVersion;
	}

	public void setProductVersion(ProductVersion productVersion) {
		this.productVersion = productVersion;
	}

	public Collection<Dictionary> getDictionaries() {
		return dictionaries;
	}

	public void setDictionaries(Collection<Dictionary> dictionaries) {
		this.dictionaries = dictionaries;
	}

	@Override
	public String toString() {
		return String.format(
				"Application [name=%s, productVersion=%s, dictionariesSize=%d]",
				name, productVersion, dictionaries.size());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((productVersion == null) ? 0 : productVersion.hashCode());
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
		Application other = (Application) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (productVersion == null) {
			if (other.productVersion != null)
				return false;
		} else if (!productVersion.equals(other.productVersion))
			return false;
		return true;
	}

	
}
