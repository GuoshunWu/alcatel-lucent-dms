package com.alcatel_lucent.dms.service.parser;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.BusinessWarning;
import com.alcatel_lucent.dms.model.Dictionary;

abstract public class DictionaryParser {
	
	protected Logger log = LoggerFactory.getLogger(this.getClass());
	
	/**
	 * Parse dictionaries into object.
	 * @param rootDir part of path to be trimmed in dictionary name
	 * @param file directory or file to be parsed, if file is a dictionary, files under the dictionary will be parsed recursively.
	 * @param acceptedFiles output parameter, holder of accepted files list
	 * @return list of Dictionary objects
	 * @throws BusinessException fatal errors, multiple exceptions can be put into a NESTED_ERROR exception.
	 * It's suggested to go through all files before throw a fatal error, so that user can get as more as possible
	 * information about errors.
	 */
	abstract public ArrayList<Dictionary> parse(String rootDir, File file, Collection<File> acceptedFiles) throws BusinessException;
	
	protected String[] splitFileName(String filename) {
		int dotPos = filename.lastIndexOf(".");
		if (dotPos != -1) {
			filename = filename.substring(0, dotPos);
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
				baseName = filename.substring(0, length - 3);
				sep = "_";
				langCode = filename.substring(length - 2);
			} else if (length >= 4 && filename.charAt(length - 4) == '.') {
				baseName = filename.substring(0, length - 4);
				sep = ".";
				langCode = filename.substring(length - 3);
			} else if (length >= 4 && filename.charAt(length - 4) == '_') {
				baseName = filename.substring(0, length - 4);
				sep = "_";
				langCode = filename.substring(length - 3);
			}
			if (baseName != null) {
				return new String[] {baseName, sep, langCode};
			}
		}
		return null;
	}


    /**
     * Obtain pair of dictionary name and dictionary path from root dir and file object.
     * @param rootDir  root directory.
     * @param file  dictionary file object.
     * @return pair object, left object is dictionary name and right object is dictionary path.
     * */
    protected Pair<String,String> getDictNamePair(String rootDir, File file){

        String dictPath = FilenameUtils.normalize(file.getAbsolutePath(), true);
        rootDir = FilenameUtils.normalize(rootDir, true);
        String dictName = dictPath;
        if (rootDir != null && dictName.startsWith(rootDir)) {
            dictName = dictName.substring(rootDir.length() + 1);
        }
        return Pair.of(dictName, dictPath);

    }
}
