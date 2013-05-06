package com.alcatel_lucent.dms.model.test;

import org.hibernate.search.annotations.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: guoshunw
 * Date: 13-4-26
 * Time: 下午3:42
 * To change this template use File | Settings | File Templates.
 */

@Entity
//@Indexed
public class Book {

    @Id
    @GeneratedValue
    private Integer id;


    @Field(index = Index.TOKENIZED, store = Store.NO)
    private String title;
    @Field(index = Index.TOKENIZED, store = Store.NO)
    private String subtitle;

    @IndexedEmbedded
    @ManyToMany
    private Set<Author> authors = new HashSet<Author>();

    @Field(index = Index.UN_TOKENIZED, store = Store.YES)
    @DateBridge(resolution = Resolution.DAY)
    private Date publicationDate;

    public Book() {
    }

    public Book(String title, String subtitle, Set<Author> authors, Date publicationDate) {
        this.title = title;
        this.subtitle = subtitle;
        this.authors = authors;
        this.publicationDate = publicationDate;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public Set<Author> getAuthors() {
        return authors;
    }

    public void setAuthors(Set<Author> authors) {
        this.authors = authors;
    }

    public Date getPublicationDate() {
        return publicationDate;
    }

    public void setPublicationDate(Date publicationDate) {
        this.publicationDate = publicationDate;
    }

    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", subtitle='" + subtitle + '\'' +
                ", authors=" + authors +
                ", publicationDate=" + publicationDate +
                '}';
    }
}
