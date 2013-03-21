package com.alcatel_lucent.dms.service.parser;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.DOMReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.BusinessWarning;
import com.alcatel_lucent.dms.Constants;
import com.alcatel_lucent.dms.model.Charset;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.DictionaryBase;
import com.alcatel_lucent.dms.model.DictionaryLanguage;
import com.alcatel_lucent.dms.model.Label;
import com.alcatel_lucent.dms.model.LabelTranslation;
import com.alcatel_lucent.dms.model.Language;
import com.alcatel_lucent.dms.service.LanguageService;
import com.alcatel_lucent.dms.util.Util;

@Component("MDCParser")
public class MDCParser extends DictionaryParser {

	@Autowired
	private LanguageService languageService;

	private Logger log = LoggerFactory.getLogger(MDCParser.class);
    
	@Override
	public ArrayList<Dictionary> parse(String rootDir, File file, Collection<File> acceptedFiles) throws BusinessException {
		BusinessException exceptions = new BusinessException(BusinessException.NESTED_ERROR);
		ArrayList<Dictionary> result = parse(rootDir, file, acceptedFiles, exceptions);
		if (exceptions.hasNestedException()) {
			throw exceptions;
		}
		return result;
	}
	
	public ArrayList<Dictionary> parse(String rootDir, File file, Collection<File> acceptedFiles, BusinessException exceptions) throws BusinessException {
		ArrayList<Dictionary> deliveredDicts = new ArrayList<Dictionary>();
		if (!file.exists())
            return deliveredDicts;
        if (file.isDirectory()) {
            File[] dctFileOrDirs = file.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.isDirectory() || Util.isMDCFile(pathname);
                }
            });
            for (File dctFile : dctFileOrDirs) {
            	deliveredDicts.addAll(parse(rootDir, dctFile, acceptedFiles, exceptions));
            }
            return deliveredDicts;
        } else if (!Util.isMDCFile(file)) {
        	return deliveredDicts;
        }
        
        String dictPath = file.getAbsolutePath().replace("\\", "/");
		String dictName = dictPath;
		if (rootDir != null && dictName.startsWith(rootDir)) {
			dictName = dictName.substring(rootDir.length() + 1);
		}
        FileInputStream in = null;
        try {
        	in = new FileInputStream(file);
        	try {
	            Dictionary dict = parseMDC(dictName, dictPath, in);
	    		deliveredDicts.add(dict);
        	} catch (BusinessException e) {
        		exceptions.addNestedException(e);
        	}
    		acceptedFiles.add(file);
            return deliveredDicts;
        } catch (IOException e) {
        	e.printStackTrace();
        	throw new BusinessException(e.toString());
        } finally {
        	if (in != null) {
        		try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
        	}
        }
	}

	public Dictionary parseMDC(String dictionaryName, String path,
			InputStream dctInputStream)
			throws BusinessException {
        log.info("Parsing Multilingual Dictionary configuration file '" + dictionaryName + "'");
        Collection<BusinessWarning> warnings = new ArrayList<BusinessWarning>();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		org.w3c.dom.Document doc;
		try {
			db = dbf.newDocumentBuilder();
			doc = db.parse(dctInputStream);
		} catch (Exception e1) {
			e1.printStackTrace();
			throw new BusinessException(BusinessException.INVALID_MDC_FILE, path);
		}

		DOMReader domReader = new DOMReader();
		Document document = domReader.read(doc);

        DictionaryBase dictBase=new DictionaryBase();
        dictBase.setName(dictionaryName);
        dictBase.setPath(path);
        dictBase.setEncoding("UTF-8");
        dictBase.setFormat(Constants.DictionaryFormat.MDC.toString());
        
		Dictionary dictionary = new Dictionary();
		dictionary.setBase(dictBase);

		BusinessException nonBreakExceptions = new BusinessException(
				BusinessException.NESTED_DCT_PARSE_ERROR, dictionary.getName());

		dictionary.setDictLanguages(readLanguages(document, dictionary,
				nonBreakExceptions));
		List nodes = document.selectNodes("/dictionary/messageString/*");
		Label label = null;
		Collection<Label> labels = new ArrayList<Label>();
		HashSet<String> labelKeys = new HashSet<String>();
		for (Object node : nodes) {
			try {
				label = readLabel((Element) node, dictionary, warnings);

			} catch (BusinessException e) {
				nonBreakExceptions.addNestedException(e);
			}
			if (labelKeys.contains(label.getKey())) {
				warnings.add(new BusinessWarning(
						BusinessWarning.DUPLICATE_LABEL_KEY, -1, label.getKey()));
			} else {
				labelKeys.add(label.getKey());
				labels.add(label);
			}
		}
		dictionary.setLabels(labels);
		if (nonBreakExceptions.hasNestedException()) {
			throw nonBreakExceptions;
		}
		dictionary.setParseWarnings(warnings);
		return dictionary;
	}

	@SuppressWarnings("unchecked")
	private Label readLabel(Element elem, Dictionary dictionary, Collection<BusinessWarning> warnings)
			throws BusinessException {
		Label label = new Label();
		label.setKey(elem.getName());
		label.setDictionary(dictionary);
		label.setDescription(null);
		label.setMaxLength(null);

		// en-GB is reference

		// Set<String> dictLangCodes = dictionary.getAllLanguageCodes();

		BusinessException exceptions = new BusinessException(
				BusinessException.NESTED_LABEL_ERROR, label.getKey());

		Map<String, String> entriesInLabel = new HashMap<String, String>();
		ArrayList<String> orderedLangCodes = new ArrayList<String>();

		List<Element> subElements = elem.elements();
		for (Element subElement : subElements) {
			Attribute attribute = (Attribute) subElement
					.selectSingleNode("@id");
			String langCode = attribute.getValue().trim();
			String translatedString = subElement.getStringValue().toString();
			log.debug(String.format("langCode=%s, translatedString=%s",
					langCode, translatedString));
			entriesInLabel.put(langCode, translatedString);
			orderedLangCodes.add(langCode);
		}
		String gae = entriesInLabel.get("en-GB");
		if (null == gae) {
			exceptions.addNestedException(new BusinessException(
					BusinessException.NO_REFERENCE_TEXT, label.getKey()));
		}
		label.setReference(gae);

		Collection<LabelTranslation> translations = new HashSet<LabelTranslation>();
		int labelSortNo = 1;
		for (String oLangCode : orderedLangCodes) {

			if (oLangCode.equals("en-GB")) {
				continue;
			}

			LabelTranslation trans = new LabelTranslation();
			trans.setLabel(label);
			DictionaryLanguage dl = dictionary.getDictLanguage(oLangCode);
			Language language = null == dl ? null : dl.getLanguage();

			trans.setLanguage(language);
			trans.setLanguageCode(oLangCode);
			trans.setOrigTranslation(entriesInLabel.get(oLangCode));
			trans.setSortNo(labelSortNo++);

			translations.add(trans);
		}
		label.setOrigTranslations(translations);

		label.setMaxLength(null);

		if (exceptions.hasNestedException()) {
			throw exceptions;
		}
		return label;
	}

	/**
	*
	* */
	private Collection<DictionaryLanguage> readLanguages(Document document,
			Dictionary dictionary, BusinessException exception) {
		HashSet<String> languageSet = new HashSet<String>();
		List nodes = document
				.selectNodes("/dictionary/messageString/*/lang/@id");
		for (Object node : nodes) {
			languageSet.add(((Attribute) node).getValue());
		}
		HashSet<DictionaryLanguage> dls = new HashSet<DictionaryLanguage>();
		DictionaryLanguage dl = null;
		int sortNo = 1;
		for (String langCode : languageSet) {
			dl = new DictionaryLanguage();
			dl.setLanguageCode(langCode);
			dl.setDictionary(dictionary);

			Language language = languageService.getLanguage(langCode);
			if (null == language) {
//				exception.addNestedException(new BusinessException(
//						BusinessException.UNKNOWN_LANG_CODE, -1, langCode));
			}
			dl.setLanguage(language);
			Charset charset = null;
			// DictionaryLanguage CharSet is the dictionary encoding
			charset = languageService.getCharsets().get("UTF-8");
			if (null == charset) {
//				exception.addNestedException(new BusinessException(
//						BusinessException.CHARSET_NOT_FOUND, dictionary
//								.getEncoding()));
			}
			dl.setCharset(charset);
			dl.setSortNo(sortNo++);
			dls.add(dl);
		}
		return dls;
	}

}
