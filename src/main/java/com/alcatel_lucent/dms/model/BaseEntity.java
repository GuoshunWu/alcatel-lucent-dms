package com.alcatel_lucent.dms.model;

import com.alcatel_lucent.dms.util.Util;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.Map;

//@MappedSuperclass
public abstract class BaseEntity implements Serializable {

    protected Logger log = LoggerFactory.getLogger(getClass());

    public static final String ANNOTATION1 = "annotation1";
    public static final String ANNOTATION2 = "annotation2";
    public static final String ANNOTATION3 = "annotation3";
    public static final String ANNOTATION4 = "annotation4";

    /**
     *
     */
    private static final long serialVersionUID = -3756598069703614852L;

    @Transient
    protected Logger getLog() {
        return log;
    }

    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public void putKeyValuePairToField(String key, String value, String fieldName) {
        String annotationValue = null;
        try {
            annotationValue = (String) BeanUtils.getSimpleProperty(this, fieldName);
            Map<String, String> map = Util.string2Map(annotationValue);
            map.put(key, value);
            BeanUtils.setProperty(this, fieldName, Util.map2String(map));
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Field name " + fieldName + " does not exist in object.");
        }
    }

    public String getValueFromField(String key, String fieldName) {
        String annotationValue = null;
        try {
            annotationValue = (String) BeanUtils.getSimpleProperty(this, fieldName);
            Map<String, String> map = Util.string2Map(annotationValue);
            return map.get(key);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Field name " + fieldName + " does not exist in object.");
            return null;
        }
    }
}
