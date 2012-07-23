package com.alcatel_lucent.dms.service;

import org.springframework.beans.factory.annotation.Autowired;

public class BaseServiceImpl {

	@Autowired
	protected DaoService dao;
	
	public void setDao(DaoService dao) {
		this.dao = dao;
	}

	public DaoService getDao() {
		return dao;
	}

}
