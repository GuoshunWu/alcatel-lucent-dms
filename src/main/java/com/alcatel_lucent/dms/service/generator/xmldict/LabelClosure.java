package com.alcatel_lucent.dms.service.generator.xmldict;

import com.alcatel_lucent.dms.model.Label;
import com.alcatel_lucent.dms.model.LabelTranslation;
import com.alcatel_lucent.dms.util.Util;
import org.apache.commons.collections.Closure;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.IterableMap;
import org.apache.commons.collections.MapIterator;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import java.util.Collection;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.center;

/**
 * Created by IntelliJ IDEA.
 * User: guoshunw
 * Date: 12-12-25
 * Time: 下午2:29
 * To change this template use File | Settings | File Templates.
 */
public class LabelClosure implements Closure {

    private static final Logger log = Logger.getLogger(LabelClosure.class);
    private Element xmlDict;
    private LabelTranslationClosure labelTranslationClosure = new LabelTranslationClosure();

    private int labelCounter = 0;

    public LabelClosure(Element xmlDict) {
        this.xmlDict = xmlDict;
    }

    public void execute(Object input) {
        final Label label = (Label) input;
        log.trace(center("Writing label " + label.getKey(), 100, '='));

        labelCounter++;
        System.out.print('.');
        if (labelCounter % 150 == 0) {
            System.out.println();
        }
        final Element xmlKey = xmlDict.addElement("KEY");
        labelTranslationClosure.setXmlKey(xmlKey);

        xmlKey.addAttribute("name", label.getKey());
        Map<String, String> map = Util.string2Map(label.getAnnotation1());

        if (null != map.get("columns")) xmlKey.addAttribute("columns", map.get("columns"));
        if (null != map.get("lines")) xmlKey.addAttribute("lines", map.get("lines"));
        if (null != map.get("message_category"))
            xmlKey.addAttribute("message_category", map.get("message_category"));
        if (null != map.get("gui_object")) xmlKey.addAttribute("gui_object", map.get("gui_object"));
        if (null != map.get("state")) xmlKey.addAttribute("state", map.get("state"));

        Collection<LabelTranslation> labelTranslations = label.getOrigTranslations();

//              write all the comment
        writeElement("COMMENT", map.get("comment"), xmlKey, labelTranslations, null);
        writeElement("CONTEXT", map.get("context"), xmlKey, labelTranslations, null);
        writeElement("HELP", map.get("help"), xmlKey, labelTranslations, map.get("follow_up"));

        IterableMap params = label.getParams();
        MapIterator it = params.mapIterator();
        while (it.hasNext()) {
            Object key = it.next();
            Element param = xmlKey.addElement("PARAM");
            param.addAttribute("name", (String) key);
            param.addAttribute("value", (String) it.getValue());
        }


        String elemName = "STATIC_TOKEN";
        String value = label.getValueFromField("STATIC_TOKEN", Label.ANNOTATION2);
        if (null != value) {
            String[] sts = value.split(";");
            for (String st : sts) {
                Element staticToken = xmlKey.addElement(elemName);
                if (!st.isEmpty()) staticToken.setText(st);
            }
        }

        writeElement("TRANSLATION", label.getReference(), xmlKey, labelTranslations, map.get("follow_up"));

    }

    /**
     * Add the elements in xmlKey element, include those elements store in label and labelTranslations
     *
     * @param elemName          The Element name
     * @param value             The Element text value
     * @param xmlKey            The parent Element
     * @param labelTranslations the LabelTranslation in this label
     * @param followUp          the follow_up attribute in HELP and TRANSLATION Element, null for others
     */
    private void writeElement(String elemName, String value, Element xmlKey, Collection<LabelTranslation> labelTranslations, String followUp) {
        if (null != value) {
            Element element = xmlKey.addElement(elemName);
            if (followUp != null) {
                element.addAttribute("follow_up", followUp);
            }
            element.addAttribute("language", "GAE");
            if (!value.isEmpty()) element.setText(value);
        }
        labelTranslationClosure.setName(elemName);
        CollectionUtils.forAllDo(labelTranslations, labelTranslationClosure);
    }
}
