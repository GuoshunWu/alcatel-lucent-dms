package com.alcatel_lucent.dms.service.parser;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.Node;
import org.dom4j.ProcessingInstruction;
import org.dom4j.io.DOMReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.BusinessWarning;
import com.alcatel_lucent.dms.Constants;
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
	
	protected String getSecondNodeName() {
		return "LABEL";
	}
	
	protected String getFormat() {
		return Constants.DICT_FORMAT_XML_LABEL;
	}
	
	protected String getXPath() {
		return "/LABELS/LABEL";
	}
	
	protected String getKeyAttributeName() {
		return "label_id";
	}

	@Override
	public ArrayList<Dictionary> parse(String rootDir, File file, Collection<File> acceptedFiles) throws BusinessException {
		BusinessException exceptions = new BusinessException(BusinessException.NESTED_ERROR);
		ArrayList<Dictionary> result = parse(rootDir, file, acceptedFiles, exceptions);
		if (exceptions.hasNestedException()) {
			throw exceptions;
		} else {
			return result;
		}
	}
	
	public ArrayList<Dictionary> parse(String rootDir, File file, Collection<File> acceptedFiles, BusinessException exceptions) throws BusinessException {
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
            		deliveredDicts.addAll(parse(rootDir, subFile, acceptedFiles, exceptions));
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
            		deliveredDicts.add(parseLabelXML(rootDir, baseName, propFiles.get(baseName)));
            		acceptedFiles.addAll(propFiles.get(baseName));
            	} catch (BusinessException e) {
            		// Ignore INVALID_XML_FILE error because the file can be another type of xml dictionary.
            		if (e.getErrorCode() != BusinessException.INVALID_XML_FILE) {
            			exceptions.addNestedException(e);
            		}
            	}
            }
        }
		return deliveredDicts;
	}
	
	private Dictionary parseLabelXML(String rootDir,
			String baseName, Collection<File> files) {
		Collection<BusinessWarning> warnings = new ArrayList<BusinessWarning>();
		String refLangCode = null;
		File refFile = null;
		String dictName = null;
		for (File file : files) {
			String[] nameParts = splitFileName(file.getName());
			String langCode = nameParts[2];
			// reference file must end with "en.xml"
			if (langCode.equalsIgnoreCase("EN")) {
				refLangCode = langCode;
				refFile = file;
				dictName = refFile.getAbsolutePath().replace("\\", "/");
				if (rootDir != null && dictName.startsWith(rootDir)) {
					dictName = dictName.substring(rootDir.length() + 1);
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
        context.setName(Context.DEFAULT_CTX);
        
		Dictionary dictionary = new Dictionary();
		dictionary.setBase(dictBase);
		BusinessException dictExceptions = new BusinessException(BusinessException.NESTED_LABEL_XML_ERROR);
		BusinessException refFileExceptions = new BusinessException(BusinessException.NESTED_LABEL_XML_FILE_ERROR, refFile.getName());
		
		int sortNo = 1;
		Collection<DictionaryLanguage> dictLanguages = new ArrayList<DictionaryLanguage>();
		dictionary.setDictLanguages(dictLanguages);
		dictionary.setLabels(readLabels(refFile, dictionary, null, warnings, refFileExceptions));
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
			if (langCode.equalsIgnoreCase("EN")) {	// skip reference language
				continue;
			}
			DictionaryLanguage dictLanguage = new DictionaryLanguage();
			dictLanguage.setLanguageCode(langCode);
			dictLanguage.setSortNo(sortNo);
			Language language = languageService.getLanguage(langCode);
			if (language == null) {
//				throw new BusinessException(BusinessException.UNKNOWN_XML_LANG_CODE, langCode);
			}
			dictLanguage.setLanguage(language);
			dictLanguage.setCharset(languageService.getCharset("UTF-8"));
			dictLanguages.add(dictLanguage);
			if (!langCode.equals(refLangCode)) {
				Dictionary tempDict = new Dictionary();	// to get file-level annotations
				Collection<Label> labels = readLabels(file, tempDict, dictLanguage, warnings, fileExceptions);
				dictLanguage.setAnnotation1(tempDict.getAnnotation1());		// attributes of root element
				dictLanguage.setAnnotation2(tempDict.getAnnotation2());		// comment of the file
				dictLanguage.setAnnotation3(tempDict.getAnnotation3());		// namespaces of root element
				dictLanguage.setAnnotation4(tempDict.getAnnotation4());		// processing instructions
				for (Label label : labels) {
					Label refLabel = dictionary.getLabel(label.getKey());
					if (refLabel == null) {
						// TODO handle unexpected labels
					} else {
						LabelTranslation trans = new LabelTranslation();
						trans.setLanguageCode(langCode);
						trans.setLanguage(language);
						trans.setOrigTranslation(label.getReference());
						trans.setAnnotation1(label.getAnnotation1());
						trans.setAnnotation2(label.getAnnotation2());
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
		dictionary.setParseWarnings(warnings);
        return dictionary;
	}
	
	private Collection<Label> readLabels(File file, Dictionary dict, DictionaryLanguage dl, Collection<BusinessWarning> warnings, BusinessException exceptions) {
		Collection<Label> result = new ArrayList<Label>();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		org.w3c.dom.Document doc;
		Document document;
		Element root;
		try {
			db = dbf.newDocumentBuilder();
			doc = db.parse(file);
			DOMReader domReader = new DOMReader();
			document = domReader.read(doc);
		} catch (Exception e1) {
			throw new BusinessException(BusinessException.INVALID_XML_FILE, file.getName());
		}
			
		root = document.getRootElement();
		if (!root.getName().equals(getRootName())) {
			throw new BusinessException(BusinessException.INVALID_XML_FILE, file.getName());
		}
		// read root comments
		StringBuffer rootComments = new StringBuffer();
		Iterator<Node> nodeIter = document.nodeIterator();
		while (nodeIter.hasNext()) {
			Node node = nodeIter.next();
			if (node.getNodeType() == Node.COMMENT_NODE) {
				String text = node.getStringValue();
				if (!text.startsWith("# Generated by DMS")) {
					String escapedComment = node.getStringValue().replace("\\", "\\\\");
					escapedComment = escapedComment.replace("\n", "\\n");
					rootComments.append(escapedComment).append("\n");
				}
			}
		}
		if (rootComments.length() > 0) {
			dict.setAnnotation2(rootComments.substring(0, rootComments.length() - 1));
		}
		
		// read attributes of root element
		List<Attribute> rootAttributes = root.attributes();
		StringBuffer rootAttrStr = new StringBuffer();
		for (Attribute attr : rootAttributes) {
			rootAttrStr.append(attr.getName()).append("=").append(attr.getValue()).append("\n");
		}
		if (rootAttrStr.length() > 0) {
			dict.setAnnotation1(rootAttrStr.substring(0, rootAttrStr.length() - 1));
		}
		
		// read namespaces of root element
		List<Namespace> nsList = root.declaredNamespaces();
		StringBuffer nsStr = new StringBuffer();
		for (Namespace ns : nsList) {
			nsStr.append(ns.getPrefix()).append("=").append(ns.getURI()).append("\n");
		}
		if (nsStr.length() > 0) {
			dict.setAnnotation3(nsStr.substring(0, nsStr.length() - 1));
		}
		
		// read processing instructions
		List<ProcessingInstruction> piList = document.processingInstructions();
		StringBuffer piStr = new StringBuffer();
		for (ProcessingInstruction pi : piList) {
			piStr.append(pi.getTarget()).append("=").append(pi.getText());
		}
		if (piStr.length() > 0) {
			dict.setAnnotation4(piStr.substring(0, piStr.length() - 1));
		}
		
		List<Element> nodes = root.elements();
		int sortNo = 1;
		StringBuffer comments = new StringBuffer();
		Iterator<Node> iter = root.nodeIterator();
//			List<Element> nodes = document.selectNodes(getXPath());
//			for (Element node : nodes) {
		HashSet<String> keys = new HashSet<String>();
		while (iter.hasNext()) {
			Node node = iter.next();
			if (node.getNodeType() == Node.COMMENT_NODE) {
				comments.append(node.getStringValue()).append("\n");
				continue;
			}
			if (node.getNodeType() != Node.ELEMENT_NODE || !node.getName().equals(getSecondNodeName())) {
				continue;
			}
			if (!(node instanceof Element)) continue;
			List<Attribute> attributes = ((Element)node).attributes();
			String key = null;
			StringBuffer annotation = new StringBuffer();
			for (Attribute attr : attributes) {
				if (attr.getName().equals(getKeyAttributeName())) {
					key = attr.getValue();
				} else {
					annotation.append(attr.getName()).append("=").append(attr.getValue()).append("\n");
				}
			}
			if (keys.contains(key)) {
				if (dl == null) {	// add this type of warning only for label
					warnings.add(new BusinessWarning(BusinessWarning.DUPLICATE_LABEL_KEY, 0, key));
				}
				continue;
			} else {
				keys.add(key);
			}
			Label label = new Label();
			label.setKey(key);
			label.setReference(node.getStringValue().trim());
			if (annotation.length() > 0) {
				label.setAnnotation1(annotation.substring(0, annotation.length() - 1));
			}
			if (comments.length() > 0) {
				label.setAnnotation2(comments.substring(0, comments.length() - 1));
				comments = new StringBuffer();
			}
			label.setSortNo(sortNo++);
			result.add(label);
		}
		return result;
	}
}
