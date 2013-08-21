package com.alcatel_lucent.dms.service.parser

import com.alcatel_lucent.dms.model.Dictionary
import org.apache.commons.io.ByteOrderMark
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.apache.commons.io.input.BOMInputStream
import org.junit.*
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.transaction.TransactionConfiguration
import org.springframework.transaction.annotation.Transactional

import java.util.regex.Matcher
import java.util.regex.Pattern

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
@Transactional
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
public class VitalSuiteParserTest {

    private String testFileRoot = 'D:/Documents/Alcatel_Lucent/DMS/exampleFiles'

    @Autowired
    private VitalSuiteDictParser vitalSuiteDictParser = new VitalSuiteDictParser();

    @BeforeClass
    static void setUpBeforeClass() throws Exception {

    }

    @Before
    void setUp() throws Exception {

    }

    @After
    void tearDown() throws Exception {

    }

    @Test
    void testParse() throws Exception {
        String srcPath = 'VitalSuite R12.5/test'
        File f = new File("${testFileRoot}/${srcPath}")
        println "Vital suite dictionaries".center(100, '=')
        println vitalSuiteDictParser.parse(f.absolutePath, f, [])

    }

//    @Test
    void testValidation() {
        Pattern p = Pattern.compile("^((?://.*\\s*)*\\s*[a-zA-Z]{2}(?:[-_][a-zA-Z]{2})?\\s*(?://.*\\s)*)\\s*\\{([\\s\\S]*)\\}\\s*(?://.*\\s*)*\$");
        String srcPath = 'VitalSuite R12.5/test'
        File f = new File("${testFileRoot}/${srcPath}/zh.txt")
        String text = FileUtils.readFileToString(f)
        Matcher m = p.matcher(text)
        println m.matches()
    }

}
