package com.alcatel_lucent.dms.service.generator.xmldict;

import com.alcatel_lucent.dms.model.DictionaryLanguage;
import com.alcatel_lucent.dms.model.Label;
import com.alcatel_lucent.dms.model.LabelTranslation;
import com.alcatel_lucent.dms.service.DaoService;
import com.alcatel_lucent.dms.util.Util;
import org.apache.commons.collections.*;
import org.hibernate.FetchMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.center;

/**
 * Created by IntelliJ IDEA.
 * User: guoshunw
 * Date: 12-12-25
 * Time: 下午2:29
 * To change this template use File | Settings | File Templates.
 */
public class LabelClosure implements Closure {

    private static final Logger log = LoggerFactory.getLogger(LabelClosure.class);
    private Element xmlDict;
    private LabelTranslationClosure labelTranslationClosure = new LabelTranslationClosure();

    private DaoService dao;

    private int labelCounter = 0;
    private int totalLabel;

    public LabelClosure(Element xmlDict, int totalLabel, DaoService dao) {
        this.xmlDict = xmlDict;
        this.totalLabel = totalLabel;
        this.dao = dao;
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
        labelTranslationClosure.setXmlKey(xmlKey);

        xmlKey.addAttribute("name", label.getKey());
        Map<String, String> map = Util.string2Map(label.getAnnotation1());

        if (null != map.get("columns")) xmlKey.addAttribute("columns", map.get("columns"));
        if (null != map.get("lines")) xmlKey.addAttribute("lines", map.get("lines"));
        if (null != map.get("message_category"))
            xmlKey.addAttribute("message_category", map.get("message_category"));
        if (null != map.get("gui_object")) xmlKey.addAttribute("gui_object", map.get("gui_object"));
        if (null != map.get("state")) xmlKey.addAttribute("state", map.get("state"));

        /**
         * TODO: We need iterate dictionary language to get all the additional added language after the
         * dictionary had been imported to DMS.
         * */
        Collection<DictionaryLanguage> dictionaryLanguages= label.getDictionary().getDictLanguages();
        for(DictionaryLanguage dictionaryLanguage: dictionaryLanguages){

        }


        Collection<LabelTranslation> labelTranslations = label.getOrigTranslations();
//              write all the comment
        writeElement("COMMENT", map.get("comment"), xmlKey, labelTranslations, null);
        writeElement("CONTEXT", map.get("context"), xmlKey, labelTranslations, null);
        writeElement("HELP", map.get("help"), xmlKey, labelTranslations, map.get("follow_up"));

        Set<Map.Entry<String, String>> params = label.getParams().entrySet();
        for (Map.Entry<String, String> param : params) {
            Element eParam = xmlKey.addElement("PARAM");
            eParam.addAttribute("name", param.getKey());
            eParam.addAttribute("value", param.getValue());
        }

        String elemName = "STATIC_TOKEN";
        String value = label.getValueFromField("STATIC_TOKEN", Label.ANNOTATION2);
        if (null != value) {
            String[] sts = value.split(";");
            for (String st : sts) {
                Element staticToken = xmlKey.addElement(elemName);
                if (!st.isEmpty()) staticToken.addCDATA(st);
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
            if (!value.isEmpty()) element.addCDATA(value);
        }
        labelTranslationClosure.setName(elemName);
        CollectionUtils.forAllDo(labelTranslations, labelTranslationClosure);
    }
}
