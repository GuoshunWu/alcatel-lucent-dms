package com.alcatel_lucent.dms.service.parser;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.BusinessWarning;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.DictionaryBase;
import com.alcatel_lucent.dms.model.DictionaryLanguage;
import com.alcatel_lucent.dms.model.Label;
import com.alcatel_lucent.dms.model.LabelTranslation;
import com.alcatel_lucent.dms.model.Language;
import com.alcatel_lucent.dms.service.LanguageService;
import com.alcatel_lucent.dms.util.Util;

//@Component("PropParser")
public class PropParser extends DictionaryParser {

	@Autowired
	private LanguageService languageService;
	
	@Override
	public ArrayList<Dictionary> parse(String rootDir, File file,
			Collection<BusinessWarning> warnings) throws BusinessException {
        ArrayList<Dictionary> deliveredDicts = new ArrayList<Dictionary>();
        if (!file.exists()) return deliveredDicts;
        if (file.isDirectory()) {
            File[] fileOrDirs = file.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.isDirectory() || Util.isPropFile(pathname)
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
            	deliveredDicts.add(parseProp(rootDir, baseName, propFiles.get(baseName), warnings));
            }
        }
		return null;
	}

	private Dictionary parseProp(String rootDir,
			String baseName, Collection<File> files,
			Collection<BusinessWarning> warnings) {
		String refLangCode = null;
		File refFile = null;
		for (File file : files) {
			String[] nameParts = splitFileName(file.getName());
			String langCode = nameParts[2];
			// reference file must end with "en.properties"
			if (langCode.equalsIgnoreCase("EN")) {
				refLangCode = langCode;
				refFile = file;
				break;
			}
		}
		if (refLangCode == null) {
			throw new BusinessException(BusinessException.NO_REFERENCE_LANGUAGE, baseName);
		}
        DictionaryBase dictBase=new DictionaryBase();
        dictBase.setName(refFile.getName());
        dictBase.setPath(refFile.getAbsolutePath());
        dictBase.setEncoding("UTF-8");
        dictBase.setFormat("prop");
        
		Dictionary dictionary = new Dictionary();
		dictionary.setBase(dictBase);
		BusinessException dictExceptions = new BusinessException(BusinessException.NESTED_PROP_ERROR);
		BusinessException refFileExceptions = new BusinessException(BusinessException.NESTED_PROP_FILE_ERROR, refFile.getName());
		int sortNo = 1;
		Collection<DictionaryLanguage> dictLanguages = new ArrayList<DictionaryLanguage>();
		dictionary.setDictLanguages(dictLanguages);
		dictionary.setLabels(readLabels(refFile, warnings, refFileExceptions));
		if (refFileExceptions.hasNestedException()) {
			dictExceptions.addNestedException(refFileExceptions);
		}
		for (File file : files) {
			String[] nameParts = splitFileName(file.getName());
			String langCode = nameParts[2];
			BusinessException fileExceptions = new BusinessException(BusinessException.NESTED_PROP_FILE_ERROR, file.getName());
			DictionaryLanguage dictLanguage = new DictionaryLanguage();
			dictLanguage.setLanguageCode(langCode);
			dictLanguage.setSortNo(sortNo);
			Language language = languageService.getLanguage(langCode);
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

	private Collection<Label> readLabels(File file,
			Collection<BusinessWarning> warnings, BusinessException exceptions) {
		
		return null;
	}

	private String[] splitFileName(String filename) {
		int dotPos = filename.lastIndexOf(".");
		if (dotPos != -1) {
			filename = filename.substring(0, dotPos);
			int pos = filename.lastIndexOf(".");
			if (pos != -1) {
				return new String[] {filename.substring(0, pos), ".", filename.substring(pos + 1)};
			}
			pos = filename.lastIndexOf("_");
			if (pos != -1) {
				return new String[] {filename.substring(0, pos), "_", filename.substring(pos + 1)};
			}
		}
		return null;
	}

}
