package com.alcatel_lucent.dms.model;

import java.util.Collection;

public class Product extends BaseEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5972950266170006869L;
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Collection<ProductVersion> getVersions() {
		return versions;
	}

	public void setVersions(Collection<ProductVersion> versions) {
		this.versions = versions;
	}

	private Collection<ProductVersion> versions;

	@Override
	public String toString() {
		return String.format("Product [name=%s, versionsSize=%d]", name, versions.size());
	}

}
