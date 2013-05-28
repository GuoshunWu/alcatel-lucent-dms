package com.alcatel_lucent.dms.model;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;
import org.hibernate.util.EqualsHelper;

import javax.persistence.*;

//@Entity
@Table(name = "DICTIONARY_LANGUAGE")
public class DictionaryLanguage extends BaseEntity {
    /**
     *
     */
    private static final long serialVersionUID = 9194788138624147936L;


    @Id
    @GeneratedValue(generator = "SEQ_GEN")
    @SequenceGenerator(name = "SEQ_GEN", sequenceName = "ID_DICTIONARY_LANGUAGE", allocationSize = 20)
    @Column(name = "ID")
    @Override
    public Long getId() {
        return super.getId();
    }

    private Dictionary dictionary;
    private Language language;
    private String languageCode;
    private Charset charset;
    private int sortNo;
    private String annotation1;
    private String annotation2;
    private String annotation3;
    private String annotation4;


    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "DICTIONARY_ID")
    public Dictionary getDictionary() {
        return dictionary;
    }

    public void setDictionary(Dictionary dictionary) {
        this.dictionary = dictionary;
    }

    @ManyToOne
    @JoinColumn(name = "LANGUAGE_ID")
    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    @Column(name = "LANGUAGE_CODE")
    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    @ManyToOne
    @JoinColumn(name = "CHARSET_ID")
    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    @Override
    public String toString() {
        return String
                .format("DictionaryLanguage [dictionary=%s, language=%s, languageCode=%s, charset=%s]",
                        dictionary, language, languageCode, charset);
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsHelper.equals(obj, this);
    }

    public void setSortNo(int sortNo) {
        this.sortNo = sortNo;
    }

    @Column(name = "SORT_NO")
    public int getSortNo() {
        return sortNo;
    }

    @Column(name = "ANNOTATION1")
    @Type(type = "text")
    public String getAnnotation1() {
        return annotation1;
    }

    public void setAnnotation1(String annotation1) {
        this.annotation1 = annotation1;
    }

    @Column(name = "ANNOTATION2")
    @Type(type = "text")
    public String getAnnotation2() {
        return annotation2;
    }

    public void setAnnotation2(String annotation2) {
        this.annotation2 = annotation2;
    }

    @Column(name = "ANNOTATION3")
    @Type(type = "text")
    public String getAnnotation3() {
        return annotation3;
    }

    public void setAnnotation3(String annotation3) {
        this.annotation3 = annotation3;
    }

    @Column(name = "ANNOTATION4")
    @Type(type = "text")
    public String getAnnotation4() {
        return annotation4;
    }

    public void setAnnotation4(String annotation4) {
        this.annotation4 = annotation4;
    }
    
    public boolean isReference() {
    	return getDictionary().getReferenceLanguage().equals(languageCode);
    }

}
