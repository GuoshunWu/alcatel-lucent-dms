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

    /**
     * Returns a string representation of the object. In general, the
     * <code>toString</code> method returns a string that
     * "textually represents" this object. The result should
     * be a concise but informative representation that is easy for a
     * person to read.
     * It is recommended that all subclasses override this method.
     * <p/>
     * The <code>toString</code> method for class <code>Object</code>
     * returns a string consisting of the name of the class of which the
     * object is an instance, the at-sign character `<code>@</code>', and
     * the unsigned hexadecimal representation of the hash code of the
     * object. In other words, this method returns a string equal to the
     * value of:
     * <blockquote>
     * <pre>
     * getClass().getName() + '@' + Integer.toHexString(hashCode())
     * </pre></blockquote>
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return String.format("TM[id=%d, reference=%s,trans=%s]", id, reference, translation);
    }

}
