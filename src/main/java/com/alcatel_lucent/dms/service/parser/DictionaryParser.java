package com.alcatel_lucent.dms.service.parser;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.BusinessWarning;
import com.alcatel_lucent.dms.model.Dictionary;

abstract public class DictionaryParser {
	
	protected Logger log = Logger.getLogger(this.getClass());
	abstract public ArrayList<Dictionary> parse(String rootDir, File file, Collection<BusinessWarning> warnings) throws BusinessException;
	
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
