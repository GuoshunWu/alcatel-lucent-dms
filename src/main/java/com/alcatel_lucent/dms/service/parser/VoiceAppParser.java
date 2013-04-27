package com.alcatel_lucent.dms.service.parser;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.Constants;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.*;
import com.alcatel_lucent.dms.service.LanguageService;
import com.alcatel_lucent.dms.util.NoOpLSResourceResolver;
import com.alcatel_lucent.dms.util.Util;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.center;

@Component
@SuppressWarnings("unchecked")
public class VoiceAppParser extends DictionaryParser {

    public static final String REFERENCE_LANG_CODE = "en_GB";
    public static final String[] extensions = new String[]{"xml"};
    public static final String DEFAULT_ENCODING = "UTF-8";
    private SuffixFileFilter xmlFilter = new SuffixFileFilter(extensions, IOCase.INSENSITIVE);
    private Validator iceJavaAlarmValidator;
    @Autowired
    private LanguageService languageService;

    public VoiceAppParser() {

        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = null;
        String schemaFile = "TUI.XSD";
        try {
            schema = factory.newSchema(getClass().getResource(schemaFile).toURI().toURL());
            iceJavaAlarmValidator = schema.newValidator();
            // avoid downloading external DTD
            iceJavaAlarmValidator.setResourceResolver(new NoOpLSResourceResolver());

        } catch (Exception e) {
            log.error(schemaFile + " not found or incorrect!");
            e.printStackTrace();
        }

    }

    @Override
    public ArrayList<Dictionary> parse(String rootDir, File file, Collection<File> acceptedFiles) throws BusinessException {
        ArrayList<Dictionary> deliveredDicts = new ArrayList<Dictionary>();
        if (null == file || !file.exists()) return deliveredDicts;

        if (file.isFile()) {
            if (FilenameUtils.isExtension(file.getName().toLowerCase(), extensions) && isVoiceAppFile(file)) {
            	log.info("Parsing VoiceApp XML " + file.getName());
                //parse single file
                String dictPath = FilenameUtils.normalize(file.getAbsolutePath(), true);
                rootDir = FilenameUtils.normalize(rootDir, true);
                String dictName = dictPath;
                if (rootDir != null && dictName.startsWith(rootDir)) {
                    dictName = dictName.substring(rootDir.length() + 1);
                }
                deliveredDicts.add(parseVoiceApp(dictName, dictPath, file));
                acceptedFiles.add(file);
            }
            return deliveredDicts;
        }

        // file is a directory
        Collection<File> files = FileUtils.listFiles(file, xmlFilter, TrueFileFilter.INSTANCE);

        for (File xmlFile : files) {
            try {
                if (!isVoiceAppFile(xmlFile)) continue;

                String dictPath = FilenameUtils.normalize(xmlFile.getAbsolutePath(), true);
                rootDir = FilenameUtils.normalize(rootDir, true);
                String dictName = dictPath;
                if (rootDir != null && dictName.startsWith(rootDir)) {
                    dictName = dictName.substring(rootDir.length() + 1);
                }
                deliveredDicts.add(parseVoiceApp(dictName, dictPath, xmlFile));
                acceptedFiles.add(xmlFile);

            } catch (BusinessException e) {
                throw e;
            }
        }
        return deliveredDicts;
    }

    private boolean isVoiceAppFile(File file) {
        // Validate if it is a valid ICEJavaAlarm dictionary.
    	FileInputStream is = null;
        try {
        	is = new FileInputStream(file);
        	Source source = new StreamSource(is);
            iceJavaAlarmValidator.validate(source);
        } catch (Exception ex) {
        	log.info("XML file '" + file.getName() + "' is not a VoiceApp XML: " + ex);
            return false;
        } finally {
        	if (is != null) {
        		try {is.close();} catch (Exception e) {}
        	}
        }
        return true;
    }

    /**
     * Parse a single VoiceApp dictionary.
     *
     * @param dictName Dictionary name which is the relative path from given root dir
     * @param path     Normalized absolute path of the dictionary file.
     * @param file     The dictionary file.
     * @return A Dictionary which represent the Voice app dictionary.
     */
    private Dictionary parseVoiceApp(String dictName, String path, File file) {
        SAXReader saxReader = new SAXReader();
        Document document = null;
        try {
            document = saxReader.read(file);
        } catch (Exception e) {
            log.error("Error parsing " + file.getName());
            throw new BusinessException(BusinessException.INVALID_XML_FILE, file.getName());
        }

        DictionaryBase dictBase = new DictionaryBase();
        dictBase.setName(dictName);
        dictBase.setPath(path);
        dictBase.setEncoding(document.getXMLEncoding());
        dictBase.setFormat(Constants.DictionaryFormat.VOICE_APP.toString());

        Dictionary dictionary = new Dictionary();
        dictionary.setDictLanguages(new ArrayList<DictionaryLanguage>());
        dictionary.setLabels(new ArrayList<Label>());
        dictionary.setBase(dictBase);

        log.info(center("Parsing '" + file + "' for dictionary '" + dictBase.getName() + "'", 50, '='));

        Element catalog = document.getRootElement();
        String attributes = Util.map2String(Util.attributeList2Map(catalog.attributes()));

        dictionary.setAnnotation1(attributes);

        String comment = "comment";
        Element elemComment = catalog.element(comment);
        if (null != elemComment) {
            dictionary.setAnnotation2(comment + "=" + elemComment.getTextTrim());
        }

        List<Element> alarms = catalog.elements("entry");
        for (Element alarm : alarms) {
            parseEntry(dictionary, alarm);
        }
        return dictionary;
    }

    /**
     * Parse a single alarm into a dictionary.
     */
    private void parseEntry(Dictionary dict, Element entry) {

        Element elemKey = entry.element("key");

        String key = elemKey.getTextTrim();

        Label label = new Label();
        label.setDictionary(dict);
        label.setOrigTranslations(new ArrayList<LabelTranslation>());
        label.setKey(key);
        dict.addLabel(label);

        // store element key attributes
        label.setAnnotation1(Util.map2String(Util.attributeList2Map(elemKey.attributes())));
        Attribute attrContext = elemKey.attribute("context");
        if (null != attrContext) {
            label.setContext(new Context(attrContext.getValue().trim()));
        }

        Element elemUsage = entry.element("usage");
        if (null != elemUsage) {
            String usage = elemUsage.getTextTrim();
            label.setAnnotation2("usage=" + usage);
        }

        List<Element> messages = entry.elements("message");
        for (Element element : messages) {
            parseTranslationElement(element, label);
        }
    }

    /**
     * Parse translation elements which are text, attributeDesc and proposeRepairAction
     */
    private void parseTranslationElement(Element message, Label label) {
        Dictionary dict = label.getDictionary();

        Element elemComment = message.element("comment");
        Element elemPhrase = message.element("phrase");
        Attribute doNotTrans = message.attribute("doNotTranslate");

        String langCode = message.attribute("lang").getValue();


        Map<String, String> attributes = new HashMap<String, String>();

        if (null != doNotTrans) {
            attributes.put("doNotTranslate", doNotTrans.getValue());
        }

        if (null != elemComment) {
            attributes.put("comment", elemComment.getTextTrim());
        }

        DictionaryLanguage dl = dict.getDictLanguage(langCode);
        if (null == dl) {
            dl = new DictionaryLanguage();
            dl.setLanguageCode(langCode);
            dl.setSortNo(-1);

            dl.setLanguage(languageService.getLanguage(langCode));
            dl.setCharset(new Charset(DEFAULT_ENCODING));

            dl.setDictionary(dict);
            dict.getDictLanguages().add(dl);
        }

        String text = null != elemPhrase ? elemPhrase.getTextTrim() : StringUtils.EMPTY;
        if (REFERENCE_LANG_CODE.equals(langCode)) {
            label.setReference(text);
            Map<String, String> origAttributes = Util.string2Map(label.getAnnotation2());
            origAttributes.putAll(attributes);
            label.setAnnotation2(Util.map2String(origAttributes));
        } else {
            //LabelTranslation
            LabelTranslation lt = new LabelTranslation();
            lt.setLabel(label);
            lt.setLanguageCode(langCode);
            lt.setSortNo(-1);
            lt.setLanguage(dict.getLanguageByCode(langCode));

            lt.setOrigTranslation(text);

            if (null != doNotTrans) {
                lt.setNeedTranslation(!Boolean.valueOf(doNotTrans.getValue()));
            }

            label.addLabelTranslation(lt);
            lt.setAnnotation1(Util.map2String(attributes));
        }
    }
}
