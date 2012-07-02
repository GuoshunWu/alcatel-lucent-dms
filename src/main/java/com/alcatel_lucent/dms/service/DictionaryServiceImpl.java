package com.alcatel_lucent.dms.service;

import java.util.Map;

import com.alcatel_lucent.dms.BusinessException;
import com.alcatel_lucent.dms.model.Dictionary;

public class DictionaryServiceImpl extends BaseServiceImpl implements
		DictionaryService {

	public Dictionary deliverDCT(String filename, Long appId, String encoding,
			String[] langCodes, Map<String, String> langCharset)
			throws BusinessException {
		myTest();
		return null;
	}

	public void generateDCT(String filename, Long dctId, String encoding,
			String[] langCodes, Map<String, String> langCharset)
			throws BusinessException {
		// TODO Auto-generated method stub

	}

	private void myTest() {
		Dictionary dict = new Dictionary("test", "dct", "utf8", "c:/test",
				null, null, null, true);
		getDao().create(dict);
	}

}
