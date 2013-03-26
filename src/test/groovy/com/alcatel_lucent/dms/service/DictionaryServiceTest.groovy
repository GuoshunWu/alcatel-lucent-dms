package com.alcatel_lucent.dms.service

import com.alcatel_lucent.dms.service.parser.ACSTextDictParser
import org.apache.commons.io.ByteOrderMark
import org.apache.commons.io.IOUtils
import org.apache.commons.io.input.BOMInputStream
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

@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = ["/spring.xml"])
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)

public class DictionaryServiceTest {

    @Autowired
    private DictionaryService dictionaryService

    @Autowired
    private ACSTextDictParser acsTextDictParser


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
        ProxoolFacade.shutdown(0)
    }

//    @Test
    void testGenerateDictForACSText() throws Exception {
        File f = new File("D:/MyDocuments/Alcatel_LucentSBell/DMS/DMSFiles/ACSTextDict")
        Collection<File> acceptedFiles = [] as Collection<File>
        println "Dictionaies List".center(100, '=')
        acsTextDictParser.parse(f.absolutePath, f, acceptedFiles).each { dict ->
            println dict
        }

        println "Accepted Files: ".center(100, '=')
        acceptedFiles.each { aFile ->
            println aFile
        }
    }

    @Test
    void testBOM(){
        File f = new File("D:/MyDocuments/Alcatel_LucentSBell/DMS/DMSFiles/ACSTextDict/MyTest.txt")
        FileInputStream fis = new FileInputStream(f)
        BOMInputStream bis = new BOMInputStream(fis, true, ByteOrderMark.UTF_8, ByteOrderMark.UTF_16BE, ByteOrderMark.UTF_16LE)
        List<String> lines = IOUtils.readLines(bis, bis.getBOMCharsetName())
        lines.each {line->
            println line
            return
        }

        IOUtils.closeQuietly(bis)
    }
}
