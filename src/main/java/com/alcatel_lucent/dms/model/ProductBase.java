package com.alcatel_lucent.dms.model;

import java.util.Collection;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement
public class ProductBase extends BaseEntity {

    private String name = "Unknown";

    @XmlTransient
    private Collection<Product> products;

    private Collection<ApplicationBase> applicationBases;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Collection<Product> getProducts() {
        return products;
    }

    public void setProducts(Collection<Product> products) {
        this.products = products;
    }

    public Collection<ApplicationBase> getApplicationBases() {
        return applicationBases;
    }

    public void setApplicationBases(Collection<ApplicationBase> applicationBases) {
        this.applicationBases = applicationBases;
    }
}
