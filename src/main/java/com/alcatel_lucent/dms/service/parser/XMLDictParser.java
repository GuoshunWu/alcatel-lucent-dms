package com.alcatel_lucent.dms.service.parser;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.Constants;
import com.alcatel_lucent.dms.SystemError;
import com.alcatel_lucent.dms.Constants.DictionaryFormat;
import com.alcatel_lucent.dms.model.*;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.service.LanguageService;
import com.alcatel_lucent.dms.service.TextService;
import com.alcatel_lucent.dms.util.Util;
import com.alcatel_lucent.dms.util.XDCPDTDEntityResolver;
import com.alcatel_lucent.dms.util.XDCTDTDEntityResolver;
import com.google.common.base.Strings;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.io.FilenameUtils;
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
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

@Component("XmlDictParser")
@SuppressWarnings("unchecked")
public class XMLDictParser extends DictionaryParser {

    private static final XDCPDTDEntityResolver xdcpdtdEntityResolver = new XDCPDTDEntityResolver();
    private static final XDCTDTDEntityResolver xdctdtdEntityResolver = new XDCTDTDEntityResolver();
    @Autowired
    private LanguageService languageService;

    @Autowired
    private TextService textService;

	@Override
	public DictionaryFormat getFormat() {
		return Constants.DictionaryFormat.XDCT;
	}
	
    @Override
    public ArrayList<Dictionary> parse(String rootDir, File file, Collection<File> acceptedFiles) throws BusinessException {
        ArrayList<Dictionary> deliveredDicts = new ArrayList<Dictionary>();
        if (!file.exists()) return deliveredDicts;

        Map<String, Collection<XDictionary>> propFiles = new HashMap<String, Collection<XDictionary>>();
        // add xdct files referred by an xdcp file
        HashSet<File> groupedFiles = new HashSet<File>();
        propFiles = groupDictionaries(file, propFiles, acceptedFiles, groupedFiles);
        
        // add other xdct files which is not referred by any xdcp file
        propFiles = addDictionariesWithoutXdcp(rootDir, file, propFiles, groupedFiles);

        /*
        * after grouped, each entry in the propFiles map is a dictionary
        * */

        long begin = System.currentTimeMillis();
        for (Map.Entry<String, Collection<XDictionary>> entry : propFiles.entrySet()) {
        	try {
        		deliveredDicts.add(processDictionary(entry, acceptedFiles));
        	} catch (Exception e) {
        		log.error(e.getMessage());
        		e.printStackTrace();
        	}
        }
        long end = System.currentTimeMillis();
        String timeStr = DurationFormatUtils.formatPeriod(begin, end, "mm 'minute(s)' ss 'second(s)'.");
        log.info(center("All the xdct files in " + file + " have been parsed, total used " + timeStr, 100, '*'));
        return deliveredDicts;
    }

    public Dictionary processDictionary(Map.Entry<String, Collection<XDictionary>> entry, Collection<File> acceptedFiles) {
        log.info(center("Parsing dictionary '" + entry.getKey() + "' in " + entry.getValue(), 50, '='));

        DictionaryBase dictBase = new DictionaryBase();
        dictBase.setName(entry.getKey());
        dictBase.setPath(null);
        dictBase.setEncoding("UTF-8");
        dictBase.setFormat(Constants.DictionaryFormat.XDCT.toString());

        Dictionary dictionary = new Dictionary();
        dictionary.setDictLanguages(new ArrayList<DictionaryLanguage>());
        dictionary.setLabels(new ArrayList<Label>());

        dictionary.setBase(dictBase);

        for (XDictionary xdct : entry.getValue()) {
            parseXdctFile(dictionary, xdct);
            acceptedFiles.add(xdct.getFile());
        }
        return dictionary;
    }

    public void parseXdctFile(Dictionary dict, XDictionary xdict) {
        File xdctFile = xdict.getFile();
        SAXReader saxReader = new SAXReader(true);
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
        for (XLanguage language : languages) processLanguage(language, dict, xdict);

        List<Element> keys = elemDict.elements("KEY");
        for (Element key : keys) processKey(key, dict);

    }

    private void processLanguage(XLanguage xLanguage, Dictionary dict, XDictionary xdict) {
        DictionaryLanguage dictLanguage = dict.getDictLanguage(xLanguage.getId());
        if (null != dictLanguage) {
            log.debug("Language code '" + dictLanguage.getLanguageCode() + "' already added in dictionary " + dict.getName() + ", ignore.");
            return;
        }
        dictLanguage = new DictionaryLanguage();

        dictLanguage.setDictionary(dict);

        dictLanguage.setLanguageCode(xLanguage.getId());
        // "zh" is regarded as "Simplified Chinese" for XmlDict
        dictLanguage.setLanguage(languageService.getLanguage(xLanguage.getId().equals("zh") ? "zh_CN" : xLanguage.getId()));
        dictLanguage.setCharset(languageService.getCharset(dict.getEncoding()));

        dictLanguage.setAnnotation1(String.format("is_reference=%s;is_context=%s", xLanguage.isIs_reference(), xLanguage.isIs_context()));
        dictLanguage.setAnnotation2(xdict.getPath());
        dictLanguage.setSortNo(-1);

        dict.addDictLanguage(dictLanguage);
        if (xLanguage.isIs_context()) {
        	dict.setReferenceLanguage(xLanguage.getId());
        }
    }

    private void processKey(Element key, Dictionary dict) {
        String name = key.attributeValue("name");
        Label label = dict.getLabel(name);
        if (null == label) {
            label = new Label();
            label.setReference("");		// set empty reference to avoid NPE
            label.setOrigTranslations(new ArrayList<LabelTranslation>());
            label.setKey(name);
            dict.addLabel(label);
        }

        label.setDictionary(dict);


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
            if (followUp != null) {    // set translation status
                boolean needTrans = !followUp.equals("no_translate");
                lt.setNeedTranslation(needTrans);
                if (needTrans) {
                    if("to_translate".equals(followUp)){
                        lt.setStatus(Translation.STATUS_UNTRANSLATED);
                    }else{
                        lt.setStatus(Translation.STATUS_TRANSLATED);
                    }
                }
            }
        }

        /**
         * Set label context to label context if label reference is empty and exist translation follow_up attribute is not "no_translate"
         * */
        if(Strings.isNullOrEmpty(label.getReference())){
//            String xpath = "TRANSLATION/@follow_up!='no_translate' or HELP/@follow_up!='no_translate'";
            String xpath = "TRANSLATION/@follow_up!='no_translate'";
            Boolean existTranslatedTranslation = (Boolean) key.selectObject(xpath);
            if(existTranslatedTranslation){
                Context labelCtx = new Context(textService.populateContextKey(Context.LABEL, label), Context.LABEL);
                label.setContext(labelCtx);
            }
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
            lt.setLanguage(label.getDictionary().getLanguageByCode(code));
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
     * @param groupedFiles	prevent duplicate read
     * @return map which contained grouped dct files, the key is their name, and the value is a collection of this
     *         dictionary related File objects.
     */
    public Map<String, Collection<XDictionary>> groupDictionaries(File file, Map<String, Collection<XDictionary>> propFiles, Collection<File> acceptedFiles, HashSet<File> groupedFiles) {
        if (file.isFile()) {
            if (!Util.isXdcpFile(file)) return propFiles;
            /* Read the xdcp file and get the dictionary and their relative path information from it.*/
            XMLProject xmlProject = readXdcp(file);
            acceptedFiles.add(file);

            for (XDictionary dict : xmlProject.getDictionaries()) {
                Collection<XDictionary> dictFiles = propFiles.get(dict.getName());
                if (null == dictFiles) {
                    dictFiles = new ArrayList<XDictionary>();
                    propFiles.put(dict.getName(), dictFiles);
                }
                File xdctFile = dict.getFile();
                if (!xdctFile.exists()) {
                    log.warn("The file '" + xdctFile + "' referenced by '" + file + "' does not exist, ignore.");
                    continue;
                }
                if (dictFiles.contains(dict) || groupedFiles.contains(dict.getFile())) {
                    log.info("The file '" + xdctFile + "' referenced by '" + file + "' already in collection, ignore.");
                    continue;
                }
                dictFiles.add(dict);
                groupedFiles.add(dict.getFile());
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
            propFiles.putAll(groupDictionaries(subFile, propFiles, acceptedFiles, groupedFiles));
        }
        return propFiles;
    }

    /**
     * Pick all single xdct files which is not referred in any xdcp file.
     * Each of such xdct file is regarded as a separate dictionary
     * @param file a xdct file or a directory containing xdct files
     * @param propFiles xdct files group (output)
     * @param groupedFiles files which already referred by an xdcp file, used to prevent duplicate read
     * @return xdct files group
     */
    public Map<String, Collection<XDictionary>> addDictionariesWithoutXdcp(String rootDir, File file, Map<String, Collection<XDictionary>> propFiles, HashSet<File> groupedFiles) {
    	if (file.isFile()) {
    		if (!groupedFiles.contains(file)) {
	    		String dictName = FilenameUtils.normalize(file.getAbsolutePath(), true);
	            if (rootDir != null && dictName.startsWith(rootDir)) {
	                dictName = dictName.substring(rootDir.length() + 1);
	            }
	            dictName = dictName.substring(0, dictName.length() - 5);	// remove ".xdct"
	    		XDictionary xdct = new XDictionary();
	    		xdct.setFile(file);
	    		xdct.setName(dictName);
	    		xdct.setPath(null);
	    		Collection<XDictionary> xdctFiles = new ArrayList<XDictionary>();
	    		xdctFiles.add(xdct);
	    		propFiles.put(dictName, xdctFiles);
    		}
    	} else if (file.isDirectory()) {
            File[] xDctFileOrDirs = file.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathName) {
                    return pathName.isDirectory() || Util.isXdctFile(pathName);
                }
            });

            for (File subFile : xDctFileOrDirs) {
                propFiles.putAll(addDictionariesWithoutXdcp(rootDir, subFile, propFiles, groupedFiles));
            }
    	}
    	return propFiles;
    }
    
    private XMLProject readXdcp(File xDcpFile) {
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
        XMLProject xmlProject = new XMLProject(xDcpFile);

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

        XLanguage() {
        }

        XLanguage(String id, boolean is_reference, boolean is_context) {
            this.id = id;
            this.is_reference = is_reference;
            this.is_context = is_context;
        }

        @Override
        public String toString() {
            return reflectionToString(this);
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
        private File file;

        XDictionary() {
        }

        XDictionary(String name, String path) {
            this.name = name;
            this.path = path;
        }

        @Override
        public String toString() {
            return reflectionToString(this);
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

        public File getFile() {
            return file;
        }

        public void setFile(File file) {
            this.file = file;
        }

        @Override
        public boolean equals(Object o) {
            if (file == null || o == null || ((XDictionary) o).getFile() == null) return false;
            return file.equals(((XDictionary) o).getFile());
        }
    }

    /**
     * Element 'PROJECT' is the root element of this XML Model
     * Attribute 'name' is the name of this project
     */
    class XMLProject {
        private File xdcpFile;
        private String name;
        private Collection<XLanguage> languages = new ArrayList<XLanguage>();
        private Collection<XDictionary> dictionaries = new ArrayList<XDictionary>();

        XMLProject(File xdcpFile) {
            this.xdcpFile = xdcpFile;
        }

        @Override
        public String toString() {
            return reflectionToString(this);
        }

        public void addDict(XDictionary dict) {
            String path = dict.getPath().replaceAll("\\\\", "/");
            try {
                File dctFile = new File(xdcpFile.getParent(), path).getCanonicalFile();
                dict.setFile(dctFile);
                this.dictionaries.add(dict);
            } catch (IOException e) {
                e.printStackTrace();
                throw new SystemError(e);
            }
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
