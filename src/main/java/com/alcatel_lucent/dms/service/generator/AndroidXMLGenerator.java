package com.alcatel_lucent.dms.service.generator;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.Constants;
import com.alcatel_lucent.dms.SystemError;
import com.alcatel_lucent.dms.model.*;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.service.DaoService;
import com.alcatel_lucent.dms.service.parser.AndroidXMLParser;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.PredicateUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.dom4j.tree.DefaultComment;
import org.dom4j.tree.DefaultText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileWriter;
import java.io.UnsupportedEncodingException;
import java.util.*;

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
        if (org.springframework.util.CollectionUtils.isEmpty(dict.getDictLanguages())) return;
        for (DictionaryLanguage dl : dict.getDictLanguages()) {
            generateAndroidXML(target, dict, dl);
        }
    }

    @Override
    public Constants.DictionaryFormat getFormat() {
        return Constants.DictionaryFormat.XML_ANDROID;
    }

    private String getArrayNameByLabel(Label label) {
        String labelKey = label.getKey();
        return labelKey.substring(0, labelKey.lastIndexOf(AndroidXMLParser.KEY_SEPARATOR));
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
        // if dl is reference, set it to null
        if (dl != null && dl.getLanguageCode().equals(refLangCode)) dl = null;
        String dictAttributes = (dl == null ? dict.getAnnotation1() : dl.getAnnotation1());
        String dictComments = (dl == null ? dict.getAnnotation2() : dl.getAnnotation2());
        String dictNamespaces = (dl == null ? dict.getAnnotation3() : dl.getAnnotation3());
        String processingInstructions = (dl == null ? dict.getAnnotation4() : dl.getAnnotation4());

        Document doc = DocumentHelper.createDocument();
        doc.addComment("\n# " + getDMSGenSign() + " using language " + (dl == null ? refLangCode : dl.getLanguageCode()) + ".\n# Labels: " + dict.getLabelNum() + "\n");
        addCommentsForElement(dictComments, doc);
        Element eleLabels = doc.addElement("resources");
        addNamespaceForElement(dictNamespaces, eleLabels);

        addAttributesForElement(dictAttributes, eleLabels);
        String lastComment = null;
        Set<String> outputtedArrayLabels = new HashSet<String>();
        for (Label label : dict.getAvailableLabels()) {
            if (outputtedArrayLabels.contains(label.getKey())) continue;
            //keep last comment above the label which is not translated
            String realComment = isArrayOrPluralLabel(label) ? getArrayOrPluralsComment(getArrayNameByLabel(label), dict) : label.getAnnotation2();
            lastComment = StringUtils.defaultIfBlank(realComment, lastComment);
            //if last comment has been write done, then make it empty
            lastComment = writeLabel(eleLabels, label, dl, lastComment, outputtedArrayLabels) ? "" : lastComment;
        }
        // output
        String filename = getFileName(dict.getName(), dl);
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setEncoding("UTF-8");
        format.setNewlines(true);
        XMLWriter output = null;

        try {
            File targetFile = new File(targetDir, filename);
            FileUtils.touch(targetFile);
            output = new XMLWriter(new FileWriter(targetFile), format);
            writeProcessingInstructions(processingInstructions, output);
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

    private void writeProcessingInstructions(String processingInstructions, XMLWriter output) throws SAXException {
        if (StringUtils.isEmpty(processingInstructions)) return;
        String[] piList = processingInstructions.split("\n");
        for (String pi : piList) {
            String[] keyValue = pi.split("=", 2);
            if (keyValue.length == 2) {
                output.processingInstruction(keyValue[0], keyValue[1]);
            }
        }

    }

    private void addNamespaceForElement(String strNameSpaces, Element element) {
        if (StringUtils.isEmpty(strNameSpaces)) return;
        String[] nsList = strNameSpaces.split("\n");
        for (String ns : nsList) {
            String[] keyValue = ns.split("=", 2);
            if (keyValue.length == 2) {
                element.addNamespace(keyValue[0], keyValue[1]);
            }
        }
    }


    private boolean isArrayOrPluralLabel(Label label) {
        String lblKey = label.getKey();
        String[] tokens = lblKey.split(AndroidXMLParser.KEY_SEPARATOR);
        return 3 == tokens.length && (lblKey.startsWith(AndroidXMLParser.ELEMENT_STRING_ARRAY) || lblKey.startsWith(AndroidXMLParser.ELEMENT_PLURALS));
    }

    /**
     * @param name array or plural name which like string-array_typeOfEmbedTags
     *             labelKey.substring(0, labelKey.lastIndexOf(AndroidXMLParser.KEY_SEPARATOR))
     */
    private List<Label> getLabelsInArrayOrPlural(final String name, final Collection<Label> allLabels) {
        List<Label> arrayLabels = new ArrayList(allLabels);
        CollectionUtils.filter(arrayLabels, new Predicate() {
            @Override
            public boolean evaluate(Object object) {
                Label dictLabel = (Label) object;
                if (!isArrayOrPluralLabel(dictLabel)) return false;
                String key = dictLabel.getKey();
                String pattern = "^" + name + "_\\d+$";
                return key.matches(pattern);
            }
        });
        return arrayLabels;
    }

    /**
     * return true, only if the array or plurals is translated(at least one item in array or plurals
     * is translated will be considered translated)
     */
    private boolean isArrayOrPluralTranslated(String name, Collection<Label> allLabels, DictionaryLanguage dl) {
        return CollectionUtils.exists(getLabelsInArrayOrPlural(name, allLabels),
                PredicateUtils.invokerPredicate("isTranslated", new Class[]{DictionaryLanguage.class}, new Object[]{dl}));
    }

    /**
     * Get the comment above the array from its first item annotation
     */
    private String getArrayOrPluralsComment(String name, Dictionary dict) {
        Label fistLabel = dict.getLabel(name + AndroidXMLParser.KEY_SEPARATOR + "0");
        String[] tokens = fistLabel.getAnnotation2().split(";");
        return decodeBase64(tokens[0]);
    }

    private String decodeBase64(String source) {
        String charset = "iso8859-1";
        String result = null;
        try {
            result = new String(Base64.decodeBase64(source.getBytes(charset)), charset);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * Write a label element from an label for a specified language
     *
     * @param eleLabels            element label to write
     * @param label
     * @param outputtedArrayLabels
     * @return whether the label is write
     */
    private boolean writeLabel(Element eleLabels, Label label, DictionaryLanguage dl, String lastComment, Set<String> outputtedArrayLabels) {
        String lblKey = label.getKey();
        String[] tokens = lblKey.split(AndroidXMLParser.KEY_SEPARATOR);


        String text = label.getReference();
        String annotation1 = label.getAnnotation1();    // attributes
//        String annotation2 = label.getAnnotation2();    // leading comments
        // keep the last comment
        boolean isArrayOrPlurals = isArrayOrPluralLabel(label);

        if (dl != null) {//language file
            LabelTranslation lt = label.getOrigTranslation(dl.getLanguageCode());
            if (lt != null) {
                annotation1 = lt.getAnnotation1();
                // DMS will take the comment in reference file instead(language file comment is discarded)
//                annotation2 = lt.getAnnotation2();
            }
            text = label.getTranslation(dl.getLanguageCode());
        }

        if (!isArrayOrPlurals) { // normal label
            // create label
            if (null != dl && !label.isTranslated(dl)) return false;
            addCommentsForElement(lastComment, eleLabels);
            Element eleLabel = eleLabels.addElement("string");
            eleLabel.addAttribute("name", label.getKey());
            addAttributesForElement(annotation1, eleLabel);
            eleLabel.addText(text);
            if (text.indexOf('\n') != -1) {    // preserve line breaks among the text
                eleLabel.addAttribute(QName.get("space", Namespace.XML_NAMESPACE), "preserve");
            }
            return true;
        }


        // string array or quantity string
        String elemName = tokens[0];
        String key = tokens[1];

        //get array element items
        String arrayName = elemName + AndroidXMLParser.KEY_SEPARATOR + key;

        Collection<Label> dictLabels = label.getDictionary().getAvailableLabels();
        Collection<Label> arrayLabels = getLabelsInArrayOrPlural(arrayName, dictLabels);

        if (dl != null && !isArrayOrPluralTranslated(arrayName, dictLabels, dl)) return true;

        addCommentsForElement(lastComment, eleLabels);
        Element elemLabel = eleLabels.addElement(elemName);
        elemLabel.addAttribute(AndroidXMLParser.getKeyAttributeName(), key);

        //if array is not translated continue
        // add each item in array or quantity string
        boolean foundComment = false;
        int index = arrayName.length() + 1;
        for (Label arrayLabel : arrayLabels) {
            annotation1 = arrayLabel.getAnnotation1();
            lastComment = arrayLabel.getAnnotation2();
            if (!foundComment && !StringUtils.isBlank(lastComment) &&
                    (foundComment = "0".equals(arrayLabel.getKey().substring(index)))) { //first label comment is special
                lastComment = decodeBase64(lastComment.split(";")[1]);
            }
            addCommentsForElement(lastComment, elemLabel);

            Element elemItem = elemLabel.addElement("item");
            text = arrayLabel.getReference();

            if (dl != null) {
                LabelTranslation lt = arrayLabel.getOrigTranslation(dl.getLanguageCode());
                if (lt != null) {
                    annotation1 = lt.getAnnotation1();
                    // DMS will take the comment in reference file instead(language file comment is discarded)
//                annotation2 = lt.getAnnotation2();
                }
                text = arrayLabel.getTranslation(dl.getLanguageCode());
            }
            addAttributesForElement(annotation1, elemItem);
            elemItem.addText(text);
            outputtedArrayLabels.add(arrayLabel.getKey());
        }
        return true;
    }

    private void addCommentsForElement(String strComments, Branch parentBranch) {
        if (StringUtils.isEmpty(strComments)) return;
        String[] comments = strComments.split("\n");

        for (String comment : comments) {
            comment = StringEscapeUtils.unescapeJava(comment);
            if (StringUtils.isBlank(comment)) {
                parentBranch.add(new DefaultText("\n"));
            } else {
                parentBranch.add(new DefaultComment(comment));
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
