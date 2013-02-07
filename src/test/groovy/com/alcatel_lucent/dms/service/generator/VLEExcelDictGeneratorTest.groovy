package com.alcatel_lucent.dms.service.generator

import com.alcatel_lucent.dms.model.Dictionary
import com.alcatel_lucent.dms.model.DictionaryBase
import com.alcatel_lucent.dms.service.parser.VLEExcelDictParser
import org.apache.commons.collections.MapIterator
import org.apache.commons.collections.map.HashedMap
import org.junit.*
import org.junit.runner.RunWith
import org.logicalcobwebs.proxool.ProxoolFacade
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.transaction.TransactionConfiguration

import static org.apache.commons.io.FileUtils.copyFile

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

public class VLEExcelDictGeneratorTest {

    @Autowired
    private VLEExcelGenerator VLEExcelGenerator = new VLEExcelGenerator();
    @Autowired
    private VLEExcelDictParser VLEExcelDictParser = new VLEExcelDictParser();

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
        println "=" * 100

        File file = new File("D:/MyDocuments/Alcatel_LucentSBell/DMS/DMSFiles/VLEDict/test")
        ArrayList<Dictionary> dictionaries = VLEExcelDictParser.parse('D:/MyDocuments/Alcatel_LucentSBell/DMS/DMSFiles/VLEDict/test', file, [] as Collection<File>)
        dictionaries.each { dict ->
            VLEExcelGenerator.generateDict(new File("E:/test"), dict)
        }

    }

//    @Test
    void testMapIterator() {
        HashedMap map = ["LABEL": 1, "Max Length": 2, "English": 3, "France": 4]
        MapIterator itr = map.mapIterator()
        println "FirstTime".center(100, '=')
        while (itr.hasNext()) {
            String key = itr.next()
            Integer value = itr.getValue()

            println "Key=$key, value= $value."
        }
        println "Second time".center(100, '=')
        itr = map.mapIterator()
        while (itr.hasNext()) {
            String key = itr.next()
            Integer value = itr.getValue()

            println "Key=$key, value= $value."
        }
    }
}
