package com.alcatel_lucent.dms.service.generator;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.Constants;
import com.alcatel_lucent.dms.SystemError;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.DictionaryLanguage;
import com.alcatel_lucent.dms.model.Label;
import com.alcatel_lucent.dms.model.LabelTranslation;
import com.alcatel_lucent.dms.service.DaoService;
import com.alcatel_lucent.dms.service.parser.AndroidXMLParser;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.plexus.util.FileUtils;
import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.HashSet;

@Component
public class AndroidXMLGenerator extends DictionaryGenerator {

    private static Logger log = LoggerFactory.getLogger(AndroidXMLGenerator.class);
    @Autowired
    private DaoService dao;

    @Override
    public void generateDict(File target, Long dictId) throws BusinessException {
        if (target.exists()) {
            if (target.isFile()) {
                throw new BusinessException(BusinessException.TARGET_IS_NOT_DIRECTORY, target.getAbsolutePath());
            }
        } else {
            if (!target.mkdirs()) {
                throw new BusinessException(BusinessException.FAILED_TO_MKDIRS, target.getAbsolutePath());
            }
        }
        Dictionary dict = (Dictionary) dao.retrieve(Dictionary.class, dictId);

        // create reference language file
        generateAndroidXML(target, dict, null);

        // generate for each language
        if (CollectionUtils.isEmpty(dict.getDictLanguages())) return;
        for (DictionaryLanguage dl : dict.getDictLanguages()) {
            generateAndroidXML(target, dict, dl);
        }
    }

    @Override
    public Constants.DictionaryFormat getFormat() {
        return Constants.DictionaryFormat.XML_ANDROID;
    }

    /**
     * Generate a single file
     *
     * @param targetDir
     * @param dict
     * @param dl        dictionar language, null for reference
     */
    private void generateAndroidXML(File targetDir, Dictionary dict, DictionaryLanguage dl) {
        String refLangCode = AndroidXMLParser.REFERENCE_LANG_CODE;
        if (dict.getDictLanguage("GAE") != null) refLangCode = "GAE";
        if (dl != null && dl.getLanguageCode().equals(refLangCode)) {    // if dl is reference, set it to null
            dl = null;
        }
        Document doc = DocumentHelper.createDocument();
        doc.addComment("\n# " + getDMSGenSign() + " using language " + (dl == null ? refLangCode : dl.getLanguageCode()) + ".\n# Labels: " + dict.getLabelNum() + "\n");
        String dictAttributes = (dl == null ? dict.getAnnotation1() : dl.getAnnotation1());
        String dictComments = (dl == null ? dict.getAnnotation2() : dl.getAnnotation2());
        String dictNamespaces = (dl == null ? dict.getAnnotation3() : dl.getAnnotation3());
        String processingInstructions = (dl == null ? dict.getAnnotation4() : dl.getAnnotation4());
        if (dictComments != null) {
            String[] comments = dictComments.split("\n");
            for (String comment : comments) {
                comment = StringEscapeUtils.unescapeJava(comment);
                doc.addComment(comment);
            }
        }
        Element eleLabels = doc.addElement("resources");
        if (dictNamespaces != null) {
            String[] nsList = dictNamespaces.split("\n");
            for (String ns : nsList) {
                String[] keyValue = ns.split("=", 2);
                if (keyValue.length == 2) {
                    eleLabels.addNamespace(keyValue[0], keyValue[1]);
                }
            }
        }
        if (dictAttributes != null) {
            String[] attributes = dictAttributes.split("\n");
            for (String entry : attributes) {
                String[] keyValue = entry.split("=", 2);
                if (keyValue.length == 2) {
                    eleLabels.addAttribute(keyValue[0], keyValue[1]);
                }
            }
        }
        for (Label label : dict.getAvailableLabels()) {
            writeLabel(eleLabels, label, dl);
        }

        // output
        String filename = getFileName(dict.getName(), dl);
        OutputFormat format = OutputFormat.createPrettyPrint();
        XMLWriter output = null;
        try {
            File targetFile = new File(targetDir, filename);
            if (!targetFile.getParentFile().exists()) {
                targetFile.getParentFile().mkdirs();
            }
            output = new XMLWriter(new FileWriter(targetFile), format);
            if (processingInstructions != null) {
                String[] piList = processingInstructions.split("\n");
                for (String pi : piList) {
                    String[] keyValue = pi.split("=", 2);
                    if (keyValue.length == 2) {
                        output.processingInstruction(keyValue[0], keyValue[1]);
                    }
                }
            }
            output.write(doc);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.toString());
            throw new SystemError(e);
        } finally {
            if (output != null) try {
                output.close();
            } catch (Exception e) {
            }
        }
    }

    private void writeLabel(Element eleLabels, Label label, DictionaryLanguage dl) {
        String text = label.getReference();
        String annotation1 = label.getAnnotation1();    // attributes
        String annotation2 = label.getAnnotation2();    // leading comments
        if (dl != null) {
            LabelTranslation lt = label.getOrigTranslation(dl.getLanguageCode());
            if (lt != null) {
                annotation1 = lt.getAnnotation1();
                annotation2 = lt.getAnnotation2();
            }
            text = label.getTranslation(dl.getLanguageCode());
        }
        // add leading comments
        if (annotation2 != null) {
            String[] comments = annotation2.split("\n");
            for (String comment : comments) {
                if (StringUtils.isBlank(comment)) {
                    eleLabels.addText("\n");
                } else
                    eleLabels.addComment(StringEscapeUtils.unescapeJava(comment));
            }
        }
        String lblKey = label.getKey();
        if (lblKey.startsWith(AndroidXMLParser.ELEMENT_STRING_ARRAY) || lblKey.startsWith(AndroidXMLParser.ELEMENT_PLURALS)) {
            String[] tokens = lblKey.split(AndroidXMLParser.KEY_SEPARATOR);
            String elemName = tokens[0];
            String key = tokens[1];
            String xpath = String.format("%s[@%s='%s']", elemName, AndroidXMLParser.getKeyAttributeName(), key);
            Element subElem = (Element) eleLabels.selectSingleNode(xpath);
            if (null == subElem) {
                subElem = eleLabels.addElement(elemName);
                subElem.addAttribute(AndroidXMLParser.getKeyAttributeName(), key);
            }

            Element elemItem = subElem.addElement("item");
            addAttributesForElement(annotation1, elemItem);
            elemItem.addText(text);

        } else {
            // create label
            Element eleLabel = eleLabels.addElement("string");
            eleLabel.addAttribute("name", label.getKey());
            addAttributesForElement(annotation1, eleLabel);
            eleLabel.addText(text);
            if (text.indexOf('\n') != -1) {    // preserve line breaks among the text
                eleLabel.addAttribute(QName.get("space", Namespace.XML_NAMESPACE), "preserve");
            }
        }
    }

    private Element addAttributesForElement(String strAttributes, Element element) {
        if (StringUtils.isEmpty(strAttributes)) return element;
        String[] attributes = strAttributes.split("\n");
        for (String entry : attributes) {
            String[] keyValue = entry.split("=", 2);
            if (keyValue.length == 2) {
                element.addAttribute(keyValue[0], keyValue[1]);
            }
        }
        return element;
    }


    private String getFileName(String dictName, DictionaryLanguage dl) {
        if (null == dl) return dictName;
        String[] tokens = dictName.split("/");
        String leftPart = StringUtils.join(ArrayUtils.subarray(tokens, 0, tokens.length - 1), "/");
        String rightPart = tokens[tokens.length - 1];
        return leftPart + AndroidXMLParser.LANG_CODE_SEPARATOR + dl.getLanguageCode() + "/" + rightPart;
    }
}
