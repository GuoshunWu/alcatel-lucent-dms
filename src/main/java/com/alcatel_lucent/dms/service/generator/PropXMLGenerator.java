package com.alcatel_lucent.dms.service.generator;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.Constants;
import com.alcatel_lucent.dms.SystemError;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.DictionaryLanguage;
import com.alcatel_lucent.dms.model.Label;
import com.alcatel_lucent.dms.model.LabelTranslation;
import com.alcatel_lucent.dms.service.DaoService;
import org.apache.commons.lang3.text.translate.LookupTranslator;
import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@Component
public class PropXMLGenerator extends DictionaryGenerator {

    private static Logger log = LoggerFactory.getLogger(PropXMLGenerator.class);
    private static final LookupTranslator convertTranslator = new LookupTranslator(new String[][]{
            {"\u0027", "\u2032"},
    });

    @Autowired
    private DaoService dao;

    @Override
    public void generateDict(File target, Long dictId, GeneratorSettings settings) throws BusinessException {
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
        generatePropXML(target, dict, null, settings);

        // generate for each language
        HashSet<String> langCodeSet = null;
        if (dict.getDictLanguages() != null) {
            for (DictionaryLanguage dl : dict.getDictLanguages()) {
                String langCode = dl.getLanguageCode();
                if (langCodeSet != null && !langCodeSet.contains(langCode)) {
                    continue;
                }
                generatePropXML(target, dict, dl, settings);
            }
        }
    }

    @Override
    public Constants.DictionaryFormat getFormat() {
        return Constants.DictionaryFormat.XML_PROP;
    }

    /**
     * Generate a single file
     *
     * @param targetDir
     * @param dict
     * @param dl        dictionar language, null for reference
     */
    private void generatePropXML(File targetDir, Dictionary dict, DictionaryLanguage dl, GeneratorSettings settings) {
        String refLangCode = "en";
        if (dict.getDictLanguage("GAE") != null) {
            refLangCode = "GAE";
        }
        if (dl != null && dl.getLanguageCode().equals(refLangCode)) {    // if dl is reference, set it to null
            dl = null;
        }
        Document doc = DocumentHelper.createDocument();
//        doc.addComment("\n# " + getDMSGenSign() + " using language " + (dl == null ? refLangCode : dl.getLanguageCode()) + ".\n# Labels: " + dict.getLabelNum() + "\n");
        String dictAttributes = (dl == null ? dict.getAnnotation1() : dl.getAnnotation1());
        String dictComments = (dl == null ? dict.getAnnotation2() : dl.getAnnotation2());
        String dictNamespaces = (dl == null ? dict.getAnnotation3() : dl.getAnnotation3());
        String processingInstructions = (dl == null ? dict.getAnnotation4() : dl.getAnnotation4());
        if (dictComments != null) {
            String[] comments = dictComments.split("\n");
            for (String comment : comments) {
                comment = comment.replace("\\n", "\n");
                comment = comment.replace("\\\\", "\\");
                doc.addComment(comment);
            }
        }
        Element eleLabels = doc.addElement("properties");
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

            if (settings.isConvertApostrophe()) {
                text = convertTranslator.translate(text);
            }
            // add leading comments
            if (annotation2 != null) {
                String[] comments = annotation2.split("\n");
                for (String comment : comments) {
                    eleLabels.addComment(comment);
                }
            }
            // create label
            Element eleLabel = eleLabels.addElement("entry");
            eleLabel.addAttribute("key", label.getKey());
            if (annotation1 != null) {
                String[] attributes = annotation1.split("\n");
                for (String entry : attributes) {
                    String[] keyValue = entry.split("=", 2);
                    if (keyValue.length == 2) {
                        eleLabel.addAttribute(keyValue[0], keyValue[1]);
                    }
                }
            }
            eleLabel.addText(text);
            if (text.indexOf('\n') != -1) {    // preserve line breaks among the text
                eleLabel.addAttribute(QName.get("space", Namespace.XML_NAMESPACE), "preserve");
            }

            // proceed CMS specific case
            // if label ends with "$LABEL" or "$HELP", append context description as another label
            if (label.getDescription() != null && !label.getDescription().trim().isEmpty() &&
                    (label.getKey().endsWith("$LABEL") || label.getKey().endsWith("$HELP"))) {
                String contextKey;
                if (label.getKey().endsWith("$LABEL")) {
                    contextKey = label.getKey().substring(0, label.getKey().length() - 6) + "$CONTEXTLABEL";
                } else {
                    contextKey = label.getKey().substring(0, label.getKey().length() - 5) + "$CONTEXTHELP";
                }
                Element eleContext = eleLabels.addElement("entry");
                eleContext.addAttribute("key", contextKey);
                eleContext.addText(label.getDescription());
            }
        }

        // output
        String filename = getFileName(dict.getName(), dl, "xml");
        OutputFormat format = OutputFormat.createPrettyPrint();
        XMLPropWriter output = null;
        try {
            File targetFile = new File(targetDir, filename);
            if (!targetFile.getParentFile().exists()) {
                targetFile.getParentFile().mkdirs();
            }

            output = new XMLPropWriter(new FileWriter(targetFile), format);
            output.setSettings(settings);

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


}

/**
 * Customized XMLWriter, which forces to escape some special characters (apostrophe char)
 *
 * @author allany
 */
class XMLPropWriter extends XMLWriter {

    private GeneratorSettings settings;
    private List<Character> escapeCharacters = Arrays.asList('\'', '\u2032', '\u2018');

    public void setSettings(GeneratorSettings settings) {
        this.settings = settings;
    }

    public XMLPropWriter(FileWriter fileWriter, OutputFormat format) {
        super(fileWriter, format);
    }

    protected boolean shouldEncodeChar(char c) {
        return settings.isEscapeApostrophe() && escapeCharacters.contains(c) ? true : super.shouldEncodeChar(c);
    }
}
