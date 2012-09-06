package com.alcatel_lucent.dms.model;

import java.util.Collection;
import java.util.Iterator;

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
	
	public void removeApplication(Long appId) {
		if (applications != null && appId != null) {
			for (Iterator<Application> iterator = applications.iterator(); iterator.hasNext(); ) {
				Application app = iterator.next();
				if (app.getId().equals(appId)) {
					iterator.remove();
				}
			}
		}
	}


}
