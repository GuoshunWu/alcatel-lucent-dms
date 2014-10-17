package com.alcatel_lucent.dms.model;

import com.alcatel_lucent.dms.BusinessWarning;
import com.alcatel_lucent.dms.util.CharsetUtil;
import com.google.common.collect.ImmutableMap;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.search.annotations.*;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//@Entity

@Table(name = "TRANSLATION")
@Indexed
@FullTextFilterDefs({
})
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

    public static final int TYPE_DICT = 1;        // imported from dictionary
    public static final int TYPE_TASK = 2;        // received from translation task
    public static final int TYPE_MANUAL = 3;    // updated manually
    public static final int TYPE_AUTO = 4;        // matched automatically from other context

    private Text text;
    private Language language;
    private String translation;
    private String warnings;
    private int status;
    private Integer translationType;
    private Timestamp lastUpdateTime;
    private Integer verifyStatus;
    private Collection<TranslationHistory> histories;

    private Collection<BusinessWarning> transWarnings = new ArrayList<BusinessWarning>();

    private static final Pattern paramPattern = Pattern.compile("%[dscf]");
    private static final Pattern BRPattern = Pattern.compile("<br\\s*/?>", Pattern.CASE_INSENSITIVE);
    private static final Pattern CNPattern = Pattern.compile("\\n", Pattern.CASE_INSENSITIVE);

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "TEXT_ID", nullable = false)
    @Index(name = "I_TRANSLATION_TEXT_LAN")
    @IndexedEmbedded
    public Text getText() {
        return text;
    }

    public void setText(Text text) {
        this.text = text;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "LANGUAGE_ID", nullable = false)
    @Index(name = "I_TRANSLATION_TEXT_LAN")
    @IndexedEmbedded
    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    @Column(name = "TRANSLATION", length = 1024)
    @Fields({
            @Field(store = Store.YES),
            @Field(name = "translation_forSort", index = org.hibernate.search.annotations.Index.UN_TOKENIZED)
    })
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

    @Transient
    public Collection<Map> getTransWarnings() {
        Collection<Map> errors = new ArrayList<Map>();
        for (BusinessWarning error : this.transWarnings) {
            errors.add(ImmutableMap.of("code", error.getCode(), "message", error.getMessage()));
        }
        return errors;
    }

    public Collection<BusinessWarning> validate(Label label) {
        transWarnings.clear();
        //check max length
        String dictName = label.getDictionary().getName();
        String labelKey = label.getKey();
        String languageName = language.getName();

        String reference = label.getReference();

        if (!label.checkLength(translation)) {
            transWarnings.add(new BusinessWarning(BusinessWarning.EXCEED_MAX_LENGTH, dictName, labelKey));
        }
        //check translation parameters
        if (!isTranslationParametersCorrect(reference)) {
            transWarnings.add(new BusinessWarning(BusinessWarning.PARAMETERS_INCORRECT, dictName, labelKey, languageName));
        }
        // check br and \n consistent
        if (!patternCheck(reference, BRPattern) || !patternCheck(reference, CNPattern)) {
            transWarnings.add(new BusinessWarning(BusinessWarning.BR_INCONSISTENT, dictName, labelKey, languageName));
        }




        return transWarnings;
    }

    private boolean patternCheck(String reference, Pattern pattern) {
        Matcher refMatcher = pattern.matcher(reference);
        Matcher translationMatcher = pattern.matcher(translation);

        while (refMatcher.find()) {
            String refMathParameter = refMatcher.group();
            //corresponding translation parameter cannot be found
            if (!translationMatcher.find()) {
                return false;
            }
            String translationMathParameter = translationMatcher.group();
            if (!refMathParameter.equals(translationMathParameter)) return false;
        }
        if (translationMatcher.find()) {
            // extra parameter found in translation
            return false;
        }
        return true;
    }

    public boolean isTranslationParametersCorrect(String reference) {
        return patternCheck(reference, paramPattern);
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Column(name = "STATUS")
    @Field
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

    public Integer getTranslationType() {
        return translationType;
    }

    public void setTranslationType(Integer translationType) {
        this.translationType = translationType;
    }

    public Timestamp getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Timestamp lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public Integer getVerifyStatus() {
        return verifyStatus;
    }

    public void setVerifyStatus(Integer verifyStatus) {
        this.verifyStatus = verifyStatus;
    }

    public Collection<TranslationHistory> getHistories() {
        return histories;
    }

    public void setHistories(Collection<TranslationHistory> histories) {
        this.histories = histories;
    }

}
