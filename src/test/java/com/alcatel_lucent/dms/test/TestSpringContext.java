package com.alcatel_lucent.dms.test;

import org.logicalcobwebs.proxool.ProxoolFacade;

import com.alcatel_lucent.dms.SpringContext;
import com.alcatel_lucent.dms.service.DaoService;



//import  org.hibernate.type.descriptor.sql.BasicBinder;


public class TestSpringContext {

	public static void main(String[] args) throws Exception {
		SpringContext.getService(DaoService.class);
		ProxoolFacade.shutdown(0);
	}
}
