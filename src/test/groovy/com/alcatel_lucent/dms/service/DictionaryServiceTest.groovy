package com.alcatel_lucent.dms.service

import com.alcatel_lucent.dms.model.Dictionary
import com.alcatel_lucent.dms.service.generator.StandardExcelGenerator
import com.alcatel_lucent.dms.service.parser.StandardExcelDictParser
import org.apache.commons.collections.MapIterator
import org.apache.commons.collections.map.HashedMap
import org.junit.*
import org.junit.runner.RunWith
import org.logicalcobwebs.proxool.ProxoolFacade
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.transaction.TransactionConfiguration

/**
 * Created by IntelliJ IDEA.
 * User: guoshunw
 * Date: 12-12-18
 * Time: 上午11:50
 * To change this template use File | Settings | File Templates.
 */


//@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = ["/spring.xml"])
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)

public class DictionaryServiceTest {

    @Autowired
    private DictionaryService dictionaryService;


    @BeforeClass
    static void setUpBeforeClass() throws Exception {

    }

    @AfterClass
    static void tearDownAfterClass() throws Exception {
    }

    @Before
    void setUp() throws Exception {

    }

    @After
    void tearDown() throws Exception {
        ProxoolFacade.shutdown(0);
    }

    @Test
    void testGenerateDict() throws Exception {
        /*
        Generating a xdct file which id is 13 from DB
        * */
        long id = 13
        dictionaryService.generateDictFiles("D:/testxdct/test/generate", [id])
    }
}
