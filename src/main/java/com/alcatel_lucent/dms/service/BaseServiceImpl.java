package com.alcatel_lucent.dms.service;

public class BaseServiceImpl {
	protected DaoService dao;

	public void init() {
		// Load the base data
	}

	public void destroy() {

	}

	public void setDao(DaoService dao) {
		this.dao = dao;
	}

	public DaoService getDao() {
		return dao;
	}

}
