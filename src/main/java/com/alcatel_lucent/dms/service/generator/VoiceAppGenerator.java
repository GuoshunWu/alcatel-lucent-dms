package com.alcatel_lucent.dms.service.generator;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.Constants;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.DictionaryLanguage;
import com.alcatel_lucent.dms.model.Label;
import com.alcatel_lucent.dms.model.LabelTranslation;
import com.alcatel_lucent.dms.service.DaoService;
import com.alcatel_lucent.dms.service.parser.ICEJavaAlarmParser;
import com.alcatel_lucent.dms.service.parser.VoiceAppParser;
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
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.center;

@Component
public class VoiceAppGenerator extends DictionaryGenerator {
    private static Logger log = LoggerFactory.getLogger(VoiceAppGenerator.class);
    @Autowired
    private DaoService dao;

    @Override
    public void generateDict(File targetDir, Long dictId) throws BusinessException {
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
        return Constants.DictionaryFormat.VOICE_APP;
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
            e.printStackTrace();
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


        // all the entries
        Collection<Label> availableLabels = dict.getAvailableLabels();
        for (Label label : availableLabels) {
            Element elemEntry = xmlDict.addElement("entry");

            Element elemKey = elemEntry.addElement("key");
            elemKey.setText(label.getKey());
            Map<String, String> keyAttires = Util.string2Map(label.getAnnotation1());
            Set<Map.Entry<String, String>> attiresEntries = keyAttires.entrySet();
            for (Map.Entry<String, String> aEntry : attiresEntries) {
                elemKey.addAttribute(aEntry.getKey(), aEntry.getValue());
            }

            //restore usage element
            if (StringUtils.isNotEmpty(label.getDescription())) {
                elemEntry.addElement("usage").setText(label.getDescription());
            }

            //restore message element
            Collection<DictionaryLanguage> dictionaryLanguages = dict.getDictLanguages();
            String langCode;

            for (DictionaryLanguage dictionaryLanguage : dictionaryLanguages) {
                Element elemMsg = elemEntry.addElement("message");
                langCode = dictionaryLanguage.getLanguageCode();
                Map<String, String> annotationMap = null;

                elemMsg.addAttribute("lang", langCode);
                LabelTranslation labelTranslation = null;
                String translation = null;
                if (VoiceAppParser.REFERENCE_LANG_CODE.equals(langCode)) {
                    annotationMap = Util.string2Map(label.getAnnotation2());
                    translation = label.getReference();
                } else if (null != (labelTranslation = label.getOrigTranslation(langCode))) {
                    annotationMap = Util.string2Map(labelTranslation.getAnnotation1());
                    translation = label.getTranslation(langCode);
                }
                if (null == annotationMap) continue;

                String translate = annotationMap.get("translate");
                if (StringUtils.isNotEmpty(translate)) {
                    elemMsg.addAttribute("translate", translate);
                }
                String comment = annotationMap.get("comment");
                if (StringUtils.isNotEmpty(comment)) {
                    elemMsg.addElement("comment").setText(comment);
                }

                if (StringUtils.isNotEmpty(translation)) {
                    elemMsg.addElement("phrase").setText(translation);
                }
            }

        }
        String annotation2 = dict.getAnnotation2();
        String comment;
        if (StringUtils.isNotEmpty(annotation2) && null != (comment = Util.string2Map(annotation2).get("comment"))) {
            xmlDict.addElement("comment").setText(comment);
        }

        return doc;
    }

}
