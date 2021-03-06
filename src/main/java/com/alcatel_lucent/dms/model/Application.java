package com.alcatel_lucent.dms.model;

import java.util.*;

import com.alcatel_lucent.dms.SpringContext;
import com.alcatel_lucent.dms.service.DictionaryService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.hibernate.annotations.*;
import org.hibernate.search.annotations.ContainedIn;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

//@Entity
@Table(name = "APPLICATION")
//@Indexed
public class Application extends BaseEntity {

    private static final long serialVersionUID = 7168527218137875020L;

    private ApplicationBase base;

    private String version;

    @Fetch(FetchMode.JOIN)
    private Collection<Dictionary> dictionaries;


    private Collection<Product> products;

    @IndexedEmbedded
    public Collection<Product> getProducts() {
        return products;
    }

    public void setProducts(Collection<Product> products) {
        this.products = products;
    }

    @Id
    @GeneratedValue(generator = "SEQ_GEN")
    @SequenceGenerator(name = "SEQ_GEN", sequenceName = "ID_APPLICATION", allocationSize = 20)
    @Column(name = "ID")
    @Override
    public Long getId() {
        return super.getId();
    }

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "APPLICATION_BASE_ID")
    public ApplicationBase getBase() {
        return base;
    }

    @Transient
    public String getName() {
        return base.getName();
    }

    @Transient
    public Integer getDictNum() {
        return dictionaries == null ? 0 : dictionaries.size();
    }

    @Transient
    public Collection getCell() {
        return Arrays.asList(getId(), getName(), version, getDictNum());
    }

    /**
     * Get translation status summary by language, used by front
     *
     * @return
     */
    @Transient
    public Map<String, int[]> getS() {
        return summaryCache;
    }

    public void setS(Map<Long, int[]> summary) {
        this.summaryCache = new HashMap<String, int[]>();
        if (summary == null) return;

        for (Map.Entry<Long, int[]> entry : summary.entrySet()) {
            summaryCache.put(entry.getKey() +"", entry.getValue());
        }
    }


    public void setBase(ApplicationBase base) {
        this.base = base;
    }

    @Column(name = "VERSION")
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }


    @ManyToMany()
    @JoinTable(
            name = "APPLICATION_DICTIONARY",
            joinColumns = @JoinColumn(name = "APPLICATION_ID"),
            inverseJoinColumns = @JoinColumn(name = "DICTIONARY_ID")
    )
    public Collection<Dictionary> getDictionaries() {
        return dictionaries;
    }

    public void setDictionaries(Collection<Dictionary> dictionaries) {
        this.dictionaries = dictionaries;
    }

    public void removeDictionary(Long dictId) {
        if (dictionaries != null && dictId != null) {
            for (Iterator<Dictionary> iterator = dictionaries.iterator(); iterator.hasNext(); ) {
                Dictionary dict = iterator.next();
                if (dict.getId().equals(dictId)) {
                    iterator.remove();
                }
            }
        }
    }

    @Transient
    public int getLabelNum() {
        DictionaryService dictService = (DictionaryService) SpringContext.getBean("dictionaryService");
        return dictService.getLabelNumByApp(getId());
    }

    private Map<String, int[]> summaryCache;

    @Transient
    public String getNameVersion() {
        return base == null ? null : base.getName() + " " + version;
    }

}
