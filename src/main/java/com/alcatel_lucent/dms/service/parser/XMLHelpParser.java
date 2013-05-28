package com.alcatel_lucent.dms.service.parser;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.Constants;
import com.alcatel_lucent.dms.SystemError;
import com.alcatel_lucent.dms.Constants.DictionaryFormat;
import com.alcatel_lucent.dms.model.*;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.service.LanguageService;
import com.alcatel_lucent.dms.util.Util;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.*;

import static org.apache.commons.lang3.BooleanUtils.toBoolean;
import static org.apache.commons.lang3.StringUtils.center;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

@Component
@SuppressWarnings("unchecked")
public class XMLHelpParser extends DictionaryParser {

    private static final EntityResolver xHelpDTDEntityResolver = new EntityResolver() {
        @Override
        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            return new InputSource(getClass().getResourceAsStream("/dtds/XmlHelp.dtd"));
        }
    };

    public static final String[] extensions = new String[]{"xhlp"};
    public static final String DEFAULT_ENCODING = "UTF-8";
    public static final String KEY_SEPARATOR = ".";
    public static final String KEY_HELP_SIGN = "!HELP";
    private SuffixFileFilter xHelpFilter = new SuffixFileFilter(extensions, IOCase.INSENSITIVE);

    @Autowired
    private LanguageService languageService;

    @Override
    public DictionaryFormat getFormat() {
        return Constants.DictionaryFormat.XML_Help;
    }

    @Override
    public ArrayList<Dictionary> parse(String rootDir, File file, Collection<File> acceptedFiles) throws BusinessException {
        ArrayList<Dictionary> deliveredDicts = new ArrayList<Dictionary>();
        if (!file.exists()) return deliveredDicts;

        Document doc;
        if (file.isFile()) {
            if (FilenameUtils.isExtension(file.getName().toLowerCase(), extensions) && null != (doc = isValidXhlpFile(file))) {
                Pair<String, String> namePair = getDictNamePair(rootDir, file);
                deliveredDicts.add(parseXHelp(namePair.getLeft(), namePair.getRight(), doc));
                acceptedFiles.add(file);
            }
            return deliveredDicts;
        }
        //obtain all the xhlp file in source dir
        Collection<File> files = FileUtils.listFiles(file, xHelpFilter, TrueFileFilter.INSTANCE);

        for (File xHelpFile : files) {
            try {
                Document document = isValidXhlpFile(xHelpFile);
                Pair<String, String> namePair = getDictNamePair(rootDir, xHelpFile);
                deliveredDicts.add(parseXHelp(namePair.getLeft(), namePair.getRight(), document));
                acceptedFiles.add(xHelpFile);

            } catch (BusinessException e) {
                throw e;
            }
        }

        return deliveredDicts;
    }

    /**
     * Test if file is a valid xhlp file.
     */
    private Document isValidXhlpFile(File file) {
        SAXReader saxReader = new SAXReader(true);
        saxReader.setEntityResolver(xHelpDTDEntityResolver);
        Document document;
        try {
            document = saxReader.read(file);
        } catch (Exception e1) {
            log.error("Error parsing " + file.getName(), e1);
            document = null;
            throw new BusinessException(BusinessException.INVALID_XML_FILE, file.getName());
        }
        return document;
    }


    /**
     * Parse a single valid xml help dictionary.
     *
     * @param dictName Dictionary name which is the relative path from given root dir
     * @param path     Normalized absolute path of the dictionary file.
     * @param document The dictionary document.
     * @return A Dictionary which represent the xml help dictionary.
     */

    private Dictionary parseXHelp(String dictName, String path, Document document) {

        DictionaryBase dictBase = new DictionaryBase();
        dictBase.setPath(path);
        dictBase.setEncoding(document.getXMLEncoding());
        dictBase.setFormat(Constants.DictionaryFormat.XML_Help.toString());

        Dictionary dictionary = new Dictionary();
        dictionary.setDictLanguages(new ArrayList<DictionaryLanguage>());
        dictionary.setLabels(new ArrayList<Label>());
        dictionary.setBase(dictBase);

        log.info(center("Parsing '" + path + "' for dictionary '" + dictBase.getName() + "'", 50, '='));

        Element helpDoc = document.getRootElement();

        String innerDictName = helpDoc.attribute("name").getValue();
        dictionary.setAnnotation1("filename=" + dictName);
        dictBase.setName(innerDictName);

        List<Element> langElements = helpDoc.elements("LANGUAGE");

        int sortNo = 1;
        for (Element langElement : langElements) {
            String langCode = langElement.attribute("id").getValue();

            DictionaryLanguage dl = new DictionaryLanguage();
            dl.setLanguageCode(langCode);
            dl.setSortNo(sortNo++);

            dl.setLanguage(languageService.getLanguage(langCode));
            dl.setCharset(new Charset(document.getXMLEncoding()));

            dl.setDictionary(dictionary);
            dictionary.getDictLanguages().add(dl);
        }

        Element refLangElement = helpDoc.element("LANGUAGE_REF");
        String langCode = refLangElement.attribute("id").getValue();

        dictionary.setReferenceLanguage(langCode);
        DictionaryLanguage dl = new DictionaryLanguage();
        dl.setLanguageCode(langCode);
        dl.setSortNo(sortNo++);

        dl.setLanguage(languageService.getLanguage(langCode));
        dl.setCharset(new Charset(document.getXMLEncoding()));

        dl.setDictionary(dictionary);
        dictionary.getDictLanguages().add(dl);

        // parse items recursively
        List<Element> elemItems = helpDoc.selectNodes("descendant::ITEM");
        for (Element elemItem : elemItems) {
            Label label = new Label(getHierarchyKey(elemItem, KEY_SEPARATOR));

            label.setDictionary(dictionary);
            label.setOrigTranslations(new ArrayList<LabelTranslation>());
            dictionary.addLabel(label);

            // store element key attributes
            label.setAnnotation1(Util.map2String(Util.attributeList2Map(elemItem.attributes())));

            List<Element> elemTranslations = elemItem.elements("TRANSLATION");

            for (Element elemTranslation : elemTranslations) {
                parseTranslationElement(elemTranslation, label, langCode);
            }
        }

        return dictionary;
    }

    private LabelTranslation createTranslation(String langCode, Label label, String text) {
        LabelTranslation lt = new LabelTranslation();
        lt.setLabel(label);
        lt.setLanguageCode(langCode);
        lt.setSortNo(-1);
        lt.setLanguage(label.getDictionary().getLanguageByCode(langCode));
        lt.setOrigTranslation(text);

        label.addLabelTranslation(lt);
        return lt;
    }

    /**
     * Add translation into label
     */
    private void parseTranslationElement(Element elemTrans, Label label, String refCode) {
        Map<String, String> attrMap = Util.attributeList2Map(elemTrans.attributes());
        String langCode = attrMap.get("language");
        String help = elemTrans.element("HELP").getTextTrim();
        String followUp = attrMap.get("follow_up");
        if (StringUtils.isNotEmpty(help)) {
            Label helpLabel = null;
            if (langCode.equals(refCode)) {
                // create new label for this help text
                helpLabel = new Label(label.getKey() + KEY_HELP_SIGN);
                helpLabel.setDictionary(label.getDictionary());
                helpLabel.setOrigTranslations(new ArrayList<LabelTranslation>());
                label.getDictionary().addLabel(helpLabel);
                helpLabel.setReference(help);
            } else {
                helpLabel =  label.getDictionary().getLabel(label.getKey() + KEY_HELP_SIGN);
                LabelTranslation lt = createTranslation(langCode, helpLabel, help);
                setLabelTranslationStatus(lt, followUp);
            }
        }
//        attrMap.put("HELP", help);
        String text = elemTrans.element("LABEL").getTextTrim();
        if (langCode.equals(refCode)) {
            label.setReference(text);
            label.setAnnotation2(Util.map2String(attrMap));
            return;
        }
        LabelTranslation lt = createTranslation(langCode, label, text);
        lt.setAnnotation1(Util.map2String(attrMap));
        // set translation status
        setLabelTranslationStatus(lt, followUp);
    }

    private void setLabelTranslationStatus(LabelTranslation lt, String followUp){
        boolean needTrans = !followUp.equals("no_translate");
        lt.setNeedTranslation(needTrans);
        if (needTrans) {
            if("to_translate".equals(followUp) || "to_validate".equals(followUp)){
                lt.setStatus(Translation.STATUS_UNTRANSLATED);
            }else{
                lt.setStatus(Translation.STATUS_TRANSLATED);
            }
        }
    }

    /**
     * generate hierarchy key string according to the item name
     */
    private String getHierarchyKey(Element item, String separator) {
        String name = item.attribute("name").getValue();

        StringBuilder sb = new StringBuilder(name);
        Element parent;
        while (!(parent = item.getParent()).isRootElement()) {
            name = parent.attribute("name").getValue();
            sb.insert(0, name + separator);
            item = parent;
        }
        return sb.toString();
    }
}
