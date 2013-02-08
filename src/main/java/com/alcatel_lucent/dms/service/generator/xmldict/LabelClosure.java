package com.alcatel_lucent.dms.service.generator.xmldict;

import com.alcatel_lucent.dms.model.DictionaryLanguage;
import com.alcatel_lucent.dms.model.Label;
import com.alcatel_lucent.dms.model.LabelTranslation;
import com.alcatel_lucent.dms.util.Util;
import org.apache.commons.collections.Closure;
import org.apache.commons.lang3.BooleanUtils;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.center;

/**
 * For populate each xml element that represent a label..
 * User: Guoshun WU
 * Date: 12-12-25
 * Time: 下午2:29
 */
public class LabelClosure implements Closure {

    private static final Logger log = LoggerFactory.getLogger(LabelClosure.class);
    private final Element xmlDict;
    private final int totalLabel;
    private int labelCounter = 0;

    public LabelClosure(Element xmlDict, int totalLabel) {
        this.xmlDict = xmlDict;
        this.totalLabel = totalLabel;
    }

    public void execute(Object input) {
        final Label label = (Label) input;
        log.trace(center("Writing label " + label.getKey(), 100, '='));

        labelCounter++;
        System.out.print('.');
        if (labelCounter % 150 == 0) {
            System.out.println(String.format("(%1$.2f%%)", (float) labelCounter * 100 / totalLabel));
        }
        final Element xmlKey = xmlDict.addElement("KEY");

        xmlKey.addAttribute("name", label.getKey());
        Map<String, String> map = Util.string2Map(label.getAnnotation1());

        if (null != map.get("columns")) xmlKey.addAttribute("columns", map.get("columns"));
        if (null != map.get("lines")) xmlKey.addAttribute("lines", map.get("lines"));
        if (null != map.get("message_category"))
            xmlKey.addAttribute("message_category", map.get("message_category"));
        if (null != map.get("gui_object")) xmlKey.addAttribute("gui_object", map.get("gui_object"));
        if (null != map.get("state")) xmlKey.addAttribute("state", map.get("state"));

        // write all the comment
        writeElement("COMMENT", xmlKey, label);
        writeElement("CONTEXT", xmlKey, label);
        writeElement("HELP", xmlKey, label);

        //      The PARAM does nothing to do with languages
        Set<Map.Entry<String, String>> params = label.getParams().entrySet();
        for (Map.Entry<String, String> param : params) {
            Element eParam = xmlKey.addElement("PARAM");
            eParam.addAttribute("name", param.getKey());
            eParam.addAttribute("value", param.getValue());
        }
        //      The STATIC_TOKEN does nothing to do with languages
        String elemName = "STATIC_TOKEN";
        String value = label.getValueFromField("STATIC_TOKEN", Label.ANNOTATION2);
        if (null != value) {
            String[] sts = value.split(";");
            for (String st : sts) {
                Element staticToken = xmlKey.addElement(elemName);
                if (!st.isEmpty()) staticToken.addCDATA(st);
            }
        }
        writeElement("TRANSLATION", xmlKey, label);
    }

    /**
     * Add the elements in xmlKey element, include those elements store in label and labelTranslations
     *
     * @param elemName The Element name
     * @param xmlKey   The parent Element
     * @param label    Current processed label
     */
    private void writeElement(String elemName, Element xmlKey, Label label) {
        Collection<DictionaryLanguage> dictionaryLanguages = label.getDictionary().getDictLanguages();
        Map<String, String> labelProp = Util.string2Map(label.getAnnotation1());
        // write language elements
        LabelTranslation lt;
        for (DictionaryLanguage dictionaryLanguage : dictionaryLanguages) {
            String langCode = dictionaryLanguage.getLanguageCode();
            lt = label.getOrigTranslation(langCode);

            // There is not LabelTranslation related this language. it is not required to ...
            boolean hasLabelTrans = lt != null;

            String xpath = "parent::*/LANGUAGE[@id='" + langCode + "']/@is_context";
            boolean isContext = BooleanUtils.toBoolean(xmlKey.selectSingleNode(xpath).getStringValue());

            // Get the element content from LabelTranslation.ANNOTATION1
            String elemTextValue = hasLabelTrans ? lt.getValueFromField(elemName.toLowerCase(), LabelTranslation.ANNOTATION1) : null;
            // Get reference language element content from label annotation1
            if (isContext) elemTextValue = labelProp.get(elemName.toLowerCase());

            if (elemName.equals("TRANSLATION"))
                elemTextValue = label.getTranslation(dictionaryLanguage.getLanguageCode());
            if (null == elemTextValue) continue;


            if (elemName.equals("CONTEXT") && !isContext) continue;

            Element element = xmlKey.addElement(elemName);
            if (!elemTextValue.isEmpty()) element.addCDATA(elemTextValue);

            String followUp = labelProp.get("follow_up");
            if (!isContext && hasLabelTrans) {
                followUp = lt.getValueFromField("follow_up", LabelTranslation.ANNOTATION1);
            }
            if (null != followUp
                    && Arrays.asList("HELP", "TRANSLATION").contains(elemName)) {
                element.addAttribute("follow_up", followUp);
            }
            element.addAttribute("language", langCode);
        }
    }
}
