package com.alcatel_lucent.dms.service

import com.alcatel_lucent.dms.service.parser.OTCWebParser
import org.apache.commons.io.ByteOrderMark
import org.apache.commons.io.IOUtils
import org.apache.commons.io.input.BOMInputStream
import org.junit.*
import org.junit.runner.RunWith
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
    private DictionaryService dictionaryService

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
    void testParserForOTCWeb() {
        File f = new File('D:/MyDocuments/Alcatel_LucentSBell/DMS/DMSFiles/OTC_Web')
        println dictionaryService.previewDictionaries(f.absolutePath, f, 1)
    }

//    @Test
    void testGenerateDictForOTCPC() {
        dictionaryService.generateDictFiles("D:/test/", [51])
    }

//    @Test
    void testBOM() {
        File f = new File("D:/MyDocuments/Alcatel_LucentSBell/DMS/DMSFiles/ACSTextDict/MyTest.txt")
        FileInputStream fis = new FileInputStream(f)
        BOMInputStream bis = new BOMInputStream(fis, ByteOrderMark.UTF_8, ByteOrderMark.UTF_16BE, ByteOrderMark.UTF_16LE)
        String encoding = bis.BOMCharsetName
        while (bis.hasBOM()) {
            bis = new BOMInputStream(bis, ByteOrderMark.UTF_8, ByteOrderMark.UTF_16BE, ByteOrderMark.UTF_16LE)
        }
        println "encoding=${encoding}"
        List<String> lines = IOUtils.readLines(bis, encoding)
        lines.each { line ->
            println line
            return
        }

        IOUtils.closeQuietly(bis)
    }
}
