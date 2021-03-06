package com.alcatel_lucent.dms.service.generator;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.Constants;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.DictionaryLanguage;
import com.alcatel_lucent.dms.model.Label;
import com.alcatel_lucent.dms.service.DaoService;
import com.alcatel_lucent.dms.service.parser.ICEJavaAlarmParser;
import com.alcatel_lucent.dms.util.Util;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.hibernate.FetchMode;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.XMLConstants;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.center;

@Component
public class ICEJavaAlarmGenerator extends DictionaryGenerator {
    private static Logger log = LoggerFactory.getLogger(ICEJavaAlarmGenerator.class);
    @Autowired
    private DaoService dao;

    @Override
    public void generateDict(File targetDir, Long dictId, GeneratorSettings settings) throws BusinessException {
//        Dictionary dict = (Dictionary) dao.retrieve(Dictionary.class, dictId);
//        Improving performance
        long begin = System.currentTimeMillis();
        Dictionary dict = (Dictionary) dao.getSession().
                createCriteria(Dictionary.class).
                add(Restrictions.idEq(dictId)).
                setFetchMode("labels", FetchMode.JOIN).
                setCacheable(true).
//                setFetchMode("labels.params", FetchMode.SELECT).
//                setFetchMode("labels.origTranslations", FetchMode.DEFAULT).
//                setFetchMode("labels.text", FetchMode.DEFAULT).
//                setFetchMode("labels.text.translations", FetchMode.DEFAULT).
        uniqueResult();
        long end = System.currentTimeMillis();
        String timeStr = DurationFormatUtils.formatPeriod(begin, end, "mm 'minute(s)' ss 'second(s)'.");
        log.info(center("Querying dictionary " + dict.getName() + " using a total of " + timeStr, 100, '*'));

        generateDict(targetDir, dict);
    }

    @Override
    public Constants.DictionaryFormat getFormat() {
        return Constants.DictionaryFormat.ICE_JAVA_ALARM;
    }

    public void generateDict(File targetDir, Dictionary dict) throws BusinessException {
        XMLWriter writer = null;
        try {
            OutputStream fos = FileUtils.openOutputStream(new File(targetDir, dict.getName()));
            OutputFormat format = OutputFormat.createPrettyPrint();
            format.setIndentSize(4);
            format.setXHTML(true);

            log.info(center("Start generating dictionary " + dict.getName() + "...", 100, '='));
            writer = new XMLWriter(new BufferedOutputStream(fos), format);
            writer.write(generateDocument(dict));

        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            try {
                if (null != writer) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public Document generateDocument(final Dictionary dict) {
        Document doc = DocumentHelper.createDocument();
        doc.setXMLEncoding(dict.getEncoding());
        if (null == dict) return doc;

        doc.addComment(StringUtils.center(getDMSGenSign(), 50, '='));


        final Element xmlDict = doc.addElement("catalog");
        xmlDict.addNamespace("xsi", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
        // catalog attributes
        Map<String, String> attributes = Util.string2Map(dict.getAnnotation1());
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            xmlDict.addAttribute(entry.getKey(), entry.getValue());
        }

        // catalog children element that need to be translated
        attributes = Util.string2Map(dict.getAnnotation2());
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            Element element = xmlDict.addElement(entry.getKey());
            element.setText(entry.getValue());
        }

        // all the alarms
        Collection<Label> availableLabels = dict.getAvailableLabels();
        for (Label label : availableLabels) {
            String labelKey = label.getKey();
            // if it is an additional label for attributesDesc or proposedRepairAction
            if (-1 != labelKey.indexOf(ICEJavaAlarmParser.ALARM_LABEL_GROUP_SEPARATOR)) {
                continue;
            }
            String constantName = null;
            Element alarm = null;
            Map<String, String> labelMap = null;
            constantName = labelKey;
            alarm = xmlDict.addElement("alarm");
            labelMap = Util.string2Map(label.getAnnotation2());

            alarm.addElement("alarmId").setText(labelMap.get("alarmId"));
            alarm.addElement("constantName").setText(label.getKey());

            //Add choice element in alarm
            String[] strChoiceElemToken = label.getAnnotation1().split(":");
            Element choiceElem = alarm.addElement(strChoiceElemToken[0]);
            String[] strSubElements = strChoiceElemToken[1].split(";");
            for (String strSubElement : strSubElements) {
                String[] subElement = strSubElement.split("=");
                choiceElem.addElement(subElement[0]).setText(subElement[1]);
            }

            alarm.addElement("perceivedSeverity").setText(labelMap.get("perceivedSeverity"));

            Collection<DictionaryLanguage> dictionaryLanguages = dict.getDictLanguages();

            Element elemText = alarm.addElement("text");
            parseTranslationElement(elemText, label);


            Label attrDescLabel = dict.getLabel(labelKey + ICEJavaAlarmParser.ALARM_LABEL_GROUP_SEPARATOR + "attributesDesc");
            if (null != attrDescLabel) {
//              add attributesDesc
                parseTranslationElement(alarm.addElement("attributesDesc"), attrDescLabel);
            }

            Label proposeRepairLabel = dict.getLabel(labelKey + ICEJavaAlarmParser.ALARM_LABEL_GROUP_SEPARATOR + "proposedRepairAction");
            if (null != proposeRepairLabel) {
//                proposedRepairAction here
                parseTranslationElement(alarm.addElement("proposedRepairAction"), proposeRepairLabel);
            }

            String clearAlarms = labelMap.get("clearAlarms");
            if (null != clearAlarms) {
                Element elemClearAlarms = alarm.addElement("clearAlarms");
                String[] alarmIds = clearAlarms.split(",");
                for (String alarmId : alarmIds) {
                    elemClearAlarms.addElement("alarmId").setText(alarmId);
                }
            }

        }
        return doc;
    }

    private void parseTranslationElement(Element element, Label label) {
        Collection<DictionaryLanguage> dictionaryLanguages = label.getDictionary().getDictLanguages();

        element.addElement(ICEJavaAlarmParser.REFERENCE_LANG_CODE).setText(label.getReference());
        for (DictionaryLanguage dictionaryLanguage : dictionaryLanguages) {
            element.addElement(dictionaryLanguage.getLanguageCode()).setText(label.getTranslation(dictionaryLanguage.getLanguageCode()));
        }
    }
}
