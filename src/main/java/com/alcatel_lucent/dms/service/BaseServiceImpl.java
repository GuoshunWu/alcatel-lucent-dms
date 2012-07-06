package com.alcatel_lucent.dms.service;

import java.util.List;

public class BaseServiceImpl {
	private DaoService dao;

	// All the alcatel language codes
	private List<String> allAlcatelLangCodes = null;

	@SuppressWarnings("unchecked")
	public List<String> getAllAlcatelLangCodes() {
		// get all alcatel language codes
		if (null == this.allAlcatelLangCodes) {
			this.allAlcatelLangCodes = getDao().retrieve(
					"select code from AlcatelLanguageCode");
			// an special check code
			allAlcatelLangCodes.add("CHK");
		}
		return allAlcatelLangCodes;
	}

	public void setDao(DaoService dao) {
		this.dao = dao;
	}

	public DaoService getDao() {
		return dao;
	}

}
