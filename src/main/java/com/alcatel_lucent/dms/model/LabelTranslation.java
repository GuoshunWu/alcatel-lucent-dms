package com.alcatel_lucent.dms.model;

import com.alcatel_lucent.dms.util.CharsetUtil;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;

//@Entity
@Table(name = "LABEL_TRANSLATION")
@org.hibernate.annotations.Table(appliesTo = "LABEL_TRANSLATION",
        indexes = {@Index(name = "I_LABEL_TRANSLATION_LABEL_LANG", columnNames = {
                "LABEL_ID", "LANGUAGE_ID"
        })})
public class LabelTranslation extends BaseEntity {

    private static final long serialVersionUID = 5749208472612761751L;

    @Id
    @GeneratedValue(generator = "HILO_GEN")
    @TableGenerator(name = "HILO_GEN", table = "ID_LABEL_TRANSLATION")
    @Column(name = "ID")
    @Override
    public Long getId() {
        return super.getId();
    }

    private Label label;
    private Language language;
    private String origTranslation;
    private String annotation1;
    private String annotation2;

    private String warnings;
    private String languageCode;
    private String comment;
    private int sortNo;

    private Integer translationType;
    public static final int TYPE_AUTO = Translation.TYPE_AUTO;        // matched automatically from other context


    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Specify if translation is requested.
     * Some dictionary can explicitly specify if translation is request
     * If this flag is set, translation status would be forcibly set to "Not translated" or "Translated".
     */
    private Boolean requestTranslation;

    /**
     * Specify if the translation of this label should be retrieved from context dictionary.
     * In some case, translation should not come from context dictionary.
     */
    private boolean needTranslation;

    public void setLabel(Label label) {
        this.label = label;
    }

    @Column(name = "TRANSLATION_TYPE", length = 1024)
    public Integer getTranslationType() {
        return translationType;
    }

    public void setTranslationType(Integer translationType) {
        this.translationType = translationType;
    }

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "LABEL_ID")
    public Label getLabel() {
        return label;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "LANGUAGE_ID", nullable = false)
    public Language getLanguage() {
        return language;
    }

    public void setOrigTranslation(String origTranslation) {
        this.origTranslation = origTranslation;
    }

    @Column(name = "ORIG_TRANSLATION", length = 1024)
    public String getOrigTranslation() {
        return origTranslation;
    }

    public void setNeedTranslation(boolean needTranslation) {
        this.needTranslation = needTranslation;
    }

    @Column(name = "NEED_TRANSLATION")
    public boolean isNeedTranslation() {
        return needTranslation;
    }


    public void setWarnings(String warnings) {
        this.warnings = warnings;
    }

    @Column(name = "WARNINGS")
    public String getWarnings() {
        return warnings;
    }

    @Transient
    public boolean isTranslationEqualsReference(){
        if(null == label) return false;
        return label.getReference().equals(getOrigTranslation());
    }

    @Transient
    public boolean isValidText() {
        if (origTranslation != null) {
            return CharsetUtil.isValid(origTranslation, language.getName());
        }
        return true;
    }

    public boolean isValidText(String text) {
        if (text != null) {
            return CharsetUtil.isValid(text, language.getName());
        }
        return true;
    }

    public void setSortNo(int sortNo) {
        this.sortNo = sortNo;
    }

    @Column(name = "SORT_NO")
    public int getSortNo() {
        return sortNo;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    @Column(name = "LANGUAGE_CODE")
    public String getLanguageCode() {
        return languageCode;
    }

    @Column(name = "REQUEST_TRANSLATION")
    public Boolean getRequestTranslation() {
        return requestTranslation;
    }

    public void setRequestTranslation(Boolean requestTranslation) {
        this.requestTranslation = requestTranslation;
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


    // transient variables

    private String translation;    // hold translation result for LabelTranslationREST

    public String getTranslation() {
        return translation;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }

    private Integer status;    // hold translation status for LabelTranslationREST, or delivery

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }


}
