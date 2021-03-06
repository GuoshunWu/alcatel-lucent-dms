package com.alcatel_lucent.dms.model;

import com.alcatel_lucent.dms.SystemError;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.analysis.LowerCaseFilterFactory;
import org.apache.solr.analysis.StandardFilterFactory;
import org.apache.solr.analysis.StandardTokenizerFactory;
import org.apache.solr.analysis.StopFilterFactory;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.*;

import javax.persistence.*;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;


@AnalyzerDef(name = "englishSnowBallAnalyzer",
//        charFilters = {
//                @CharFilterDef(factory = MappingCharFilterFactory.class, params = {
//                        @Parameter(name = "mapping", value = "")
//                })
//        },
        tokenizer = @TokenizerDef(factory = StandardTokenizerFactory.class),
        filters = {
                @TokenFilterDef(factory = StandardFilterFactory.class),
                @TokenFilterDef(factory = LowerCaseFilterFactory.class),
                @TokenFilterDef(factory = StopFilterFactory.class)
//                @TokenFilterDef(factory = SnowballPorterFilterFactory.class,
//                        params = @Parameter(name = "language", value = "english")
//                )
        }
)

//@Entity
@Table(name = "LABEL")
@Indexed
//@Analyzer(definition = "englishSnowBallAnalyzer")
public class Label extends BaseEntity implements Cloneable {
    /**
     *
     */
    private static final long serialVersionUID = -4086873912554236932L;
    
    // Capitalization styles
    public static final int CAPITALIZATION_ALL_UPPER_CASE = 1;
    public static final int CAPITALIZATION_ALL_CAPITALIZED = 2;
    public static final int CAPITALIZATION_FIRST_CAPITALIZED = 3;
    public static final int CAPITALIZATION_FIRST_CAPITALIZED_ONLY = 4;
    public static final int CAPITALIZATION_ALL_LOWER_CASE = 5;


    @Id
    @GeneratedValue(generator = "LABEL_GEN")
    @TableGenerator(name = "LABEL_GEN", table = "ID_LABEL", allocationSize = 100,
            valueColumnName = "next_hi")
    @Column(name = "ID")
    @Override
    @DocumentId
    public Long getId() {
        return super.getId();
    }

    public Label() {
    }

    public Label(String key) {
        this.key = key;
    }

    public static final String CHECK_FIELD_NAME = "CHK";
    public static final String REFERENCE_FIELD_NAME = "GAE";


    private Dictionary dictionary;
    private String key;
    private int sortNo;


    private String reference;
    private String description;
    private String maxLength;
    private Context context;
    private Text text;
    private boolean removed;

    private String fontName;
    private String fontSize;
    private Integer capitalization;

    private String annotation1;
    private String annotation2;
    private String annotation3;
    private String annotation4;

    private Collection<LabelTranslation> origTranslations;

    public String getFontName() {
        return fontName;
    }

    public void setFontName(String fontName) {
        this.fontName = fontName;
    }

    public String getFontSize() {
        return fontSize;
    }

    public void setFontSize(String fontSize) {
        this.fontSize = fontSize;
    }

    public void addLabelTranslation(LabelTranslation labelTranslation) {
        this.origTranslations.add(labelTranslation);
    }

    @Column(name = "ANNOTATION2")
    @Type(type = "text")
    public String getAnnotation2() {
        return annotation2;
    }

    public void setAnnotation2(String annotation2) {
        this.annotation2 = annotation2;
    }

    @Column(name = "ANNOTATION1")
    @Type(type = "text")
    public String getAnnotation1() {
        return annotation1;
    }

    public void setAnnotation1(String annotation1) {
        this.annotation1 = annotation1;
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

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "DICTIONARY_ID")
    @IndexedEmbedded
    public Dictionary getDictionary() {
        return dictionary;
    }

    public void setDictionary(Dictionary dictionary) {
        this.dictionary = dictionary;
    }

    @Field(index = org.hibernate.search.annotations.Index.UN_TOKENIZED)
    @Column(name = "LABEL_KEY", nullable = false, length = 1024)
    public String getKey() {
        return key;
    }


    public void setKey(String key) {
        this.key = key;
    }

    //    @Field(store = Store.YES, analyzer = @Analyzer(definition = "ngram"))
    @Fields({
            @Field(store = Store.YES),
            @Field(name = "reference_forSort", index = org.hibernate.search.annotations.Index.UN_TOKENIZED)
    })
    @Column(name = "REFERENCE", nullable = false, length = 1024)
    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    @Column(name = "DESCRIPTION")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Field(index = org.hibernate.search.annotations.Index.UN_TOKENIZED)
    @Column(name = "MAX_LENGTH")
    public String getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(String maxLength) {
        this.maxLength = maxLength;
    }

    @IndexedEmbedded
    @ManyToOne
    @JoinColumn(name = "CONTEXT_ID", nullable = false)
    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @ManyToOne
    @JoinColumn(name = "TEXT_ID", nullable = false)
    public Text getText() {
        return text;
    }

    public void setText(Text text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return reflectionToString(this);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((context == null) ? 0 : context.hashCode());
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        return result;
    }

    /**
     * Check if text meets max length constraint of the label
     *
     * @param text
     * @return
     */
    public boolean checkLength(String text) {
        if (StringUtils.isEmpty(maxLength) || text == null) {
            return true;    // no constraint
        }
        
        String[] texts = text.split("\n");
        
        try {
            if (maxLength.contains("*")) {
                String[] linesAndColumns = maxLength.split("\\*");
                int lines = Integer.parseInt(linesAndColumns[0]);
                if (-1 == lines) lines = Integer.MAX_VALUE;
                int columns = Integer.parseInt(linesAndColumns[1]);
                if (-1 == columns) columns = Integer.MAX_VALUE;

                if (texts.length > lines) return false;
                for (String strLine : texts) {
                    if (strLine.length() > columns) return false;
                }
                return true;
            }

            if (maxLength.contains("x")) {
                String[] linesAndColumns = maxLength.split("x");
                int lines = Integer.parseInt(linesAndColumns[1].trim());
                if (-1 == lines) lines = Integer.MAX_VALUE;
                int columns = Integer.parseInt(linesAndColumns[0].trim());
                if (-1 == columns) columns = Integer.MAX_VALUE;

                if (texts.length > lines) return false;
                for (String strLine : texts) {
                    if (strLine.length() > columns) return false;
                }
                return true;
            }
        } catch (Exception e) {
        	log.warn("Unrecognized maxLength: " + maxLength);
        }

        String[] lens = maxLength.split(",");
        for (int rowIndex = 0; rowIndex < texts.length; rowIndex++) {
            try {
                if (rowIndex >= lens.length || texts[rowIndex].getBytes("ISO-8859-1").length > Integer.parseInt(lens[rowIndex])) {
                    return false;
                }
            } catch (NumberFormatException e) {
                log.warn("Invalid maxLength for label " + key + ": " + maxLength);
            } catch (UnsupportedEncodingException e) {
                throw new SystemError(e);
            }
        }
        return true;
    }

    public void setSortNo(int sortNo) {
        this.sortNo = sortNo;
    }

    @Field(index = org.hibernate.search.annotations.Index.UN_TOKENIZED)
    @Column(name = "SORT_NO")
    public int getSortNo() {
        return sortNo;
    }

    public void setOrigTranslations(Collection<LabelTranslation> origTranslations) {
        this.origTranslations = origTranslations;
    }


    @OneToMany
    public Collection<LabelTranslation> getOrigTranslations() {
        return origTranslations;
    }

    public LabelTranslation getOrigTranslation(Long languageId) {
        if (origTranslations == null) return null;
        for (LabelTranslation trans : origTranslations) {
            if (trans.getLanguage().getId().equals(languageId)) return trans;
        }
        return null;
    }

    public LabelTranslation getOrigTranslation(Language language) {
        return getOrigTranslation(language.getId());
    }

    public LabelTranslation getOrigTranslation(String languageCode) {
        if (origTranslations == null) return null;
        for (LabelTranslation trans : origTranslations) {
            if (trans.getLanguageCode().equals(languageCode)) {
                return trans;
            }
        }
        return null;
    }

    public void addOrigTranslation(LabelTranslation trans) {
        if (origTranslations == null) {
            origTranslations = new HashSet<LabelTranslation>();
        }
        origTranslations.add(trans);
    }

    // transient variable for REST service
    private LabelTranslation ot;
    private Translation ct;
    private Integer t;
    private Integer n;
    private Integer i;
    private Application app;
    private Product prod;

    @Transient
    public LabelTranslation getOt() {
        return ot;
    }

    public void setOt(LabelTranslation ot) {
        this.ot = ot;
    }

    @Transient
    public boolean isTranslated(DictionaryLanguage dl) {
        return Translation.STATUS_TRANSLATED == getTranslationStatus(dl.getLanguageCode());

    }

    @Transient
    public Translation getCt() {
        return ct;
    }

    public void setCt(Translation ct) {
        this.ct = ct;
    }

    public Integer getT() {
        return t;
    }

    public void setT(Integer t) {
        this.t = t;
    }

    public Integer getN() {
        return n;
    }

    public void setN(Integer n) {
        this.n = n;
    }

    public Integer getI() {
        return i;
    }

    public void setI(Integer i) {
        this.i = i;
    }


    public Translation getTranslationObject(DictionaryLanguage dl) {
        if (context.getName().equals(Context.EXCLUSION)) {
            return getTransientTranslation(Translation.STATUS_TRANSLATED);
        }

        LabelTranslation lt = getOrigTranslation(dl.getLanguageCode());
        if (lt != null && (!lt.isNeedTranslation())) {
            return getTransientTranslation(Translation.STATUS_TRANSLATED, lt.getOrigTranslation());
        }
        Translation trans = getText().getTranslation(dl.getLanguage().getId());
        if (trans == null) {
        	trans = getTransientTranslation(Translation.STATUS_UNTRANSLATED);
        }
        return trans;
    }

    public Translation getTranslationObject(String langCode) {
        //TODO: null == dl need to be fixed
        DictionaryLanguage dl = this.getDictionary().getDictLanguage(langCode);
        if (null == dl) return null;
        return getTranslationObject(dl);
    }
    
    public Translation getTranslationObject(Long languageId) {
    	DictionaryLanguage dl = this.getDictionary().getDictLanguage(languageId);
    	if (dl != null) {
    		return getTranslationObject(dl);
    	} else {	// although the language doesn't exist in the dictionary, return corresponding Translation object
            Translation trans = getText().getTranslation(languageId);
            if (trans == null) {
            	trans = getTransientTranslation(Translation.STATUS_UNTRANSLATED);
            }
            return trans;
    	}
    }

    private Translation getTransientTranslation(int status) {
        return getTransientTranslation(status, null);
    }

    private Translation getTransientTranslation(int status, String translation) {
        Translation tempTrans = new Translation();
        tempTrans.setId(-1L);  //virtual id < 0, indicating a non-existing translation object
        tempTrans.setStatus(status);
        tempTrans.setTranslation(StringUtils.defaultString(translation, reference));
        return tempTrans;
    }

    /**
     * Calculate final translation of the label
     *
     * @param langCode language code
     * @return
     */
    public String getTranslation(String langCode) {
        Translation translation = getTranslationObject(langCode);
        if (null == translation) return reference;
        return translation.getTranslation();
    }

    public int getTranslationStatus(String langCode) {
        return getTranslationObject(langCode).getStatus();
    }

    /**
     * The follow properties are used for xml dict parser and generator
     */
    private Map<String, String> params = new HashMap<String, String>();

    @ElementCollection
    @CollectionTable(name = "LABEL_PARAMS", joinColumns = @JoinColumn(name = "LABEL_ID"))
    @MapKeyColumn(name = "name")
    @Column(name = "value")
    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public void addParam(String key, String value) {
        params.put(key, value);
    }

    @Field
    public boolean isRemoved() {
        return removed;
    }

    public void setRemoved(boolean removed) {
        this.removed = removed;
    }

    @Transient
    public Application getApp() {
        return app;
    }

    public void setApp(Application app) {
        this.app = app;
    }

    @Transient
    public Product getProd() {
        return prod;
    }

    public void setProd(Product prod) {
        this.prod = prod;
    }

	public Integer getCapitalization() {
		return capitalization;
	}

	public void setCapitalization(Integer capitalization) {
		this.capitalization = capitalization;
	}


    @Override
    public Label clone(){
        Label label = null;
        try {
            label = (Label) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return label;
    }
}
