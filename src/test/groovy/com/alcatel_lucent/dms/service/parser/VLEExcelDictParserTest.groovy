package com.alcatel_lucent.dms.service.parser;


import com.alcatel_lucent.dms.model.Dictionary
import org.junit.After
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.transaction.TransactionConfiguration
import org.junit.AfterClass
import org.logicalcobwebs.proxool.ProxoolFacade

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
public class VLEExcelDictParserTest {

    @Autowired
    private XMLDictParser VLEExcelDictParser = new XMLDictParser();

    @BeforeClass
    static void setUpBeforeClass() throws Exception {

    }

    @AfterClass
    static void tearDownAfterClass() throws Exception {
//        ProxoolFacade.shutdown(0)
    }

    @Before
    void setUp() throws Exception {

    }

    @After
    void tearDown() throws Exception {

    }

    @Test
    void testParse() throws Exception {
        File file = new File("D:/testxdct")
        println "=" * 100
//        ArrayList<Dictionary> dictionaries= xmlDictParser.parse('', file, [] as Collection<File>)
//        dictionaries.each {dict->
//            println dict
//        }
    }

}
