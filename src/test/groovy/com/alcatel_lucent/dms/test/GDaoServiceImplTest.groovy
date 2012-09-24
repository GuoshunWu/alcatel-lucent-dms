package com.alcatel_lucent.dms.test

import org.junit.runner.RunWith
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.transaction.TransactionConfiguration
import org.junit.Test
import com.alcatel_lucent.dms.model.ProductBase
import com.alcatel_lucent.dms.model.Product
import com.alcatel_lucent.dms.service.DaoService
import org.springframework.beans.factory.annotation.Autowired
import org.junit.Ignore
import com.alcatel_lucent.dms.service.JSONServiceImpl
import com.alcatel_lucent.dms.service.JSONService
import com.alcatel_lucent.dms.model.Application
import com.alcatel_lucent.dms.model.ApplicationBase
import net.sf.json.JSONArray
import org.apache.log4j.Logger
import org.apache.log4j.Level

/**
 * Created by IntelliJ IDEA.
 * User: guoshunw
 * Date: 12-8-16
 * Time: 下午2:49
 * To change this template use File | Settings | File Templates.
 */

//@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = ["/spring.xml"])
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)

class GDaoServiceImplTest {

    private static Logger log=Logger.getLogger(GDaoServiceImplTest.class);
    @Autowired
    private DaoService dao

    @Test
    void testRetrieveOnePage() {
        String hsql="from Application"
        Logger sqlLog=Logger.getLogger("org.hibernate.SQL");
        sqlLog.setLevel(Level.DEBUG)

        List<Application> applications=dao.retrieveOnePage hsql,2,10
        println 'applications'.center(100,'=')
        println applications
    }
}
