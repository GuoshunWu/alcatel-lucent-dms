package com.alcatel_lucent.dms.model;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * Created with IntelliJ IDEA.
 * User: guoshunw
 * Date: 13-7-24
 * Time: 上午11:37
 * To change this template use File | Settings | File Templates.
 */

@Entity
@Table(name = "GLOSSARY")  @Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class Glossary {
    private Timestamp createTime;

    private String text;

    public Glossary() {
    }

    public Glossary(Timestamp createTime, String text) {
        this.createTime = createTime;
        this.text = text;
    }

    @Column(name="CREATE_TIME")
    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    @Id
    @Column(name="TEXT")
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
}
