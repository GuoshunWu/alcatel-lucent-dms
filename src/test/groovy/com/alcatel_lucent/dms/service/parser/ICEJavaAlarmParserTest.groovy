package com.alcatel_lucent.dms.service.parser

import com.alcatel_lucent.dms.model.Dictionary
import com.alcatel_lucent.dms.util.XDCPDTDEntityResolver
import org.dom4j.Document
import org.dom4j.io.SAXReader
import org.junit.*
import org.junit.runner.RunWith
import org.logicalcobwebs.proxool.ProxoolFacade
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.transaction.TransactionConfiguration
import org.xml.sax.InputSource
import org.xml.sax.SAXException

/**
 * Created by IntelliJ IDEA.
 * User: guoshunw
 * Date: 12-12-18
 * Time: 上午11:50
 * To change this template use File | Settings | File Templates.
 */

@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = ["/spring.xml"])
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)

public class ICEJavaAlarmParserTest {

    @Autowired
    private ICEJavaAlarmParser iceJavaAlarmParser = new ICEJavaAlarmParser();

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
    }

    @Test
    void testParse() throws Exception {
//        File file = new File("D:/MyDocuments/Alcatel_LucentSBell/DMS/DMSFiles/ICEJavaAlarm/catalog-builder-plugin-1.3.000.000-schemas/")
//
//        println "=" * 100
//        ArrayList<Dictionary> dictionaries = iceJavaAlarmParser.parse(file.absolutePath, file, [] as Collection<File>)
//
//        dictionaries.each { dict ->
//            println dict
//        }
    }
}
