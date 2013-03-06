package com.alcatel_lucent.dms.service.parser;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.Constants;
import com.alcatel_lucent.dms.model.*;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.service.LanguageService;
import com.alcatel_lucent.dms.util.Util;
import com.alcatel_lucent.dms.util.XDCPDTDEntityResolver;
import com.alcatel_lucent.dms.util.XDCTDTDEntityResolver;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.*;

import static org.apache.commons.lang3.BooleanUtils.toBoolean;
import static org.apache.commons.lang3.StringUtils.center;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

@Component("XmlDictParser")
@SuppressWarnings("unchecked")
public class XMLDictParser extends DictionaryParser {

    @Autowired
    private LanguageService languageService;

    private static final XDCPDTDEntityResolver xdcpdtdEntityResolver = new XDCPDTDEntityResolver();
    private static final XDCTDTDEntityResolver xdctdtdEntityResolver = new XDCTDTDEntityResolver();

    @Override
    public ArrayList<Dictionary> parse(String rootDir, File file, Collection<File> acceptedFiles) throws BusinessException {
        ArrayList<Dictionary> deliveredDicts = new ArrayList<Dictionary>();
        if (!file.exists()) return deliveredDicts;

        Map<String, Collection<File>> propFiles = new HashMap<String, Collection<File>>();
        propFiles = groupDictionaries(file, propFiles, acceptedFiles);

        /*
        * after grouped, each entry in the propFiles map is a dictionary
        * */

        long begin = System.currentTimeMillis();
        for (Map.Entry<String, Collection<File>> entry : propFiles.entrySet()) {
            deliveredDicts.add(processDictionary(entry, acceptedFiles));
        }
        long end = System.currentTimeMillis();
        String timeStr = DurationFormatUtils.formatPeriod(begin, end, "mm 'minute(s)' ss 'second(s)'.");
        log.info(center("All the xdct files in " + file + " have been parsed, total used " + timeStr, 100, '*'));
        return deliveredDicts;
    }

    public Dictionary processDictionary(Map.Entry<String, Collection<File>> entry, Collection<File> acceptedFiles) {
        log.info(center("Parsing dictionary '" + entry.getKey() + "' in " + entry.getValue(), 50, '='));

        DictionaryBase dictBase = new DictionaryBase();
        dictBase.setName(entry.getKey());
        dictBase.setPath(null);
        dictBase.setEncoding("UTF-8");
        dictBase.setFormat(Constants.DICT_FORMAT_XDCT);

        Dictionary dictionary = new Dictionary();
        dictionary.setDictLanguages(new ArrayList<DictionaryLanguage>());
        dictionary.setLabels(new ArrayList<Label>());

        dictionary.setBase(dictBase);

        for (File xdct : entry.getValue()) {
            acceptedFiles.add(xdct);
            parseXdctFile(dictionary, xdct);
        }
        return dictionary;
    }

    public void parseXdctFile(Dictionary dict, File xdctFile) {
        SAXReader saxReader = new SAXReader();
        saxReader.setEntityResolver(xdctdtdEntityResolver);
        Document document;

        log.info(center("Parsing '" + xdctFile + "' for dictionary '" + dict.getName() + "'", 50, '='));
        try {
            document = saxReader.read(xdctFile);
        } catch (Exception e1) {
            log.error("Error parsing " + xdctFile.getName(), e1);
            throw new BusinessException(BusinessException.INVALID_XML_FILE, xdctFile.getName());
        }

        Element elemDict = document.getRootElement();

//      the name of this dictionary
        String name = elemDict.attributeValue("name");
        /*
        * Indicates use type of this dictionary
        * Value 'dictionary' : a simple dictionary
        * Value 'help' : generated help text from this dictionary
        *  */
//        String type = elemDict.attributeValue("type");
////      Indicates the application which uses this dictionary
//        String appli = elemDict.attributeValue("appli");
////      To cut help's key. This attribute is used if only type is 'help'
//        String sep = elemDict.attributeValue("separator");

        if (!name.equals(dict.getName())) {
            log.warn("The dictionary name " + name + " in " + xdctFile + " is not same with dictionary name' " + dict.getName() + "', use the one in dictionary.");
            dict.setName(name);
        }
//        save the related info in dict annotation1
//        dict.setAnnotation1(String.format("name=%s;type=%s;appli=%s;separator=%s", name, type, appli, sep));
        String attributes = Util.map2String(Util.attributeList2Map(elemDict.attributes()));
        dict.setAnnotation1(attributes);
        if (null == dict.getPath()) dict.setPath(xdctFile.getPath());
//        dict.setEncoding(document.getXMLEncoding());

        /*
       * @see XLanguage comments
       * */
        Collection<XLanguage> languages = readLanguagesFromElementList(elemDict.elements("LANGUAGE"));
        for (XLanguage language : languages) processLanguage(language, dict);

        List<Element> keys = elemDict.elements("KEY");
        for (Element key : keys) processKey(key, dict);

    }

    private void processLanguage(XLanguage xLanguage, Dictionary dict) {
        DictionaryLanguage dictLanguage = dict.getDictLanguage(xLanguage.getId());
        if (null != dictLanguage) {
            log.debug("Language code '" + dictLanguage.getLanguageCode() + "' already added in dictionary " + dict.getName() + ", ignore.");
            return;
        }
        dictLanguage = new DictionaryLanguage();

        dictLanguage.setDictionary(dict);

        dictLanguage.setLanguageCode(xLanguage.getId());
        dictLanguage.setLanguage(languageService.getLanguage(xLanguage.getId()));
        dictLanguage.setCharset(languageService.getCharset(dict.getEncoding()));

        dictLanguage.setAnnotation1(String.format("is_reference=%s;is_context=%s", xLanguage.isIs_reference(), xLanguage.isIs_context()));
        dictLanguage.setSortNo(-1);

        dict.addDictLanguage(dictLanguage);
    }

    private void processKey(Element key, Dictionary dict) {
        String name = key.attributeValue("name");
        Label label = dict.getLabel(name);
        if (null == label) {
            label = new Label();
            label.setOrigTranslations(new ArrayList<LabelTranslation>());
            label.setKey(name);
            dict.addLabel(label);
        }

        label.setDictionary(dict);
        Context ctxExclusion = new Context(Context.EXCLUSION);

        /**
         * Indicates global state of translations of this element, this attribute is used in merge process
         * value 'new' indicates that it's a new created KEY, it must translate in each languages
         * value 'modified' indicates the element KEY is modified, so it need translate one more time
         * value 'unmodified' indicates the element KEY is unmodified, so it need not translate one more time
         * */
        String state = key.attributeValue("state");
//      An information field about graphic object which is used this KEY, 'other' indicates no specific graphic zone
        String guiObject = key.attributeValue("gui_object", "other");
//      an Information field about category of this 'KEY'
        String msgCategory = key.attributeValue("message_category", "label");
//      controls maxi lines number of each translation, allowed values are number or "unlimited"
        String lines = key.attributeValue("lines", "1");
//      controls maxi columns number of each line, allowed values are number or "unlimited"
        String column = key.attributeValue("columns", "-1");

        /* put the above attributes to the label level annotation1 */
        label.putKeyValuePairToField("state", state, Label.ANNOTATION1);
        label.putKeyValuePairToField("gui_object", guiObject, Label.ANNOTATION1);
        label.putKeyValuePairToField("message_category", msgCategory, Label.ANNOTATION1);
        label.putKeyValuePairToField("lines", lines, Label.ANNOTATION1);
        label.putKeyValuePairToField("columns", column, Label.ANNOTATION1);

//        Label
        label.setSortNo(-1);

        if ("unlimited".equals(lines)) lines = "-1";
        if ("unlimited".equals(column)) column = "-1";
        label.setMaxLength(lines + '*' + column);

        /**
         * A comment from translator to programmer about translation
         * Attribute 'language' references an existing element LANGUAGE
         * */
        List<Element> elemComments = key.elements("COMMENT");

        for (Element elemComment : elemComments) {
            String langCode = elemComment.attributeValue("language");
            if ("gae".equalsIgnoreCase(langCode)) {
                label.putKeyValuePairToField("comment", elemComment.getStringValue(), Label.ANNOTATION1);
                continue;
            }
            LabelTranslation lt = getOrCreateNewLabelTranslation(langCode, label);
//            comment for language '$code'
            lt.putKeyValuePairToField("comment", elemComment.getStringValue(), LabelTranslation.ANNOTATION1);
        }

        /**
         * Contains some context descriptions about 'KEY'
         * Attribute 'language' references an existing element LANGUAGE
         * */
        List<Element> elemContexts = key.elements("CONTEXT");
        for (Element elementContext : elemContexts) {
            String langCode = elementContext.attributeValue("language");
            String contextText = elementContext.getStringValue();
            if ("gae".equalsIgnoreCase(langCode)) {
                label.putKeyValuePairToField("context", contextText, Label.ANNOTATION1);
                continue;
            }
            LabelTranslation lt = getOrCreateNewLabelTranslation(langCode, label);
            lt.putKeyValuePairToField("context", contextText, LabelTranslation.ANNOTATION1);
        }

        /**
         * A text help writted in specified language associated to 'KEY'. It's made up of string, image and table as translation
         * Attribute 'language' references an existing element LANGUAGE
         * Value 'no_translate' means don't need to translate
         * Value 'to_translate' means to translate
         * Value 'to_validate' means to validate
         * Value 'validated' means this translation is validated
         * */
        List<Element> elemHelps = key.elements("HELP");
        for (Element elemHelp : elemHelps) {
            String langCode = elemHelp.attributeValue("language").trim();
            if ("gae".equalsIgnoreCase(langCode)) {
                label.putKeyValuePairToField("help", elemHelp.getStringValue(), Label.ANNOTATION1);
                label.putKeyValuePairToField("follow_up", elemHelp.attributeValue("follow_up"), Label.ANNOTATION1);
                continue;
            }
            LabelTranslation lt = getOrCreateNewLabelTranslation(langCode, label);
            lt.putKeyValuePairToField("help", elemHelp.getStringValue(), LabelTranslation.ANNOTATION1);
        }
        /**
         * 'PARAM' is a generic element which is used to define a specific parameter for a application
         * Attribute 'name' defines name of this parameter
         * Attribute 'value' defines value of this parameter
         * Example : a parameter indiques the type of a KEY : name='type' value ='class' or 'attribute'
         * */
        List<Element> elemParams = key.elements("PARAM");
        for (Element elemParam : elemParams) {
            String pName = elemParam.attributeValue("name");
            String pValue = elemParam.attributeValue("value");
            label.addParam(pName, pValue);
        }

        /**
         * 'STATIC_TOKEN' is a string don't need to translate, so this strings must find again unchanged in each translation.
         * Example : Alcatel or OmniVista 4760
         * */

        List<Element> elemStaticTokens = key.elements("STATIC_TOKEN");
        if (!elemStaticTokens.isEmpty()) {
            String staticTokens =
                    StringUtils.join(
                            CollectionUtils.collect(elemStaticTokens, new Transformer() {
                                @Override
                                public Object transform(Object input) {
                                    return ((Element) input).getStringValue();
                                }
                            }).toArray(ArrayUtils.EMPTY_STRING_ARRAY), ';');
            label.putKeyValuePairToField("STATIC_TOKEN", staticTokens, Label.ANNOTATION2);
        }
        /**
         * A translation writted in specified language associated to 'KEY'. It's made up of string, image and table.
         * Attribute 'language' references an existing element LANGUAGE
         * Attribute 'follow_up' indicates state of this translation, this attribute is used in merge process
         * Value 'not_to_translate' means don't need to translate
         * Value 'to_translate' means to translate
         * Value 'to_validate' means to validate
         * Value 'validated' means this translation is validated
         * */
        List<Element> elemTranslations = key.elements("TRANSLATION");

        Boolean allExclusion = null;	// if all follow_up are "no translation", set the label to EXCLUSION context 
        for (Element elemTrans : elemTranslations) {
            String langCode = elemTrans.attributeValue("language").trim();
            if ("gae".equalsIgnoreCase(langCode)) {
                label.putKeyValuePairToField("follow_up", elemTrans.attributeValue("follow_up"), Label.ANNOTATION1);
                label.setReference(elemTrans.getStringValue());
                continue;
            }
            LabelTranslation lt = getOrCreateNewLabelTranslation(langCode, label);
            lt.setOrigTranslation(elemTrans.getStringValue());
            String followUp = elemTrans.attributeValue("follow_up");
            lt.putKeyValuePairToField("follow_up", followUp, BaseEntity.ANNOTATION1);
            if (followUp != null) {	// set translation status
            	if (followUp.equals("no_translate")) {
            		if (allExclusion == null) allExclusion = true;
            		lt.setStatus(Translation.STATUS_TRANSLATED);
            	} else if (followUp.equals("validated")) {
            		allExclusion = false;
            		lt.setStatus(Translation.STATUS_TRANSLATED);
            	} else {
            		allExclusion = false;
            		lt.setStatus(Translation.STATUS_UNTRANSLATED);
            	}
            }
        }
        if (allExclusion != null && allExclusion) {
        	label.setContext(ctxExclusion);
        }
    }

    private LabelTranslation getOrCreateNewLabelTranslation(String code, Label label) {
        LabelTranslation lt = label.getOrigTranslation(code);
        if (null == lt) {
            lt = new LabelTranslation();
            lt.setLabel(label);
            lt.setLanguageCode(code);
            lt.setSortNo(-1);
            lt.setOrigTranslation("");
            DictionaryLanguage dl = label.getDictionary().getDictLanguage(code);
            lt.setLanguage(null == dl ? null : dl.getLanguage());
            label.addLabelTranslation(lt);
        }
        return lt;
    }

    /**
     * Group all the xdct files by their name which described in xdcp files
     *
     * @param file          a xdcp file or a dir contained xdcp and xdct files.
     * @param propFiles     the initial map for xdct files group.
     * @param acceptedFiles
     * @return map which contained grouped dct files, the key is their name, and the value is a collection of this
     *         dictionary related File objects.
     */
    public Map<String, Collection<File>> groupDictionaries(File file, Map<String, Collection<File>> propFiles, Collection<File> acceptedFiles) {
        if (file.isFile()) {
            if (!Util.isXdcpFile(file)) return propFiles;
            /* Read the xdcp file and get the dictionary and their relative path information from it.*/
            XMLProject xmlProject = readXdcp(file);
            acceptedFiles.add(file);

            for (XDictionary dict : xmlProject.getDictionaries()) {
                Collection<File> dictFiles = propFiles.get(dict.getName());
                if (null == dictFiles) {
                    dictFiles = new ArrayList<File>();
                    propFiles.put(dict.getName(), dictFiles);
                }
                File xdctFile = null;
                try {
                	String xdctFilepath = dict.getPath().replaceAll("\\\\", "/");
                    xdctFile = new File(file.getParent(), xdctFilepath).getCanonicalFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (!xdctFile.exists()) {
                    log.warn("The file '" + xdctFile + "' referenced by '" + file + "' does not exist, ignore.");
                    continue;
                }
                if (dictFiles.contains(xdctFile)) {
                    log.info("The file '" + xdctFile + "' referenced by '" + file + "' already in collection, ignore.");
                    continue;
                }
                dictFiles.add(xdctFile);
            }
            return propFiles;
        }

        File[] xDcpFileOrDirs = file.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathName) {
                return pathName.isDirectory() || Util.isXdcpFile(pathName);
            }
        });

        for (File subFile : xDcpFileOrDirs) {
            propFiles.putAll(groupDictionaries(subFile, propFiles, acceptedFiles));
        }
        return propFiles;
    }

    private XMLProject readXdcp(File xDcpFile) {
        Collection<XDictionary> xDctFiles = new ArrayList<XDictionary>();
        SAXReader saxReader = new SAXReader();
        saxReader.setEntityResolver(xdcpdtdEntityResolver);
        Document document;
        try {
            document = saxReader.read(xDcpFile);
        } catch (Exception e1) {
            log.error("Error parsing " + xDcpFile.getName(), e1);
            throw new BusinessException(BusinessException.INVALID_XML_FILE, xDcpFile.getName());
        }

        Element project = document.getRootElement();
        XMLProject xmlProject = new XMLProject();

        xmlProject.setName(project.attributeValue("name"));

        List<Element> dictionaries = project.elements("DICTIONARY");
        for (Element dictionary : dictionaries) {
            xmlProject.addDict(new XDictionary(dictionary.attributeValue("name"), dictionary.attributeValue("path")));
        }
        xmlProject.setLanguages(readLanguagesFromElementList(project.elements("LANGUAGE")));
        return xmlProject;
    }

    private Collection<XLanguage> readLanguagesFromElementList(List<Element> elemLanguages) {
        return CollectionUtils.collect(elemLanguages, new Transformer() {
            @Override
            public Object transform(Object input) {
                Element elemLanguage = (Element) input;
                return new XLanguage(elemLanguage.attributeValue("id"),
                        toBoolean(elemLanguage.attributeValue("is_reference")),
                        toBoolean(elemLanguage.attributeValue("is_context")));
            }
        });
    }

    /**
     * Element 'LANGUAGE' defines one allowed language
     * Attribute 'id' is a locale code made up of language code ISO-639 and country code ISO-3166
     * Example 'id' : 'fr_CA' for Canada French or 'fr' for French without country information
     * Attribute 'is_reference' : true if this language is a reference for translator, false overwise
     * Attribute 'is_context' : true if this language is allowed to have a context description, false overwise
     */
    class XLanguage {
        private String id;
        private boolean is_reference;
        private boolean is_context;

        @Override
        public String toString() {
            return reflectionToString(this);
        }

        XLanguage() {
        }

        XLanguage(String id, boolean is_reference, boolean is_context) {
            this.id = id;
            this.is_reference = is_reference;
            this.is_context = is_context;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public boolean isIs_reference() {
            return is_reference;
        }

        public void setIs_reference(boolean is_reference) {
            this.is_reference = is_reference;
        }

        public boolean isIs_context() {
            return is_context;
        }

        public void setIs_context(boolean is_context) {
            this.is_context = is_context;
        }
    }

    /**
     * Element 'DICTIONARY' define a dictionary of this project
     * Attribute 'name' is the name of this dictionary
     * Attribute 'path' is the path of this dictionary file
     * path must be a relatif path if it's possible
     */
    class XDictionary {
        private String name;
        private String path;

        @Override
        public String toString() {
            return reflectionToString(this);
        }

        XDictionary() {
        }

        XDictionary(String name, String path) {
            this.name = name;
            this.path = path;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }

    /**
     * Element 'PROJECT' is the root element of this XML Model
     * Attribute 'name' is the name of this project
     */
    class XMLProject {
        private String name;
        private Collection<XLanguage> languages = new ArrayList<XLanguage>();
        private Collection<XDictionary> dictionaries = new ArrayList<XDictionary>();

        @Override
        public String toString() {
            return reflectionToString(this);
        }

        XMLProject() {
        }

        public void addDict(XDictionary dict) {
            this.dictionaries.add(dict);
        }

        public void addLanguage(XLanguage language) {
            this.languages.add(language);
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Collection<XLanguage> getLanguages() {
            return languages;
        }

        public void setLanguages(Collection<XLanguage> languages) {
            this.languages = languages;
        }

        public Collection<XDictionary> getDictionaries() {
            return dictionaries;
        }

        public void setDictionaries(Collection<XDictionary> dictionaries) {
            this.dictionaries = dictionaries;
        }
    }
}
