package com.alcatel_lucent.dms.model;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.util.Collection;


//@Entity
@Table(name = "LANGUAGE")
public class Language extends BaseEntity {

    private static final long serialVersionUID = -5141417262905873634L;

    private String name;
    private Collection<ISOLanguageCode> isoCodes;
    private String defaultCharset;


    @Id
    @GeneratedValue(generator = "SEQ_GEN")
    @SequenceGenerator(name = "SEQ_GEN", sequenceName = "ID_LANGUAGE", allocationSize = 20)
    @Column(name = "ID")
    @Override
    public Long getId() {
        return super.getId();
    }

    @Column(name = "NAME")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @OneToMany
    public Collection<ISOLanguageCode> getIsoCodes() {
        return isoCodes;
    }

    public void setIsoCodes(Collection<ISOLanguageCode> isoCodes) {
        this.isoCodes = isoCodes;
    }

    @Override
    public String toString() {
        return String.format("Language [name=%s, isoCode=%s]",
                name, isoCodes);
    }

    public void setDefaultCharset(String defaultCharset) {
        this.defaultCharset = defaultCharset;
    }

    @Column(name = "DEFAULT_CHARSET")
    public String getDefaultCharset() {
        return defaultCharset;
    }


}
