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
class DumbTest {


    @Autowired
    private DaoService dao

    @Test
    void testDumb() {
        println "Hello, dump..."
//        ProductBase pb = new ProductBase(name: "ISC")
//        Product p1 = new Product(version: "1.0")
//        p1.base=pb
//        Product p2=new Product(version: "2.0")
//        p2.base= pb
//
//        pb.products=[p1, p2]  as Set<Product>
//
//        dao.create(pb)
//        dao.create(p1)
//        dao.create(p2)
//
//        dao.delete('delete from ProductBase where name=:name',['name':'ISC'] as Map)


    }
}
