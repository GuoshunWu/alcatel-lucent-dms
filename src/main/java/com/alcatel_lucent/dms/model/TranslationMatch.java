package com.alcatel_lucent.dms.model;

import org.apache.lucene.util.ToStringUtils;

/**
 * Created with IntelliJ IDEA.
 * User: guoshunw
 * Date: 13-6-13
 * Time: 下午1:17
 */
public class TranslationMatch implements Comparable<TranslationMatch> {
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TranslationMatch)) return false;

        TranslationMatch that = (TranslationMatch) o;

        if (!reference.equals(that.reference)) return false;
        if (!translation.equals(that.translation)) return false;

        return true;
    }

    @Override
    public int hashCode() {
//        int result = id.hashCode();
//        result = 31 * result + (score != +0.0f ? Float.floatToIntBits(score) : 0);
        int result = translation.hashCode();
        result = 31 * result + reference.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return String.format("TM[id=%d, reference=%s,trans=%s]", id, reference, translation);
    }

    @Override
    public int compareTo(TranslationMatch o) {
        float result = score - o.score;
        if (result > 0) return 1;
        if (result < 0) return -1;
        return 0;
    }
}
