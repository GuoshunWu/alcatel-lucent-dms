package com.alcatel_lucent.dms.service.generator.xmldict;

import com.alcatel_lucent.dms.model.LabelTranslation;
import org.apache.commons.collections.Closure;
import org.apache.commons.lang3.BooleanUtils;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;

import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: guoshunw
 * Date: 12-12-25
 * Time: 下午2:20
 */
public class LabelTranslationClosure implements Closure {
    private String name;
    private Element xmlKey;

    public LabelTranslationClosure() {
    }

    public void setName(String name) {
        this.name = name;
    }

    public LabelTranslationClosure(String name, Element xmlKey) {
        this.name = name;
        this.xmlKey = xmlKey;
    }

    public Element getXmlKey() {
        return xmlKey;
    }

    public void setXmlKey(Element xmlKey) {
        this.xmlKey = xmlKey;
    }

    @Override
    public void execute(Object input) {
        LabelTranslation lt = (LabelTranslation) input;
        String xpath = "parent::*/LANGUAGE[@id='" + lt.getLanguageCode() + "']/@is_context";
        boolean isContext = BooleanUtils.toBoolean(xmlKey.selectSingleNode(xpath).getStringValue());

        String value = name.equals("TRANSLATION") ? lt.getOrigTranslation() : lt.getValueFromField(name.toLowerCase(), LabelTranslation.ANNOTATION1);
        if (null == value || (name.equals("CONTEXT") && !isContext)) return;

        Element element = xmlKey.addElement(name);
        if (!value.isEmpty()) element.addCDATA(value);

        String followUp = lt.getValueFromField("follow_up", LabelTranslation.ANNOTATION1);
        if (Arrays.asList("HELP", "TRANSLATION").contains(name) && null != followUp)
            element.addAttribute("follow_up", followUp);

        element.addAttribute("language", lt.getLanguageCode());
    }
}
