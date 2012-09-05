package com.alcatel_lucent.dms.service.parser;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.BusinessWarning;
import com.alcatel_lucent.dms.model.Dictionary;

abstract public class DictionaryParser {
	
	abstract public ArrayList<Dictionary> parse(String rootDir, File file, Collection<BusinessWarning> warnings) throws BusinessException;
	
}
