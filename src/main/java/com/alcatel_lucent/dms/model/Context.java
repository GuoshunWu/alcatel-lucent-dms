package com.alcatel_lucent.dms.model;

import org.apache.commons.lang3.ArrayUtils;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;

import javax.persistence.*;
import java.util.Arrays;
import java.util.Collection;

//@Entity
@Table(name = "CONTEXT")
public class Context extends BaseEntity {

    public static final String DEFAULT = "[DEFAULT]";
    public static final String EXCLUSION = "[EXCLUSION]";
    public static final String LABEL = "[LABEL]";
    public static final String DICT = "[DICT]";
    public static final String APP = "[APP]";
    public static final String PROD = "[PROD]";

    public static final Collection<String> SPECIAL_CONTEXT_NAMES = Arrays.asList(DEFAULT, PROD, APP, DICT, LABEL);
    /**
     *
     */
    private static final long serialVersionUID = 1417207278044645451L;

    @Id
    @GeneratedValue(generator = "SEQ_GEN")
    @SequenceGenerator(name = "SEQ_GEN", sequenceName = "ID_CONTEXT", allocationSize = 20)
    @Column(name = "ID")
    @Override
    public Long getId() {
        return super.getId();
    }

    private String key;
    private String name;

    public Context() {
    }

    @Transient
    public boolean isNameIn(String... names) {
        return ArrayUtils.contains(names, name);
    }
    @Transient
    public boolean isSpecial() {
        return isNameIn(SPECIAL_CONTEXT_NAMES.toArray(ArrayUtils.EMPTY_STRING_ARRAY));
    }

    public Context(String name) {
        this.name = name;
    }



    public Context(String key, String name) {
        this(name);
        this.key = key;
    }

    @Field(index = Index.UN_TOKENIZED)
    @Column(name = "NAME")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return String.format("Context [name=%s]", name);
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
        Context other = (Context) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

    @Column(name = "CONTEXT_KEY")
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

}
