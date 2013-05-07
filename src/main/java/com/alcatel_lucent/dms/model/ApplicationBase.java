package com.alcatel_lucent.dms.model;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.Collection;


//@Entity
@Table(name = "APPLICATION_BASE")
@XmlRootElement
public class ApplicationBase extends BaseEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7168527218137875020L;


    @Id
    @GeneratedValue(generator = "SEQ_GEN")
    @SequenceGenerator(name = "SEQ_GEN", sequenceName = "ID_APPLICATION_BASE", allocationSize = 20)
    @Column(name = "ID")
    @Override
    public Long getId() {
        return super.getId();
    }

    @Column(name = "NAME")
    private String name;
    @XmlTransient
    private ProductBase productBase;
    @XmlTransient
    private Collection<Application> applications;
    @XmlTransient
    private Collection<DictionaryBase> dictionaryBases;

    private User owner;

    @ManyToOne
    @JoinColumn(name = "USER_ID")
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

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "PRODUCT_BASE_ID")
    public ProductBase getProductBase() {
        return productBase;
    }

    public void setProductBase(ProductBase productBase) {
        this.productBase = productBase;
    }

    @OneToMany(mappedBy = "base")
    public Collection<Application> getApplications() {
        return applications;
    }

    public void setApplications(Collection<Application> applications) {
        this.applications = applications;
    }

    @OneToMany(mappedBy = "applicationBase")
    public Collection<DictionaryBase> getDictionaryBases() {
        return dictionaryBases;
    }

    public void setDictionaryBases(Collection<DictionaryBase> dictionaryBases) {
        this.dictionaryBases = dictionaryBases;
    }
}
