package com.alcatel_lucent.dms.service

import com.alcatel_lucent.dms.Constants
import com.alcatel_lucent.dms.UserContext
import com.alcatel_lucent.dms.config.AppConfig
import com.alcatel_lucent.dms.model.User
import com.alcatel_lucent.dms.util.Util
import org.apache.commons.io.ByteOrderMark
import org.apache.commons.io.IOUtils
import org.apache.commons.io.input.BOMInputStream
import org.junit.*
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.transaction.TransactionConfiguration
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.transaction.annotation.Transactional

import javax.annotation.Resource

/**
 * Created by IntelliJ IDEA.
 * User: guoshunw
 * Date: 12-12-18
 * Time: 上午11:50
 * To change this template use File | Settings | File Templates.
 */

@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration()
@ContextConfiguration(classes = [AppConfig])

@Transactional
//Important, or the transaction control will be invalid
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)

public class DictionaryServiceTest {

    @Autowired
    private DictionaryService dictionaryService

    @Resource
    private DaoService daoService

//    @Resource
//    private OTCAndoridOrIPhoneParser otcAndoridOrIPhoneParser

    private String testFileRoot = 'D:/Documents/Alcatel_Lucent/DMS/exampleFiles'

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

//    @Test
    void tempTest() {
        com.alcatel_lucent.dms.model.Dictionary dict = daoService.retrieve(com.alcatel_lucent.dms.model.Dictionary, 1L)
        dict.validate(false)

        println dict.dictWarnings
    }

//    @Test
    void testMe() {}

//    @Test
//    @Rollback(false)
    void testDictionaryProcess() {
        String targetDir = "D:/test/dictgenerate/"
        String srcPath = 'Test/ts'

        File f = new File("${testFileRoot}/${srcPath}")

        UserContext.userContext = new UserContext(Locale.ENGLISH, new User('admin', "Guoshun WU", 'Guoshun.Wu@alcatel-sbell.com.cn'), null)

        Collection<com.alcatel_lucent.dms.model.Dictionary> dicts = dictionaryService.previewDictionaries(f.absolutePath, f, 1)

        println "About to import dicts: ${dicts}".center(100, '=')
        dictionaryService.importDictionaries(1, dicts, Constants.ImportingMode.DELIVERY)
        daoService.session.clear()

//        There may be dictionaries transWarnings need to be adjust manually here
        List dictNames = Util.getObjectPropertiesList(dicts, 'name')
//        List dictNames = Arrays.asList(
////                'OTC-Android_languages-v2.0.30.0.xls',
//                'Test2'
//        )
        if (dictNames.isEmpty()) return

        Collection<Long> dbIds = daoService.retrieve('select id from Dictionary where base.name in :names', ['names': dictNames])
        dictionaryService.generateDictFiles(targetDir, dbIds)
        println "Test done.".center(100, '=')
    }

//    @Test
//    @Rollback(true)
    void testDBRollback() {
        daoService.create(new User('test', 'Test', 'Test@alcatel-lucent.com'))
        println daoService.retrieve("from User where loginName = 'test'")
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
