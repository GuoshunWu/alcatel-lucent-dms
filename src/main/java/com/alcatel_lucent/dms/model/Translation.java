package com.alcatel_lucent.dms.model;

import com.alcatel_lucent.dms.util.CharsetUtil;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;

@Entity
@Table(name = "TRANSLATION")
public class Translation extends BaseEntity {
    /**
     *
     */
    private static final long serialVersionUID = -4338889575255363014L;

    @Id
    @GeneratedValue(generator = "HILO_GEN")
    @TableGenerator(name = "HILO_GEN", table = "ID_TRANSLATION")
    @Column(name = "ID")
    @Override
    public Long getId() {
        return super.getId();
    }


    public static final int STATUS_UNTRANSLATED = 0;
    public static final int STATUS_IN_PROGRESS = 1;
    public static final int STATUS_TRANSLATED = 2;

    private Text text;
    private Language language;
    private String translation;
    private String warnings;
    private int status;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "TEXT_ID", nullable = false)
    @Index(name = "I_TRANSLATION_TEXT_LAN")
    public Text getText() {
        return text;
    }

    public void setText(Text text) {
        this.text = text;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "LANGUAGE_ID", nullable = false)
    @Index(name = "I_TRANSLATION_TEXT_LAN")
    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    @Column(name = "TRANSLATION", length = 1024)
    public String getTranslation() {
        return translation;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }

    @Override
    public String toString() {
        return String.format(
                "Translation [text=%s, language=%s, translation=%s]", text,
                language, translation);
    }

    @Transient
    public boolean isValidText() {
        if (translation != null) {
            return CharsetUtil.isValid(translation, language.getName());
        }
        return true;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Column(name = "STATUS")
    public int getStatus() {
        return status;
    }

    public void setWarnings(String warnings) {
        this.warnings = warnings;
    }

    @Column(name = "WARNINGS", length = 255)
    public String getWarnings() {
        return warnings;
    }

}
