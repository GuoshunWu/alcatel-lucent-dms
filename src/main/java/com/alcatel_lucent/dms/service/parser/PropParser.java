package com.alcatel_lucent.dms.service.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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

@Component("PropParser")
public class PropParser extends DictionaryParser {

	@Autowired
	private LanguageService languageService;
	
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
		return deliveredDicts;
	}

	private Dictionary parseProp(String rootDir,
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
        log.info("Parsing java properties file '" + dictName + "'");
        DictionaryBase dictBase=new DictionaryBase();
        dictBase.setName(dictName);
        dictBase.setPath(refFile.getAbsolutePath());
        dictBase.setEncoding("ISO-8859-1");
        dictBase.setFormat("prop");
        
        Context context = new Context();
        context.setName(dictName);
        
		Dictionary dictionary = new Dictionary();
		dictionary.setBase(dictBase);
		BusinessException dictExceptions = new BusinessException(BusinessException.NESTED_PROP_ERROR);
		BusinessException refFileExceptions = new BusinessException(BusinessException.NESTED_PROP_FILE_ERROR, refFile.getName());
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
		ArrayList<Label> result = new ArrayList<Label>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "ISO-8859-1"));
			String line;
			StringBuffer comments = new StringBuffer();
			int sortNo = 1;
			int lineNo = 0;
			StringBuffer logicalLine = new StringBuffer();
			while ((line = br.readLine()) != null) {
				lineNo++;
				if (lineNo == 1) {	// remove BOM if any
	            	if (line.length() >= 2 && line.charAt(0) == 0xff && line.charAt(1) == 0xfe) {
	            		line = line.substring(2);
	            	} else if (line.length() >= 3 && line.charAt(0) == 0xef && line.charAt(1) == 0xbb && line.charAt(2) == 0xbf) {
	            		line = line.substring(3);
	            	} else if (line.length() >= 6 && (line.substring(0, 6).equalsIgnoreCase("\\ufeff") || 
	            			line.substring(0, 6).equalsIgnoreCase("\\ufffe"))) {
	            		line = line.substring(6);
	            	}
				}
				if (isCommentOrBlankLine(line)) {
					comments.append(line).append("\n");
				} else {
					logicalLine.append(line.trim());
					if (isLogicalLineEnd(line)) {
						String[] keyElement = parseKeyElement(logicalLine.toString());
						Label label = new Label();
						label.setAnnotation1(comments.toString());
						label.setKey(keyElement[0]);
						label.setReference(keyElement[1]);
						label.setSortNo(sortNo++);
						result.add(label);
						comments = new StringBuffer();
						logicalLine = new StringBuffer();
					} else {
						logicalLine.deleteCharAt(logicalLine.length() - 1);
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			exceptions.addNestedException(new BusinessException(e.toString()));
		} catch (IOException e) {
			e.printStackTrace();
			exceptions.addNestedException(new BusinessException(e.toString()));
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}

	/**
	 * Parse a key-element line.
	 * @param string
	 * @return result[0] is key and result[0] is element
	 */
	private String[] parseKeyElement(String string) {
		boolean afterEscape = false;
		int i;
		for (i = 0; i < string.length(); i++) {
			char c = string.charAt(i);
			if (!afterEscape && (c == '=' || c == ':')) {
				break;
			}
		}
		if (i < string.length()) {	//
			return new String[] {unescape(string.substring(0, i)), unescape(string.substring(i + 1))};
		} else {
			return new String[] {unescape(string), ""};
		}
	}

	/**
	 * Unescape a logical line of properties file. It performs the following:
	 *   Remove starting and ending space characters
	 *   Convert '\u0000' to a unicode character
	 *   Convert '\ ' to ' '
	 *   Convert '\=' to '='
	 *   Convert '\:' to ':'
	 *   Convert '\#' to '#'
	 *   Convert '\!' to '!'
	 *   Convert other escaped characters '\t', '\n', '\r'
	 *   Convert '\\' to '\'
	 *   Remove single backslash '\' that can't be recognized as a valid escape sequence
	 * @param line
	 * @return
	 */
	private String unescape(String line) {
		StringBuffer result = new StringBuffer();
		line = line.trim();
		for (int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);
			if (c == '\\') {
				if (i < line.length() - 1) {
					char next = line.charAt(i + 1);
					switch (next) {
					case ' ':
						result.append(' ');
						i++;
					case '=':
						result.append('=');
						i++;
					case ':':
						result.append(':');
						i++;
					case '#':
						result.append('#');
						i++;
					case '!':
						result.append('!');
						i++;
					case 't':
						result.append('\t');
						i++;
					case 'n':
						result.append('\n');
						i++;
					case 'r':
						result.append('\r');
						i++;
					case '\\':
						result.append('\\');
						i++;
					case 'u':
						if (i < line.length() - 5) {
							try {
								result.append((char) Integer.parseInt(line.substring(i + 2, i + 6), 16));
								i += 5;
							} catch (NumberFormatException e) {
								// do nothing
							}
						}
					} // switch
				}
			} else {	// normal character
				result.append(c);
			}
		}
		return result.toString();
	}

	/**
	 * A logical line may contain several natural lines ending with odd number of backslash.
	 * The method determines if the line is last natural line of a logical line.
	 * @param line
	 * @return true if logical line ends (not ending with backslash)
	 */
	private boolean isLogicalLineEnd(String line) {
		line = line.trim();
		int i = line.length() - 1;
		while (i >= 0 && line.charAt(i) == '\\') {
			i--;
		}
		return (line.length() - 1 - i) % 2 == 0;
	}

	/**
	 * Determine if the line is a comment line or blank line.
	 * A line is a comment line when the first non-space character is '#' or '!'.
	 * @param line
	 * @return
	 */
	private boolean isCommentOrBlankLine(String line) {
		line = line.trim();
		return line.isEmpty() || line.charAt(0) == '#' || line.charAt(0) == '!';
	}

	private String[] splitFileName(String filename) {
		int dotPos = filename.lastIndexOf(".");
		if (dotPos != -1) {
			filename = filename.substring(0, dotPos);
			int pos = filename.lastIndexOf(".");
			int length = filename.length();
			String baseName = null, sep = null, langCode = null;
			if (length >= 3 && filename.charAt(length - 3) == '.') {
				baseName = filename.substring(0, length - 3);
				sep = ".";
				langCode = filename.substring(length - 2);
			} else if (length >= 6 && filename.charAt(length - 6) == '.') {
				baseName = filename.substring(0, length - 6);
				sep = ".";
				langCode = filename.substring(length - 5);
			} else if (length >= 6 && filename.charAt(length - 6) == '_') {
				baseName = filename.substring(0, length - 6);
				sep = "_";
				langCode = filename.substring(length - 5);
			} else if (length >= 3 && filename.charAt(length - 3) == '_') {
				baseName =filename.substring(0, length - 3);
				sep = "_";
				langCode = filename.substring(length - 2);
			}
			if (baseName != null) {
				return new String[] {baseName, sep, langCode};
			}
		}
		return null;
	}

}