package com.alcatel_lucent.dms.model;

import javax.persistence.*;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: guoshunw
 * Date: 13-7-24
 * Time: 上午11:37
 * To change this template use File | Settings | File Templates.
 */

@Entity
@Table(name = "GLOSSARY")
@org.hibernate.annotations.Entity(dynamicInsert = true)
public class Glossary {

    private User creator;
    private Date createTime = new Date();
    private String text;
    private boolean dirty = true;


    public Glossary() {
    }

    public Glossary(String text) {
        this.text = text;
    }

    public Glossary(String text, User creator) {
        this(text);
        this.creator = creator;
    }

    @Column(name = "DIRTY", nullable = false)
    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    @ManyToOne
    @JoinColumn(name = "creator", updatable = false)
    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    @Temporal(TemporalType.TIMESTAMP)
//    @Generated(GenerationTime.ALWAYS)
    @Column(name = "CREATE_TIME", updatable = false)
    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Id
    @Column(name = "TEXT")
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Glossary)) return false;

        Glossary glossary = (Glossary) o;

        if (text != null ? !text.equals(glossary.text) : glossary.text != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return text != null ? text.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Glossary{" +
                "createTime=" + createTime +
                ", text='" + text + '\'' +
                '}';
    }
}
