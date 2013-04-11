package com.alcatel_lucent.dms.util;

import org.hibernate.HibernateException;
import org.hibernate.connection.ProxoolConnectionProvider;
import org.logicalcobwebs.proxool.ProxoolFacade;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 13-3-29
 * Time: 下午8:35
 * To change this template use File | Settings | File Templates.
 */
public class DMSProxoolConnectionProvider extends ProxoolConnectionProvider {
    @Override
    public void close() throws HibernateException {
        if (0 == ProxoolFacade.getAliases().length) return;
        super.close();
    }
}
