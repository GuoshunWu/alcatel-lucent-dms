package com.alcatel_lucent.dms.model;

import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;


import javax.persistence.*;
import javax.validation.constraints.NotNull;
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
public class Glossary {

//    @Temporal(TemporalType.TIMESTAMP)
//    @Generated(GenerationTime.ALWAYS)
    @Column(name = "CREATE_TIME", updatable = false, nullable = false)
    private Date createTime;

    @Id
    @Column(name = "TEXT")
    private String text;

    public Glossary() {
    }

    public Glossary(String text) {
        this.text = text;
    }

    public Glossary(Date createTime, String text) {
        this(text);
        this.createTime = createTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

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
