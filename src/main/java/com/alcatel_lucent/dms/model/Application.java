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
				"Application [name=%s, productVersion=%s, dictionaries=%s]",
				name, productVersion, dictionaries);
	}
}
