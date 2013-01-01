package com.alcatel_lucent.dms.model;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "ISO_LANGUAGE_CODE")
public class ISOLanguageCode implements Serializable, LanguageCode {

    private String code;
    private Language language;
    private boolean defaultCode;

    @Id
    @Column(name = "CODE")
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @ManyToOne
    @JoinColumn(name = "LANGUAGE_ID", nullable = false, updatable = false)
    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    @Column(name = "DEFAULT_CODE")
    public boolean isDefaultCode() {
        return defaultCode;
    }

    public void setDefaultCode(boolean defaultCode) {
        this.defaultCode = defaultCode;
    }

    @Override
    public String toString() {
        return String.format("ISOLanguageCode [code=%s]", code);
    }

}
