package com.alcatel_lucent.dms.service.generator;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.Constants;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.DictionaryLanguage;
import com.alcatel_lucent.dms.model.Label;
import com.alcatel_lucent.dms.model.LabelTranslation;
import com.alcatel_lucent.dms.service.DaoService;
import com.alcatel_lucent.dms.service.parser.VoiceAppParser;
import com.alcatel_lucent.dms.service.parser.XMLHelpParser;
import com.alcatel_lucent.dms.util.Util;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
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
import static org.apache.commons.lang3.StringUtils.split;

@Component
public class XmlHelpGenerator extends DictionaryGenerator {
    private static Logger log = LoggerFactory.getLogger(XmlHelpGenerator.class);
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
        return Constants.DictionaryFormat.XML_Help;
    }

    public void generateDict(File targetDir, Dictionary dict) throws BusinessException {
        XMLWriter writer = null;
        try {
            String fileName = Util.string2Map(dict.getAnnotation1()).get("filename");
            OutputStream fos = FileUtils.openOutputStream(new File(targetDir, fileName));
            OutputFormat format = OutputFormat.createPrettyPrint();
            format.setIndentSize(4);
            format.setXHTML(true);
            format.setExpandEmptyElements(false);

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

    /**
     * Get parent element of specific label key
     */
    private Element getParent(String key, Element root) {
        String[] keyToken = split(key, XMLHelpParser.KEY_SEPARATOR);
        if (1 == keyToken.length) return root;

        int index = 0;
        StringBuilder sb = new StringBuilder("ITEM[@name='" + keyToken[index++] + "']");
        for (; index < keyToken.length - 1; index++) {
            sb.append("/ITEM[@name='" + keyToken[index] + "']");
        }
        return (Element) root.selectSingleNode(sb.toString());
    }

    public Document generateDocument(final Dictionary dict) {
        Document doc = DocumentHelper.createDocument();
        doc.setXMLEncoding(dict.getEncoding());

        if (null == dict) return doc;

        doc.addComment(StringUtils.center(getDMSGenSign(), 50, '='));

        doc.addDocType("HELP_DOCUMENT", "", "XmlHelp.dtd");
        final Element xmlDict = doc.addElement("HELP_DOCUMENT");
        xmlDict.addAttribute("name", dict.getName());

        //add languages and language ref
        Collection<DictionaryLanguage> dictionaryLanguages = dict.getDictLanguages();
        for (DictionaryLanguage dictionaryLanguage : dictionaryLanguages) {
            String elemName = "LANGUAGE";
            if (dictionaryLanguage.getLanguageCode().equals(dict.getReferenceLanguage())) {
                elemName += "_REF";
            }
            Element elemLang = xmlDict.addElement(elemName);
            elemLang.addAttribute("id", dictionaryLanguage.getLanguageCode());
        }

        // all the entries
        Collection<Label> availableLabels = dict.getAvailableLabels();
        for (Label label : availableLabels) {
            // it is a help label
            boolean isHelpLabel = label.getKey().endsWith(XMLHelpParser.KEY_HELP_SIGN);
            if (isHelpLabel) continue;
            Element elemParent = getParent(label.getKey(), xmlDict);
//            // elemParent may haven't created yet
            Element elemItem = elemParent.addElement("ITEM");
            addAttributes(elemItem, Util.string2Map(label.getAnnotation1()));

            for (DictionaryLanguage dictionaryLanguage : dictionaryLanguages) {
                Element elemTrans = elemItem.addElement("TRANSLATION");
                String langCode = dictionaryLanguage.getLanguageCode();
                boolean isRef = langCode.equals(dict.getReferenceLanguage());

                Map<String, String> annotationMap = Util.string2Map(isRef ?
                        label.getAnnotation2() : label.getOrigTranslation(langCode).getAnnotation1());

                String text = isRef ? label.getReference() : label.getTranslation(langCode);
//                annotationMap.remove("HELP");
                addAttributes(elemTrans, annotationMap);

                elemTrans.addElement("LABEL").addCDATA(text);
                Element elemHelp = elemTrans.addElement("HELP");

//                String help = annotationMap.get("HELP");
//                if (!StringUtils.isEmpty(help)) {
//                    elemHelp.addCDATA(help);
//                }

                Label keyLabel = dict.getLabel(label.getKey() + XMLHelpParser.KEY_HELP_SIGN);
                if (null != keyLabel) {
                    elemHelp.addCDATA(keyLabel.getTranslation(langCode));
                }
            }
        }
        return doc;
    }

    private void addAttributes(Element element, Map<String, String> annotationMap) {
        Set<Map.Entry<String, String>> entries = annotationMap.entrySet();
        for (Map.Entry<String, String> entry : entries) {
            element.addAttribute(entry.getKey(), entry.getValue());
        }
    }

}
