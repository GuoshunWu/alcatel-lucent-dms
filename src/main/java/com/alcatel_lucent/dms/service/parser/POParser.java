package com.alcatel_lucent.dms.service.parser;

import static org.apache.commons.io.FilenameUtils.normalize;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.BusinessWarning;
import com.alcatel_lucent.dms.Constants;
import com.alcatel_lucent.dms.Constants.DictionaryFormat;
import com.alcatel_lucent.dms.model.Dictionary;
import com.alcatel_lucent.dms.model.DictionaryBase;
import com.alcatel_lucent.dms.model.DictionaryLanguage;
import com.alcatel_lucent.dms.model.Label;
import com.alcatel_lucent.dms.model.LabelTranslation;
import com.alcatel_lucent.dms.model.Language;
import com.alcatel_lucent.dms.model.Translation;
import com.alcatel_lucent.dms.service.LanguageService;
import com.alcatel_lucent.dms.util.Util;

@Component("POParser")
public class POParser extends DictionaryParser {
	
	@Autowired
    private LanguageService languageService;
	
	private static Pattern charsetPattern = Pattern.compile(".*Content-Type:.*;\\s*charset=([^\\\\]*)\\\\n.*");
	
	@Override
	public DictionaryFormat getFormat() {
		return Constants.DictionaryFormat.PO;
	}
	
	@Override
	public ArrayList<Dictionary> parse(String rootDir, File file,
			Collection<File> acceptedFiles) throws BusinessException {
        BusinessException exceptions = new BusinessException(BusinessException.NESTED_ERROR);
        ArrayList<Dictionary> result = parse(rootDir, file, acceptedFiles, exceptions);
        if (exceptions.hasNestedException()) {
            throw exceptions;
        } else {
            return result;
        }
	}
	
	/**
	 * Parse directories recursively
	 * @param rootDir
	 * @param file
	 * @param acceptedFiles
	 * @param exceptions
	 * @return
	 * @throws BusinessException
	 */
    public ArrayList<Dictionary> parse(String rootDir, File file, Collection<File> acceptedFiles, BusinessException exceptions) throws BusinessException {
        ArrayList<Dictionary> deliveredDicts = new ArrayList<Dictionary>();
        if (!file.exists()) return deliveredDicts;
        rootDir = normalize(rootDir, true);
        if (file.isDirectory()) {
        	// try to parse PO files under the folder
        	deliveredDicts.addAll(parsePOFiles(rootDir, file, acceptedFiles, exceptions));
            File[] fileOrDirs = file.listFiles();
            for (File subFile : fileOrDirs) {
            	deliveredDicts.addAll(parse(rootDir, subFile, acceptedFiles, exceptions));
            }
        }
        return deliveredDicts;
    }
    
    /**
     * Try to parse a folder as parent folder of PO files
     * @param rootDir
     * @param file
     * @param acceptedFiles
     * @param exceptions
     * @return
     * @throws BusinessException
     */
    private ArrayList<Dictionary> parsePOFiles(String rootDir, File file, Collection<File> acceptedFiles, BusinessException exceptions) throws BusinessException {
    	ArrayList<Dictionary> result = new ArrayList<Dictionary>();
    	File[] fileOrDirs = file.listFiles();
    	Map<String, Collection<File>> poMap = new HashMap<String, Collection<File>>();
    	// group po files by file name
    	for (File subFile : fileOrDirs) {
    		if (!subFile.isDirectory()) continue;
    		File[] poFiles = subFile.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return Util.isPOFile(pathname);
                }
            });
    		for (File po : poFiles) {
    			Collection<File> files = poMap.get(po.getName());
    			if (files == null) {
    				files = new ArrayList<File>();
    				poMap.put(po.getName(), files);
    			}
    			files.add(po);
    		}
    	}
    	
    	// parse po files for each filename
    	for (String filename : poMap.keySet()) {
    		Collection<File> poFiles = poMap.get(filename);
    		try {
	    		result.add(parsePODictionary(rootDir, file, poFiles, exceptions));
	    		acceptedFiles.addAll(poFiles);
    		} catch (BusinessException e) {
    			exceptions.addNestedException(e);
    		}
    	}
    	return result;
    }

    /**
     * Parse a set of po files with same name.
     * @param poFiles
     * @param exceptions
     * @return
     */
	private Dictionary parsePODictionary(String rootDir, File parentFolder, Collection<File> poFiles,
			BusinessException exceptions) {
        log.info("Parsing po files under '" + parentFolder.getAbsolutePath() + "'");
        Collection<BusinessWarning> warnings = new ArrayList<BusinessWarning>();
        File refFile = null;
        String refLangCode = "en";
        for (File file : poFiles) {
        	if (file.getParentFile().getName().equals(refLangCode)) {
        		refFile = file;
        		break;
        	}
        }
        if (refFile == null) {
        	throw new BusinessException(BusinessException.NO_REFERENCE_LANGUAGE, parentFolder.getAbsolutePath());
        }
        
        String dictName = normalize(parentFolder.getAbsolutePath(), true);
        dictName += "/*/" + refFile.getName();
        if (rootDir != null && dictName.startsWith(rootDir)) {
            dictName = dictName.substring(rootDir.length() + 1);
        }
        DictionaryBase dictBase = new DictionaryBase();
        dictBase.setName(dictName);
        dictBase.setPath(normalize(parentFolder.getAbsolutePath(), true));
        dictBase.setEncoding("ISO-8859-1");
        dictBase.setFormat(Constants.DictionaryFormat.PO.toString());
        
        Dictionary dictionary = new Dictionary();
        dictionary.setBase(dictBase);
        BusinessException dictExceptions = new BusinessException(BusinessException.NESTED_PROP_ERROR);
        BusinessException refFileExceptions = new BusinessException(BusinessException.NESTED_PROP_FILE_ERROR, refFile.getName());
        int sortNo = 1;
        Collection<DictionaryLanguage> dictLanguages = new ArrayList<DictionaryLanguage>();
        dictionary.setDictLanguages(dictLanguages);
        dictionary.setReferenceLanguage(refLangCode);
        DictionaryLanguage refDl = new DictionaryLanguage();
        refDl.setLanguageCode(refLangCode);
        // charset is decided by "Context-Type" meta in the file header
//        refDl.setCharset(languageService.getCharset("UTF-8"));
        refDl.setSortNo(sortNo++);
        Language language = languageService.getLanguage(refLangCode);
        refDl.setLanguage(language);
        dictLanguages.add(refDl);
        dictionary.setLabels(readLabels(refFile, dictionary, refDl, warnings, refFileExceptions));
        if (refFileExceptions.hasNestedException()) {
            dictExceptions.addNestedException(refFileExceptions);
        }
        for (File file : poFiles) {
        	String langCode = file.getParentFile().getName();
        	if (langCode.equals(refLangCode)) continue;
            BusinessException fileExceptions = new BusinessException(BusinessException.NESTED_PROP_FILE_ERROR, file.getName());
            DictionaryLanguage dictLanguage = new DictionaryLanguage();
            dictLanguage.setLanguageCode(langCode);
            dictLanguage.setSortNo(sortNo);
            language = languageService.getLanguage(langCode);
            dictLanguage.setLanguage(language);
            // charset is decided by "Context-Type" meta in the file header
//            dictLanguage.setCharset(languageService.getCharset("UTF-8"));
            dictLanguages.add(dictLanguage);
            Collection<Label> labels = readLabels(file, dictionary, dictLanguage, warnings, fileExceptions);
            for (Label label : labels) {
            	Label refLabel = null;
            	for (Label rl : dictionary.getLabels()) {	// find first label without LT by key in case duplicate key
            		if (rl.getKey().equals(label.getKey()) && rl.getOrigTranslation(langCode) == null) {
            			refLabel = rl;
            			break;
            		}
            	}
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
                    // for PO file, string is translated when msgid is not empty, even if it's same with the reference
                    if (label.getReference().trim().isEmpty()) {
                    	trans.setStatus(Translation.STATUS_UNTRANSLATED);
                    } else {
                    	trans.setStatus(Translation.STATUS_TRANSLATED);
                    }
                    refLabel.addOrigTranslation(trans);
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

	
	private Collection<Label> readLabels(File file, Dictionary dict, DictionaryLanguage dl,
            Collection<BusinessWarning> warnings, BusinessException exceptions) {
		ArrayList<Label> result = new ArrayList<Label>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "ISO-8859-1"));
			String line = null;
			Label label = null;
			StringBuffer comments = null;
			StringBuffer msgctxt = null;
			StringBuffer msgid = null;
			StringBuffer msgstr = null;
			StringBuffer msgidPlural = null;
			StringBuffer msgstrPlural = null;
			StringBuffer text = null;
			boolean firstLabel = true;
			while ((line = br.readLine()) != null) {
				if (line.startsWith("#~")) continue;	// ignore obselete lines
				if (line.trim().isEmpty()) {
					// end of entry
					if (label != null)	{
						if (firstLabel && msgid.toString().equals("")) {
							dl.setAnnotation1(msgstr.toString());
							Matcher m = charsetPattern.matcher(dl.getAnnotation1());
							if (m.matches() && m.groupCount() > 0) {
								String charset = m.group(1).trim().toUpperCase();
								dl.setCharset(languageService.getCharset(charset));
							}
							firstLabel = false;
						} else {
							addLabel(result, label, msgctxt, msgid, msgidPlural, msgstr, msgstrPlural, comments, exceptions);
						}
						label = null;
						msgctxt = null;
						msgid = null;
						msgstr = null;
						msgidPlural = null;
						msgstrPlural = null;
						text = null;
					}
				} else {
					if (label == null) {
						label = new Label();
						comments = new StringBuffer();
					}
					line = line.trim();
					if (line.startsWith("#")) {
						comments.append(line).append("\n");
						if (line.startsWith("#.")) {
							addDescription(label, getText(line.substring(2)));
						}
					} else if (line.startsWith("msgid_plural")) {
						text = msgidPlural = new StringBuffer();
						text.append(getText(line.substring(12)));
					} else if (line.startsWith("msgid")) {
						text = msgid = new StringBuffer();
						text.append(getText(line.substring(5)));
					} else if (line.startsWith("msgstr[1]")) {
						text = msgstrPlural = new StringBuffer();
						text.append(getText(line.substring(9)));
					} else if (line.startsWith("msgstr")) {
						text = msgstr = new StringBuffer();
						text.append(getText(line.substring(6)));
					} else if (line.startsWith("msgctxt")) {
						text = msgctxt = new StringBuffer();
						String ctxtStr = getText(line.substring(7));
						text.append(ctxtStr);
						addDescription(label, ctxtStr);
					} else {
						if (text != null) {
							text.append(getText(line));
						} else {
							exceptions.addNestedException(new BusinessException(BusinessException.INVALID_PO_SYNTAX, line));
						}
					}
				}
			}
			if (label != null) {
				if (firstLabel && msgid.equals("")) {
					dl.setAnnotation1(msgstr.toString());
					Matcher m = charsetPattern.matcher(dl.getAnnotation1());
					if (m.matches() && m.groupCount() > 0) {
						String charset = m.group(1).trim().toUpperCase();
						dl.setCharset(languageService.getCharset(charset));
					}
					firstLabel = false;
				} else {
					addLabel(result, label, msgctxt, msgid, msgidPlural, msgstr, msgstrPlural, comments, exceptions);
				}
			}
			if (dl.getCharset() == null) {	// set default charset UTF-8
				dl.setCharset(languageService.getCharset("UTF-8"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			exceptions.addNestedException(new BusinessException(e.toString()));
		} finally {
			if (br != null) try {br.close();} catch (Exception e) {}
		}
		return result;
	}

	private void addDescription(Label label, String text) {
		String desc = label.getDescription();
		desc = desc == null ? text : desc + "\n" + text;
		if (desc.length() >= 255) {
			desc = desc.substring(0, 255);
		}
		label.setDescription(desc);
	}

	private String getText(String str) {
		str = str.trim();
		if (str.startsWith("\"") && str.endsWith("\"")) {
			return str.substring(1, str.length() - 1);
		} else {
			return str;
		}
	}

	private void addLabel(ArrayList<Label> result, Label label, StringBuffer msgctxt,
			StringBuffer msgid, StringBuffer msgidPlural, StringBuffer msgstr,
			StringBuffer msgstrPlural, StringBuffer comments, BusinessException exceptions) {
		if (msgid == null) {
			exceptions.addNestedException(new BusinessException(BusinessException.INVALID_PO_SYNTAX, "No msgid"));
			return;
		}
		if (msgstr == null) {
			exceptions.addNestedException(new BusinessException(BusinessException.INVALID_PO_SYNTAX, "No msgstr"));
			return;
		}
		label.setKey(msgid.toString());
		label.setReference(msgstr.toString());
		label.setAnnotation1(comments == null ? null : comments.toString());
		label.setAnnotation2(msgctxt == null ? null : msgctxt.toString());
		result.add(label);
		if (msgidPlural != null) {
			if (msgstrPlural == null) {
				exceptions.addNestedException(new BusinessException(BusinessException.INVALID_PO_SYNTAX, "No msgstr[1]"));
				return;
			}
			Label label2 = new Label();
			label2.setDescription(label.getDescription());
			label2.setMaxLength(label.getMaxLength());
			label2.setKey(msgidPlural.toString());
			label2.setReference(msgstrPlural.toString());
			label2.setAnnotation1("plural");
//			label.setAnnotation2(msgctxt == null ? null : msgctxt.toString());
			result.add(label2);
		}
	}
}
