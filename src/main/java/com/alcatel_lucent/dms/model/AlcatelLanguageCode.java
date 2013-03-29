package com.alcatel_lucent.dms.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import java.io.Serializable;

@Entity
@Table(name = "ALCATEL_LANGUAGE_CODE")
public class AlcatelLanguageCode implements Serializable, LanguageCode {

    /**
     *
     */
    private static final long serialVersionUID = -7050518854634982880L;

    @Id
    @Column(name = "CODE")
    private String code;

    @ManyToOne
    @JoinColumn(name = "LANGUAGE_ID", nullable = false, updatable = false)
    private Language language;

    @Column(name = "DEFAULT_CODE")
    private boolean defaultCode;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public boolean isDefaultCode() {
        return defaultCode;
    }

    public void setDefaultCode(boolean defaultCode) {
        this.defaultCode = defaultCode;
    }

    @Override
    public String toString() {
        return String.format("AlcatelLanguageCode [code=%s]", code);
    }

}
