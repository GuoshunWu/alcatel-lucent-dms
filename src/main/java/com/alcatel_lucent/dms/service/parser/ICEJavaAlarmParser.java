package com.alcatel_lucent.dms.service.parser;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.Constants;
import com.alcatel_lucent.dms.Constants.DictionaryFormat;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.*;
import com.alcatel_lucent.dms.service.LanguageService;
import com.alcatel_lucent.dms.util.NoOpLSResourceResolver;
import com.alcatel_lucent.dms.util.Util;
import com.alcatel_lucent.dms.util.XDCTDTDEntityResolver;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.functors.InvokerTransformer;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.OrFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.tree.DefaultText;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.center;
import static org.apache.commons.lang3.StringUtils.join;

@Component("ICEJavaAlarmParser")
@SuppressWarnings("unchecked")
public class ICEJavaAlarmParser extends DictionaryParser {

    public static final String REFERENCE_LANG_CODE = "i-alu";
    public static final String ALARM_LABEL_GROUP_SEPARATOR = ".";
    private FileFilter xmlFilter = new OrFileFilter(new SuffixFileFilter(Arrays.asList(".xml")), DirectoryFileFilter.INSTANCE);
    private Validator iceJavaAlarmValidator;
    @Autowired
    private LanguageService languageService;

	@Override
	public DictionaryFormat getFormat() {
		return Constants.DictionaryFormat.ICE_JAVA_ALARM;
	}
	
    public ICEJavaAlarmParser() {

        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = null;
        try {
            schema = factory.newSchema(getClass().getResource("componentAlarmCatalog.xsd").toURI().toURL());
            iceJavaAlarmValidator = schema.newValidator();
            // avoid downloading external DTD
            iceJavaAlarmValidator.setResourceResolver(new NoOpLSResourceResolver());
        } catch (Exception e) {
            log.error("componentAlarmCatalog.xsd not found or incorrect!");
            e.printStackTrace();
        }

    }

    @Override
    public ArrayList<Dictionary> parse(String rootDir, File file, Collection<File> acceptedFiles) throws BusinessException {
        ArrayList<Dictionary> deliveredDicts = new ArrayList<Dictionary>();
        if (!file.exists()) return deliveredDicts;

        if (file.isDirectory()) {
            File[] dctFileOrDirs = file.listFiles(xmlFilter);
            for (File dctFile : dctFileOrDirs) {
                deliveredDicts.addAll(parse(rootDir, dctFile, acceptedFiles));
            }
            return deliveredDicts;
        }
        // Validate if it is a valid ICEJavaAlarm dictionary.
        FileInputStream is = null;
        try {
            is = new FileInputStream(file);
            Source source = new StreamSource(is);
            iceJavaAlarmValidator.validate(source);
        } catch (Exception ex) {
            log.info(file + " is not a valid ICE Java Alarm file because {}", ex.getMessage());
            return deliveredDicts;
        } finally {
        	if (is != null) {
        		try {is.close();} catch (Exception e) {}
        	}
        }

        String dictPath = FilenameUtils.normalize(file.getAbsolutePath(), true);
        rootDir = FilenameUtils.normalize(rootDir, true);
        String dictName = dictPath;
        if (rootDir != null && dictName.startsWith(rootDir)) {
            dictName = dictName.substring(rootDir.length() + 1);
        }

        deliveredDicts.add(parseICEJavaAlarm(dictName, dictPath, file));
        acceptedFiles.add(file);
        return deliveredDicts;
    }

    /**
     * Parse a single ICEJavaAlarm dictionary.
     *
     * @param dictName Dictionary name which is the relative path from given root dir
     * @param path     Normalized absolute path of the dictionary file.
     * @param file     The ICEJavaAlarm dictionary file.
     * @return A Dictionary which represent the ICEJavaAlarm dictionary.
     */
    private Dictionary parseICEJavaAlarm(String dictName, String path, File file) {
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
        dictBase.setFormat(Constants.DictionaryFormat.ICE_JAVA_ALARM.toString());

        Dictionary dictionary = new Dictionary();
        dictionary.setDictLanguages(new ArrayList<DictionaryLanguage>());
        dictionary.setLabels(new ArrayList<Label>());
        dictionary.setBase(dictBase);

        log.info(center("Parsing '" + file + "' for dictionary '" + dictBase.getName() + "'", 50, '='));

        Element catalog = document.getRootElement();
        String attributes = Util.map2String(Util.attributeList2Map(catalog.attributes()));

        dictionary.setAnnotation1(attributes);

        String defCtx = "defaultContextService";
        Element elementDefCtx = catalog.element(defCtx);
        dictionary.setAnnotation2(defCtx + "=" + elementDefCtx.getTextTrim());

        List<Element> alarms = catalog.elements("alarm");
        for (Element alarm : alarms) {
            parseAlarm(dictionary, alarm);
        }
        return dictionary;
    }

    /**
     * Parse a single alarm into a dictionary.
     */
    private void parseAlarm(Dictionary dict, Element alarm) {
        Map<String, String> attributes = new HashMap<String, String>();
        String alarmId = alarm.element("alarmId").getTextTrim();
        attributes.put("alarmId", alarmId);

        String constantName = alarm.element("constantName").getTextTrim();
        Label label = new Label();
        label.setDictionary(dict);
        label.setOrigTranslations(new ArrayList<LabelTranslation>());
        label.setKey(constantName);
        dict.addLabel(label);

        // process xs:choice element
        List<String> choiceElementNames = Arrays.asList(
                "communications", "processingError", "environmental", "qualityOfService",
                "equipment", "integrityViolation", "operationViolation", "physicalViolation",
                "securityServiceOrMechanismViolation", "timeDomainViolation"
        );

        Element choiceElement = null;
        for (String name : choiceElementNames) {
            choiceElement = alarm.element(name);
            if (null != choiceElement) break;
        }
        String eventType = choiceElement.element("eventType").getTextTrim();
        String probableCause = choiceElement.element("probableCause").getTextTrim();
        // store choiceElement in annotation1
        label.setAnnotation1(String.format("%s:eventType=%s;probableCause=%s", choiceElement.getName(), eventType, probableCause));

        String perceivedSeverity = alarm.element("perceivedSeverity").getTextTrim();

        attributes.put("perceivedSeverity", perceivedSeverity);

        //Now process those ones need to be translated
        parseTranslationElement(alarm.element("text"), label);

        Element elemAttributesDesc = alarm.element("attributesDesc");
        if (null != elemAttributesDesc) {
            Label attrDescLabel = new Label();
            attrDescLabel.setDictionary(dict);
            attrDescLabel.setOrigTranslations(new ArrayList<LabelTranslation>());
            attrDescLabel.setKey(constantName + ALARM_LABEL_GROUP_SEPARATOR + elemAttributesDesc.getName());
            dict.addLabel(attrDescLabel);
            parseTranslationElement(elemAttributesDesc, attrDescLabel);
        }
        Element elemProposedRepairAction = alarm.element("proposedRepairAction");
        if (null != elemProposedRepairAction) {
            Label proposeRepairLabel = new Label();
            proposeRepairLabel.setDictionary(dict);
            proposeRepairLabel.setOrigTranslations(new ArrayList<LabelTranslation>());
            proposeRepairLabel.setKey(constantName + ALARM_LABEL_GROUP_SEPARATOR + elemProposedRepairAction.getName());
            dict.addLabel(proposeRepairLabel);
            parseTranslationElement(elemProposedRepairAction, proposeRepairLabel);
        }
        List<DefaultText> texts = (List<DefaultText>) alarm.selectNodes("clearAlarms/alarmId/child::text()");
        if (!texts.isEmpty()) {
            String alarmIds = join(CollectionUtils.collect(texts, InvokerTransformer.getInstance("getStringValue")), ",");
            attributes.put("clearAlarms", alarmIds);
        }
        label.setAnnotation2(Util.map2String(attributes));
    }

    /**
     * Parse translation elements which are text, attributeDesc and proposeRepairAction
     */
    private void parseTranslationElement(Element element, Label label) {
        Dictionary dict = label.getDictionary();
        List<Element> elemLanguages = element.elements();
        for (Element elemLanguage : elemLanguages) {
            String langCode = elemLanguage.getName();
            String translation = elemLanguage.getTextTrim();
            log.info("Label {}, langCode: {} translation is {}.", new Object[]{label.getKey(), langCode, translation});

            DictionaryLanguage dictLanguage = dict.getDictLanguage(langCode);
            if (null == dictLanguage && !REFERENCE_LANG_CODE.equals(langCode)) {
                dictLanguage = new DictionaryLanguage();

                dictLanguage.setLanguageCode(langCode);
                dictLanguage.setLanguage(languageService.getLanguage(langCode));
                dictLanguage.setCharset(languageService.getCharset(dict.getEncoding()));
                dictLanguage.setSortNo(-1);
                label.getDictionary().addDictLanguage(dictLanguage);
            }

            if (REFERENCE_LANG_CODE.equals(langCode)) {
                label.setReference(translation);
            } else {
                LabelTranslation lt = new LabelTranslation();
                lt.setLabel(label);
                lt.setLanguageCode(langCode);
                lt.setSortNo(-1);
                lt.setLanguage(dict.getLanguageByCode(langCode));
                lt.setOrigTranslation(translation);
                label.addLabelTranslation(lt);
            }
        }
    }
}
