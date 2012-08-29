package com.alcatel_lucent.dms.model;

import java.util.Collection;

public class Product extends BaseEntity {

	private ProductBase base;
    private String version;
	private Collection<Application> applications;

    public ProductBase getBase() {
        return base;
    }

    public void setBase(ProductBase base) {
        this.base = base;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Collection<Application> getApplications() {
		return applications;
	}

	public void setApplications(Collection<Application> applications) {
		this.applications = applications;
	}

}
