package com.alcatel_lucent.dms.service.parser;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.DOMReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.BusinessWarning;
import com.alcatel_lucent.dms.model.Context;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.DictionaryBase;
import com.alcatel_lucent.dms.model.DictionaryLanguage;
import com.alcatel_lucent.dms.model.Label;
import com.alcatel_lucent.dms.model.LabelTranslation;
import com.alcatel_lucent.dms.model.Language;
import com.alcatel_lucent.dms.service.LanguageService;
import com.alcatel_lucent.dms.util.Util;

@Component("labelXMLParser")
public class LabelXMLParser extends DictionaryParser {
	
	@Autowired
	private LanguageService languageService;
	
	protected String getRootName() {
		return "LABELS";
	}
	
	protected String getFormat() {
		return "XML labels";
	}
	
	protected String getXPath() {
		return "/LABELS/LABEL";
	}
	
	protected String getKeyAttributeName() {
		return "label_id";
	}

	@Override
	public ArrayList<Dictionary> parse(String rootDir, File file,
			Collection<BusinessWarning> warnings) throws BusinessException {
        ArrayList<Dictionary> deliveredDicts = new ArrayList<Dictionary>();
        if (!file.exists()) return deliveredDicts;
        rootDir = rootDir.replace("\\", "/");
        if (file.isDirectory()) {
            File[] fileOrDirs = file.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.isDirectory() || Util.isXmlFile(pathname)
                            || Util.isZipFile(pathname);
                }
            });
            Map<String, Collection<File>> propFiles = new HashMap<String, Collection<File>>();
            for (File subFile : fileOrDirs) {
            	if (subFile.isDirectory()) {
            		deliveredDicts.addAll(parse(rootDir, subFile, warnings));
            	} else {
            		String[] nameParts = splitFileName(subFile.getName());
            		if (nameParts != null) {
            			Collection<File> files = propFiles.get(nameParts[0]);
            			if (files == null) {
            				files = new ArrayList<File>();
            				propFiles.put(nameParts[0], files);
            			}
            			files.add(subFile);
            		}
            	}
            }
            for (String baseName : propFiles.keySet()) {
            	try {
            		deliveredDicts.add(parseLabelXML(rootDir, baseName, propFiles.get(baseName), warnings));
            	} catch (Exception e) {
            		log.warn(e);
            	}
            }
        }
		return deliveredDicts;
	}
	
	private Dictionary parseLabelXML(String rootDir,
			String baseName, Collection<File> files,
			Collection<BusinessWarning> warnings) {
		String refLangCode = null;
		File refFile = null;
		String dictName = null;
		for (File file : files) {
			String[] nameParts = splitFileName(file.getName());
			String langCode = nameParts[2];
			// reference file must end with "en.properties"
			if (langCode.equalsIgnoreCase("EN")) {
				refLangCode = langCode;
				refFile = file;
				dictName = refFile.getAbsolutePath().replace("\\", "/");
				if (rootDir != null && dictName.startsWith(rootDir)) {
					dictName = dictName.substring(rootDir.length());
				}
				break;
			}
		}
		if (refLangCode == null) {
			throw new BusinessException(BusinessException.NO_REFERENCE_LANGUAGE, baseName);
		}
        log.info("Parsing label xml file '" + dictName + "'");
        DictionaryBase dictBase=new DictionaryBase();
        dictBase.setName(dictName);
        dictBase.setPath(refFile.getAbsolutePath());
        dictBase.setEncoding("UTF-8");
        dictBase.setFormat(getFormat());
        
        Context context = new Context();
        context.setName(dictName);
        
		Dictionary dictionary = new Dictionary();
		dictionary.setBase(dictBase);
		BusinessException dictExceptions = new BusinessException(BusinessException.NESTED_LABEL_XML_ERROR);
		BusinessException refFileExceptions = new BusinessException(BusinessException.NESTED_LABEL_XML_FILE_ERROR, refFile.getName());
		
		int sortNo = 1;
		Collection<DictionaryLanguage> dictLanguages = new ArrayList<DictionaryLanguage>();
		dictionary.setDictLanguages(dictLanguages);
		dictionary.setLabels(readLabels(refFile, warnings, refFileExceptions));
		for (Label label : dictionary.getLabels()) {
			label.setContext(context);
		}
		if (refFileExceptions.hasNestedException()) {
			dictExceptions.addNestedException(refFileExceptions);
		}
		for (File file : files) {
			String[] nameParts = splitFileName(file.getName());
			String langCode = nameParts[2];
			BusinessException fileExceptions = new BusinessException(BusinessException.NESTED_LABEL_XML_FILE_ERROR, file.getName());
			DictionaryLanguage dictLanguage = new DictionaryLanguage();
			dictLanguage.setLanguageCode(langCode);
			dictLanguage.setSortNo(sortNo);
			Language language = languageService.getLanguage(langCode);
			if (language == null) {
				throw new BusinessException(BusinessException.UNKNOWN_XML_LANG_CODE, langCode);
			}
			dictLanguage.setLanguage(language);
			dictLanguages.add(dictLanguage);
			if (!langCode.equals(refLangCode)) {
				Collection<Label> labels = readLabels(file, warnings, fileExceptions);
				for (Label label : labels) {
					Label refLabel = dictionary.getLabel(label.getKey());
					if (refLabel == null) {
						// TODO handle unexpected labels
					} else {
						LabelTranslation trans = new LabelTranslation();
						trans.setLanguageCode(langCode);
						trans.setLanguage(language);
						trans.setOrigTranslation(label.getReference());
						trans.setSortNo(sortNo);
						refLabel.addOrigTranslation(trans);
					}						
				}
			}
			sortNo++;
			if (fileExceptions.hasNestedException()) {
				dictExceptions.addNestedException(fileExceptions);
			}
		}
		if (dictExceptions.hasNestedException()) {
			throw dictExceptions;
		}
        return dictionary;
	}
	
	private Collection<Label> readLabels(File file, Collection<BusinessWarning> warnings, BusinessException exceptions) {
		Collection<Label> result = new ArrayList<Label>();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		org.w3c.dom.Document doc;
		try {
			db = dbf.newDocumentBuilder();
			doc = db.parse(file);
			DOMReader domReader = new DOMReader();
			Document document = domReader.read(doc);
			Element root = document.getRootElement();
			if (!root.getName().equals(getRootName())) {
				throw new BusinessException(BusinessException.INVALID_XML_FILE, file.getName());
			}
			List<Element> nodes = document.selectNodes(getXPath());
			int sortNo = 1;
			for (Element node : nodes) {
				List<Attribute> attributes = node.attributes();
				String key = null;
				StringBuffer annotation = new StringBuffer();
				for (Attribute attr : attributes) {
					if (attr.getName().equals(getKeyAttributeName())) {
						key = attr.getValue();
					} else {
						annotation.append(attr.getName()).append("=").append(attr.getValue()).append(";");
					}
				}
				Label label = new Label();
				label.setKey(key);
				label.setReference(node.getStringValue().trim());
				if (annotation.length() > 0) {
					label.setAnnotation1(annotation.substring(0, annotation.length() - 1));
				}
				label.setSortNo(sortNo++);
				result.add(label);
			}
		} catch (Exception e1) {
			throw new BusinessException(BusinessException.INVALID_XML_FILE, file.getName());
		}
		return result;
	}
}
