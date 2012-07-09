/**
 * 
 */
package com.alcatel_lucent.dms.test;

import org.logicalcobwebs.proxool.ProxoolFacade;

import com.alcatel_lucent.dms.model.HibernateSessionFactory;

/**
 * @author Guoshun.Wu
 * Date: 2012-06-28
 *
 */
public class HibernateTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		HibernateSessionFactory.getConfiguration();
		ProxoolFacade.shutdown(0);
	}
}
