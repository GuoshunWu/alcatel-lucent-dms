package com.alcatel_lucent.dms.test

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.transaction.TransactionConfiguration
import com.alcatel_lucent.dms.service.ProductService
import org.springframework.beans.factory.annotation.Autowired

/**
 * Created by IntelliJ IDEA.
 * User: guoshunw
 * Date: 12-8-21
 * Time: 下午5:46
 * To change this template use File | Settings | File Templates.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = ["/spring.xml"])
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
class GProductServiceImplTest {


    @Autowired
    private ProductService productService

    @Test
    public void testRetrieveAll() throws Exception {
        println productService.retrieveAll()
    }
}
