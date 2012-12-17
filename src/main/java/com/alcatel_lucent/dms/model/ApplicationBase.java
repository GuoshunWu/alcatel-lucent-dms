package com.alcatel_lucent.dms.model;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.Collection;

@XmlRootElement
public class ApplicationBase extends BaseEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7168527218137875020L;

    private String name;
    @XmlTransient
    private ProductBase productBase;
    @XmlTransient
    private Collection<Application> applications;
    @XmlTransient
    private Collection<DictionaryBase> dictionaryBases;

    private User owner;

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

    public ProductBase getProductBase() {
        return productBase;
    }

    public void setProductBase(ProductBase productBase) {
        this.productBase = productBase;
    }

    public Collection<Application> getApplications() {
        return applications;
    }

    public void setApplications(Collection<Application> applications) {
        this.applications = applications;
    }

    public Collection<DictionaryBase> getDictionaryBases() {
        return dictionaryBases;
    }

    public void setDictionaryBases(Collection<DictionaryBase> dictionaryBases) {
        this.dictionaryBases = dictionaryBases;
    }
}
