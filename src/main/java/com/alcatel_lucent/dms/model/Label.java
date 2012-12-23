package com.alcatel_lucent.dms.model;

import com.alcatel_lucent.dms.SystemError;
import com.alcatel_lucent.dms.util.Util;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.IterableMap;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.log4j.Logger;
import sun.reflect.misc.FieldUtil;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

public class Label extends BaseEntity {

    private static Logger log = Logger.getLogger(Label.class);
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
        if (maxLength == null || maxLength.isEmpty()) {
            return true;    // no constraint
        }
        String[] lens = maxLength.split(",");
        String[] texts = text.split("\n");
        for (int i = 0; i < texts.length; i++) {
            try {
                if (i >= lens.length || texts[i].getBytes("ISO-8859-1").length > Integer.parseInt(lens[i])) {
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
    private IterableMap params = new HashedMap();

    public IterableMap getParams() {
        return params;
    }

    public void setParams(IterableMap params) {
        this.params = params;
    }

    public void addParam(String key, String value) {
        params.put(key, value);
    }

    private List<String> staticTokens = new ArrayList<String>();

    public List<String> getStaticTokens() {
        return staticTokens;
    }

    public void setStaticTokens(List<String> staticTokens) {
        this.staticTokens = staticTokens;
    }

    public void addStaticToken(String staticToken) {
        this.staticTokens.add(staticToken);
    }

    public static final String annotation1Field = "annotation1";
    public static final String annotation2Field = "annotation2";

    public void putKeyValuePairToField(String key, String value, String fieldName) {
        String annotationValue = null;
        try {
            annotationValue = (String)BeanUtils.getSimpleProperty(this, fieldName);
            Map<String, String> map = Util.string2Map(annotationValue);
            map.put(key, value);
            BeanUtils.setProperty(this,fieldName,Util.map2String(map));
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Field name " + fieldName + " does not exist in object.");
        }
    }

    public String getValueFromField(String key, String fieldName) {
        String annotationValue = null;
        try {
            annotationValue = (String)BeanUtils.getSimpleProperty(this, fieldName);
            Map<String, String> map = Util.string2Map(annotationValue);
            return map.get(key);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Field name " + fieldName + " does not exist in object.");
            return null;
        }
    }
}
