package com.alcatel_lucent.dms.model;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.BusinessWarning;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.UnsupportedEncodingException;
import java.util.*;

@Entity
@Table(name = "DICTIONARY")
public class Dictionary extends BaseEntity {

    private static final long serialVersionUID = 4926531636839152201L;

    @Id
    @GeneratedValue(generator = "SEQ_GEN")
    @GenericGenerator(name = "SEQ_GEN", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
            @org.hibernate.annotations.Parameter(name = "sequence_name", value = "ID_DICTIONARY"),
            @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled")
    })
//    @SequenceGenerator(name = "SEQ_GEN", sequenceName = "ID_DICTIONARY", allocationSize = 20)
    @Column(name = "ID")
    @Override
    public Long getId() {
        return super.getId();
    }

    private Collection<DictionaryLanguage> dictLanguages;
    private Collection<Label> labels;
    private boolean locked;
    private DictionaryBase base;
    private String version;

    private String annotation1;
    private String annotation2;
    private String annotation3;
    private String annotation4;

    private static Map<String, String> refCodes = JSONObject.fromObject("{'DCT':'GAE','Dictionary conf':'EN-UK','Text properties':'en','XML labels':'en'}");

    public String getName() {
        return base.getName();
    }

    public void addDictLanguage(DictionaryLanguage dictionaryLanguage) {
        this.dictLanguages.add(dictionaryLanguage);
    }

    public void addLabel(Label label) {
        this.labels.add(label);
    }

    @Transient
    public String getLanguageReferenceCode() {
        String ref = refCodes.get(getFormat());
        return null == ref ? "en" : ref;
    }

    public void setName(String name) {
        base.setName(name);
    }

    public String getFormat() {
        return base.getFormat();
    }

    public void setFormat(String format) {
        base.setFormat(format);
    }

    public String getEncoding() {
        return base.getEncoding();
    }

    public void setEncoding(String encoding) {
        base.setEncoding(encoding);
    }

    public String getPath() {
        return base.getPath();
    }

    public void setPath(String path) {
        base.setPath(path);
    }

    @ManyToOne
    @JoinColumn(name = "DICTIONARY_BASE_ID")
    @OnDelete(action = OnDeleteAction.CASCADE)
    public DictionaryBase getBase() {
        return base;
    }

    @Transient
    public int getLabelNum() {
        return labels == null ? 0 : labels.size();
    }

    public void setBase(DictionaryBase base) {
        this.base = base;
    }

    @Column(name = "VERSION")
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Dictionary() {
        super();
    }

    @OneToMany
    public Collection<DictionaryLanguage> getDictLanguages() {
        return dictLanguages;
    }

    public void setDictLanguages(Collection<DictionaryLanguage> dictLanguages) {
        this.dictLanguages = dictLanguages;
    }

    @OneToMany
    public Collection<Label> getLabels() {
        return labels;
    }

    public void setLabels(Collection<Label> labels) {
        this.labels = labels;
    }

    @Column(name = "LOCKED")
    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public Language getLanguageByCode(String langCode) {
        if (dictLanguages != null) {
            for (DictionaryLanguage dl : dictLanguages) {
                if (dl.getLanguageCode().equals(langCode)) {
                    return dl.getLanguage();
                }
            }
        }
        return null;
    }

    public String getLanguageCode(Long languageId) {
        if (dictLanguages != null) {
            for (DictionaryLanguage dl : dictLanguages) {
                if (dl.getLanguage().getId().equals(languageId)) {
                    return dl.getLanguageCode();
                }
            }
        }
        return null;
    }

    @Transient
    public HashSet<String> getAllLanguageCodes() {
        HashSet<String> result = new HashSet<String>();
        if (dictLanguages != null) {
            for (DictionaryLanguage dl : dictLanguages) {
                result.add(dl.getLanguageCode());
            }
        }
        return result;
    }

    @Transient
    public ArrayList<String> getAllLanguageCodesOrdered() {
        ArrayList<String> result = new ArrayList<String>();
        if (dictLanguages != null) {
            for (DictionaryLanguage dl : dictLanguages) {
                result.add(dl.getLanguageCode());
            }
        }
        return result;
    }

    @Transient
    public ArrayList<Language> getAllLanguages() {
        ArrayList<Language> result = new ArrayList<Language>();
        if (dictLanguages != null) {
            for (DictionaryLanguage dl : dictLanguages) {
                result.add(dl.getLanguage());
            }
        }
        return result;
    }

    @Transient
    public Map<Long, String> getLangCodeMap() {
        Map<Long, String> result = new HashMap<Long, String>();
        if (dictLanguages != null) {
            for (DictionaryLanguage dl : dictLanguages) {
                result.put(dl.getLanguage().getId(), dl.getLanguageCode());
            }
        }
        return result;
    }

    public DictionaryLanguage getDictLanguage(Long languageId) {
        if (dictLanguages != null) {
            for (DictionaryLanguage dl : dictLanguages) {
                if (dl.getLanguage().getId().equals(languageId)) {
                    return dl;
                }
            }
        }
        return null;
    }

    public DictionaryLanguage getDictLanguage(String languageCode) {
        if (dictLanguages != null) {
            for (DictionaryLanguage dl : dictLanguages) {
                if (dl.getLanguageCode().equals(languageCode)) {
                    return dl;
                }
            }
        }
        return null;
    }


    public Label getLabel(String key) {
        if (labels != null) {
            for (Label label : labels) {
                if (label.getKey().equals(key)) {
                    return label;
                }
            }
        }
        return null;
    }

    private Map<String, int[]> summaryCache;

    /**
     * Get translation status summary by language, used by front
     *
     * @return
     */
    @Transient
    public Map<String, int[]> getS() {
        return summaryCache;
    }

    public void setS(Map<Long, int[]> summary) {
        this.summaryCache = new HashMap<String, int[]>();
        if (summary == null) return;
        for (Long langId : summary.keySet()) {
            summaryCache.put(langId.toString(), summary.get(langId));
        }
    }

    private Application app;    // transient variable for REST service

    public void setApp(Application app) {
        this.app = app;
    }

    @Transient
    public Application getApp() {
        return app;
    }

    private Collection<BusinessWarning> parseWarnings;        // transient variable for parse warnings information
    private Collection<BusinessWarning> importWarnings;        // transient variable for import warnings information
    private Collection<BusinessException> previewErrors;            // transient variable for errors information;

    @Transient
    public Collection<BusinessWarning> getParseWarnings() {
        return parseWarnings;
    }

    public void setParseWarnings(Collection<BusinessWarning> parseWarnings) {
        this.parseWarnings = parseWarnings;
    }

    @Transient
    public Collection<BusinessWarning> getImportWarnings() {
        return importWarnings;
    }

    public void setImportWarnings(Collection<BusinessWarning> importWarnings) {
        this.importWarnings = importWarnings;
    }

    @Transient
    public Collection<BusinessException> getPreviewErrors() {
        return previewErrors;
    }

    public void setPreviewErrors(Collection<BusinessException> previewErrors) {
        this.previewErrors = previewErrors;
    }

    @Transient
    public int getWarningCount() {
        return (parseWarnings == null ? 0 : parseWarnings.size()) +
                (importWarnings == null ? 0 : importWarnings.size());
    }

    @Transient
    public int getErrorCount() {
        return previewErrors == null ? 0 : previewErrors.size();
    }


    @Transient
    public Collection<String> getWarnings() {
        Collection<String> result = new ArrayList<String>();
        if (parseWarnings != null) {
            for (BusinessWarning warning : parseWarnings) {
                result.add(warning.toString());
            }
        }
        if (importWarnings != null) {
            for (BusinessWarning warning : importWarnings) {
                result.add(warning.toString());
            }
        }
        return result;
    }

    @Transient
    public Collection<String> getErrors() {
        Collection<String> result = new ArrayList<String>();
        if (previewErrors != null) {
            for (BusinessException e : previewErrors) {
                result.add(e.toString());
            }
        }
        return result;
    }

    /**
     * Validate dictionary before importing.
     * The method will refresh "previewErrors" and "importWarnings" properties.
     */
    public void validate() {
        previewErrors = new ArrayList<BusinessException>();
        importWarnings = new ArrayList<BusinessWarning>();
        if (getName() == null || getName().trim().isEmpty()) {
            previewErrors.add(new BusinessException(BusinessException.LACK_DICT_NAME));
        }
        if (getVersion() == null || getVersion().trim().isEmpty()) {
            previewErrors.add(new BusinessException(BusinessException.LACK_DICT_VERSION));
        }
        if (dictLanguages != null) {
            for (DictionaryLanguage dl : dictLanguages) {
                if (dl.getLanguage() == null) {
                    previewErrors.add(new BusinessException(BusinessException.UNKNOWN_LANG_CODE, 0, dl.getLanguageCode()));
                }
                if (dl.getCharset() == null) {
                    previewErrors.add(new BusinessException(BusinessException.CHARSET_NOT_DEFINED, dl.getLanguageCode()));
                }
            }
        }
        if (labels != null) {
            // check duplicate reference+context
            HashSet<String> existingReference = new HashSet<String>();
            HashSet<String> alreadyWarning = new HashSet<String>();
            for (Label label : labels) {
                String key = label.getContext() + "~~" + label.getReference();
                if (existingReference.contains(key)) {
                    if (!alreadyWarning.contains(key)) {    // warn only once
                        importWarnings.add(new BusinessWarning(BusinessWarning.DUPLICATE_REFERENCE, label.getContext().getName(), label.getReference()));
                        alreadyWarning.add(key);
                    }
                } else {
                    existingReference.add(key);
                }
            }

            // sort malformed character warnings
            TreeMap<String, Collection<BusinessWarning>> malformWarnings = new TreeMap<String, Collection<BusinessWarning>>();
            for (Label label : labels) {
                if (label.getOrigTranslations() != null) {
                    for (LabelTranslation lt : label.getOrigTranslations()) {
                        String langCode = lt.getLanguageCode();
                        DictionaryLanguage dl = getDictLanguage(langCode);
                        if (dl.getCharset() == null) continue;
                        String charsetName = dl.getCharset().getName();
                        String encodedTranslation = lt.getOrigTranslation();
                        boolean invalidText = false;
                        if (!getEncoding().equals(charsetName)) {
                            try {
                                byte[] source = lt.getOrigTranslation().getBytes(getEncoding());
                                encodedTranslation = new String(source, charsetName);
                                byte[] target = encodedTranslation.getBytes(charsetName);
                                if (!Arrays.equals(source, target)) {
                                    invalidText = true;
                                    Collection<BusinessWarning> warnings = malformWarnings.get(langCode);
                                    if (warnings == null) {
                                        warnings = new ArrayList<BusinessWarning>();
                                        malformWarnings.put(langCode, warnings);
                                    }
                                    warnings.add(new BusinessWarning(
                                            BusinessWarning.INVALID_TEXT,
                                            encodedTranslation, dl.getCharset().getName(),
                                            langCode, label.getKey()));
                                }
                            } catch (UnsupportedEncodingException e) {
                                previewErrors.add(new BusinessException(
                                        BusinessException.CHARSET_NOT_FOUND, charsetName));
                            }
                        }
                        if (!invalidText && lt.getLanguage() != null && !lt.isValidText(encodedTranslation)) {
                            Collection<BusinessWarning> warnings = malformWarnings.get(langCode);
                            if (warnings == null) {
                                warnings = new ArrayList<BusinessWarning>();
                                malformWarnings.put(langCode, warnings);
                            }
                            warnings.add(new BusinessWarning(
                                    BusinessWarning.SUSPICIOUS_CHARACTER,
                                    encodedTranslation, dl.getCharset().getName(),
                                    langCode, label.getKey()));
                        }
                    }
                }
            }
            for (Collection<BusinessWarning> warnings : malformWarnings.values()) {
                importWarnings.addAll(warnings);
            }
            for (Label label : labels) {
                if (!label.checkLength(label.getReference())) {
                    importWarnings.add(new BusinessWarning(
                            BusinessWarning.EXCEED_MAX_LENGTH, "Reference", label.getKey()));
                }
                if (label.getOrigTranslations() != null) {
                    for (LabelTranslation lt : label.getOrigTranslations()) {
                        if (!label.checkLength(lt.getOrigTranslation())) {
                            importWarnings.add(new BusinessWarning(
                                    BusinessWarning.EXCEED_MAX_LENGTH,
                                    lt.getLanguageCode(), label.getKey()));
                        }
                    }
                }
            }
        }
    }

    @Transient
    public int getMaxSortNo() {
        if (dictLanguages == null) {
            return 0;
        }
        int max = 0;
        for (DictionaryLanguage dl : dictLanguages) {
            if (dl.getSortNo() > max) {
                max = dl.getSortNo();
            }
        }
        return max;
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

    /**
     * Update language of a specific language code
     *
     * @param languageCode language code
     * @param language     retrieve language
     */
    public void updateLanguage(String languageCode, Language language) {
        DictionaryLanguage dl = this.getDictLanguage(languageCode);
        if (dl != null) {
            dl.setLanguage(language);
        }
        if (labels != null) {
            for (Label label : labels) {
                LabelTranslation lt = label.getOrigTranslation(languageCode);
                if (lt != null) {
                    lt.setLanguage(language);
                }
            }
        }

    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("name", getName())
                .append("version", version)
                .toString();
    }
}
