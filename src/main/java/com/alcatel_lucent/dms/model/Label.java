package com.alcatel_lucent.dms.model;

import com.alcatel_lucent.dms.SystemError;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

public class Label extends BaseEntity {
    /**
     *
     */
    private static final long serialVersionUID = -4086873912554236932L;
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

    private String annotation1;
    private String annotation2;
    private Collection<LabelTranslation> origTranslations;


    public void addLabelTranslation(LabelTranslation labelTranslation) {
        this.origTranslations.add(labelTranslation);
    }

    public String getAnnotation2() {
        return annotation2;
    }

    public void setAnnotation2(String annotation2) {
        this.annotation2 = annotation2;
    }

    public String getAnnotation1() {
        return annotation1;
    }

    public void setAnnotation1(String annotation1) {
        this.annotation1 = annotation1;
    }

    public Dictionary getDictionary() {
        return dictionary;
    }

    public void setDictionary(Dictionary dictionary) {
        this.dictionary = dictionary;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(String maxLength) {
        this.maxLength = maxLength;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

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

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        Label other = (Label) obj;
        if (context == null) {
            if (other.context != null)
                return false;
        } else if (!context.equals(other.context))
            return false;
        if (key == null) {
            if (other.key != null)
                return false;
        } else if (!key.equals(other.key))
            return false;
        return true;
    }

    /**
     * Check if text meets max length constraint of the label
     *
     * @param text
     * @return
     */
    public boolean checkLength(String text) {
        if (StringUtils.isEmpty(maxLength)) {
            return true;    // no constraint
        }
        String[] texts = text.split("\n");

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

        String[] lens = maxLength.split(",");
        for (int rowIndex = 0; rowIndex < texts.length; rowIndex++) {
            try {
                if (rowIndex >= lens.length || texts[rowIndex].getBytes("ISO-8859-1").length > Integer.parseInt(lens[rowIndex])) {
                    return false;
                }
            } catch (NumberFormatException e) {
                throw new SystemError(e);
            } catch (UnsupportedEncodingException e) {
                throw new SystemError(e);
            }
        }
        return true;
    }

    public void setSortNo(int sortNo) {
        this.sortNo = sortNo;
    }

    public int getSortNo() {
        return sortNo;
    }

    public void setOrigTranslations(Collection<LabelTranslation> origTranslations) {
        this.origTranslations = origTranslations;
    }

    public Collection<LabelTranslation> getOrigTranslations() {
        return origTranslations;
    }

    public LabelTranslation getOrigTranslation(String languageCode) {
        if (origTranslations != null) {
            for (LabelTranslation trans : origTranslations) {
                if (trans.getLanguageCode().equals(languageCode)) {
                    return trans;
                }
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

    public LabelTranslation getOt() {
        return ot;
    }

    public void setOt(LabelTranslation ot) {
        this.ot = ot;
    }

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

    /**
     * Calculate final translation of the label
     *
     * @param langCode language code
     * @return
     */
    public String getTranslation(String langCode) {
        DictionaryLanguage dl = this.getDictionary().getDictLanguage(langCode);
        LabelTranslation lt = getOrigTranslation(langCode);
        if (lt != null && (!lt.isNeedTranslation() || context.getName().equals(Context.EXCLUSION))) {
            return lt.getOrigTranslation();
        } else {
            for (Translation translation : getText().getTranslations()) {
                if (translation.getLanguage().getId().equals(dl.getLanguage().getId())) {
                    return translation.getTranslation();
                }
            }
        }
        return reference;
    }

    /**
     * The follow properties are used for xml dict parser and generator
     */
    private Map<String, String> params = new HashMap<String, String>();

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public void addParam(String key, String value) {
        params.put(key, value);
    }
}
