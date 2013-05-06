package com.alcatel_lucent.dms.model;

import org.hibernate.annotations.ManyToAny;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.util.Collection;
import java.util.Iterator;

@Entity
@Table(name = "PRODUCT")
public class Product extends BaseEntity {

	private ProductBase base;
    private String version;
	private Collection<Application> applications;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name="PRODUCT_BASE_ID")
    public ProductBase getBase() {
        return base;
    }

    @Id
    @GeneratedValue(generator = "SEQ_GEN")
    @SequenceGenerator(name = "SEQ_GEN", sequenceName = "ID_PRODUCT", allocationSize = 20)
    @Column(name = "ID")
    @Override
    public Long getId() {
        return super.getId();
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

    @ManyToMany
    @JoinTable(name = "PRODUCT_APPLICATION")
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

	public String getName() {
		return base == null ? null : base.getName();
	}

}
