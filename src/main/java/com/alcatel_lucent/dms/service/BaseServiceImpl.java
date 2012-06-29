package com.alcatel_lucent.dms.service;

public class BaseServiceImpl {
	private DaoService dao;

	public void setDao(DaoService dao) {
		this.dao = dao;
	}

	public DaoService getDao() {
		return dao;
	}
}
