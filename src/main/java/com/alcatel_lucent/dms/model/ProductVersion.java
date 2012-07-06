package com.alcatel_lucent.dms.model;

import java.util.Collection;

public class ProductVersion extends BaseEntity {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6344477861555040993L;

	private String name;
	private Product product;
	private Collection<Application> applications;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Product getProduct() {
		return product;
	}
	public void setProduct(Product product) {
		this.product = product;
	}

	public Collection<Application> getApplications() {
		return applications;
	}
	public void setApplications(Collection<Application> applications) {
		this.applications = applications;
	}
	@Override
	public String toString() {
		return String.format(
				"ProductVersion [name=%s, product=%s, applications=%s]", name,
				product, applications);
	}


}
