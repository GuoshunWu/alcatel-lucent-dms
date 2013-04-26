package com.alcatel_lucent.dms.test

import org.junit.BeforeClass
import org.junit.Test
import com.alcatel_lucent.dms.model.ProductBase
import com.alcatel_lucent.dms.model.ApplicationBase
import com.alcatel_lucent.dms.model.Application
import com.alcatel_lucent.dms.rest.ApplicationREST
import org.junit.Ignore

/**
 * Created by IntelliJ IDEA.
 * User: guoshunw
 * Date: 12-8-31
 * Time: 下午10:03
 * To change this template use File | Settings | File Templates.
 */
@Ignore
class GApplicationRESTTest {

    ApplicationREST appRest=new ApplicationREST()

    @BeforeClass
    static void setUpBeforeClass() throws Exception {

    }

    @Test
    void testRetrieveAllProductByProductBaseId(){
        ProductBase productBase=new ProductBase(id: 1,name: 'ISC')
        ApplicationBase appBase=new ApplicationBase(productBase: productBase,name: "TestApp1")
        Application app=new Application(base: appBase,version: 1,dictionaries: [new com.alcatel_lucent.dms.model.Dictionary()]);

    }
}
