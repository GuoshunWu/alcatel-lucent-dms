package com.alcatel_lucent.dms.model;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by guoshunw on 14-1-16.
 */

@Entity
@Table(name = "PREFERRED_TRANSLATION")
@org.hibernate.annotations.Entity(dynamicInsert = true)
public class PreferredTranslation extends BaseEntity {

    private PreferredReference preferredReference;

    private User creator;
    private Date createTime = new Date();

    private Language language;

    private String translation;
    private String comment;

    @Override
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "IdPreferredTranslationGenerator")
    @SequenceGenerator(name = "IdPreferredTranslationGenerator",  sequenceName = "ID_PREFERRED_TRANSLATION")
    public Long getId() {
        return super.getId();
    }

    @ManyToOne
    @JoinColumn(name = "CREATOR", updatable = false)
    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATE_TIME", updatable = false)
    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @ManyToOne
    @JoinColumn(name = "LANGUAGE", updatable = false)
    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    @Column(name = "TRANSLATION", length = 2048)
    public String getTranslation() {
        return translation;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }

    @Column(name = "COMMENT", length = 4096)
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @ManyToOne
    @JoinColumn(name = "PREFERRED_REFERENCE")
    public PreferredReference getPreferredReference() {
        return preferredReference;
    }

    public void setPreferredReference(PreferredReference preferredReference) {
        this.preferredReference = preferredReference;
    }
}
