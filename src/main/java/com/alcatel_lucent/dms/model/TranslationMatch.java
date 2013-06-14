package com.alcatel_lucent.dms.model;

import org.apache.lucene.util.ToStringUtils;

/**
 * Created with IntelliJ IDEA.
 * User: guoshunw
 * Date: 13-6-13
 * Time: 下午1:17
 */
public class TranslationMatch {
    private Long id;

    private float score;
    private String translation;
    private String reference;

    public TranslationMatch(Long id, float score, String translation, String reference) {
        this.id = id;
        this.score = score;
        this.translation = translation;
        this.reference = reference;
    }

    public TranslationMatch() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public String getTranslation() {
        return translation;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    @Override
    public String toString() {
        return String.format("TM[id=%d, reference=%s,trans=%s]", id, reference, translation);
    }

}
