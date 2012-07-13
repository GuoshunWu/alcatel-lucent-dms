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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
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
		Product other = (Product) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
}
