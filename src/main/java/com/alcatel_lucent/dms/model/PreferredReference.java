package com.alcatel_lucent.dms.model;

import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.util.Collection;
import java.util.Date;

/**
 * Created by guoshunw on 14-1-22.
 */

@Entity
@Table(name = "PREFERRED_REFERENCE")
public class PreferredReference extends BaseEntity{
    private String reference;
    private String comment;

    private User creator;
    private Date createTime = new Date();

    private Collection<PreferredTranslation> preferredTranslations;

    @Column(name = "REFERENCE", length = 1024)
    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
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

    @OneToMany(mappedBy = "preferredReference")
    @OnDelete(action = OnDeleteAction.CASCADE)
    public Collection<PreferredTranslation> getPreferredTranslations() {
        return preferredTranslations;
    }

    public void setPreferredTranslations(Collection<PreferredTranslation> preferredTranslations) {
        this.preferredTranslations = preferredTranslations;
    }

    @Override
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "IdPreferredReferenceGenerator")
    @SequenceGenerator(name = "IdPreferredReferenceGenerator",  sequenceName = "ID_PREFERRED_REFERENCE")
    public Long getId() {
        return super.getId();
    }

    @Column(name = "COMMENT")
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    /*
    * Transient object for front end
    * */
    private PreferredTranslation pt;
    @Transient
    public PreferredTranslation getPt() {
        return pt;
    }

    public void setPt(PreferredTranslation pt) {
        this.pt = pt;
    }
}
