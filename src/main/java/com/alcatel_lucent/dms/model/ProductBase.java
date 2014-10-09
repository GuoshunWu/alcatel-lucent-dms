package com.alcatel_lucent.dms.model;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;

import java.util.Collection;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

//@Entity
@Table(name = "PRODUCT_BASE")
@XmlRootElement
public class ProductBase extends BaseEntity {

    private String name = "Unknown";
    private User owner;

    @ManyToOne
    @JoinColumn(nullable = false, name = "USER_ID")
    public User getOwner() {
        return owner;
    }

    @Id
    @GeneratedValue(generator = "SEQ_GEN")
    @SequenceGenerator(name = "SEQ_GEN", sequenceName = "ID_PRODUCT_BASE", allocationSize = 20)
    @Column(name = "ID")
    @Override
    public Long getId() {
        return super.getId();
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    @XmlTransient
    private Collection<Product> products;

    private Collection<ApplicationBase> applicationBases;

    @Field(index = Index.UN_TOKENIZED)
    @Column(name = "NAME", nullable = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @OneToMany(mappedBy = "base")
    public Collection<Product> getProducts() {
        return products;
    }

    public void setProducts(Collection<Product> products) {
        this.products = products;
    }

    @OneToMany
   public Collection<ApplicationBase> getApplicationBases() {
        return applicationBases;
    }

    public void setApplicationBases(Collection<ApplicationBase> applicationBases) {
        this.applicationBases = applicationBases;
    }
}
