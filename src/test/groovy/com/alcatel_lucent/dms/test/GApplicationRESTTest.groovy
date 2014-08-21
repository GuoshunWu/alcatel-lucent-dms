package com.alcatel_lucent.dms.test

import com.alcatel_lucent.dms.config.AppConfig
import com.alcatel_lucent.dms.rest.AppTranslationHistoryREST
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.transaction.TransactionConfiguration
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.transaction.annotation.Transactional

/**
 * Created by IntelliJ IDEA.
 * User: guoshunw
 * Date: 12-8-31
 * Time: 下午10:03
 * To change this template use File | Settings | File Templates.
 */
//@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration()
@ContextConfiguration(classes = [AppConfig])

@Transactional //Important, or the transaction control will be invalid
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
class RESTTest {


    @Autowired
    private AppTranslationHistoryREST appTransHistoryREST

    @BeforeClass
    static void setUpBeforeClass() throws Exception {

    }

    @Test
    void testTranslationHistories(){

    }
}
