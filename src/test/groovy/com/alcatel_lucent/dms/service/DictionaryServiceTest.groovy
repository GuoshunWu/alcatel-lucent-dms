package com.alcatel_lucent.dms.service

import com.alcatel_lucent.dms.Constants
import com.alcatel_lucent.dms.UserContext
import com.alcatel_lucent.dms.model.User
import com.alcatel_lucent.dms.util.Util
import org.apache.commons.io.ByteOrderMark
import org.apache.commons.io.IOUtils
import org.apache.commons.io.input.BOMInputStream
import org.junit.*
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.orm.hibernate3.SessionFactoryUtils
import org.springframework.test.annotation.Rollback
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.transaction.TransactionConfiguration
import org.springframework.transaction.annotation.Transactional

import javax.annotation.Resource

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
@Transactional //Important, or the transaction control will be invalid
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)

public class DictionaryServiceTest {

    @Autowired
    private DictionaryService dictionaryService

    @Resource
    private DaoService daoService


    private String testFileRoot = 'D:/MyDocuments/Alcatel_LucentSBell/DMS/DMSFiles'

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
//    @Rollback(false)
    void testDictionaryProcess() {
        String targetDir = "D:/test/dictgenerate/"

        String srcPath = 'OTC_Web'
        srcPath = 'ACSTextDict'
        srcPath = 'VoiceApp/test'



        File f = new File("${testFileRoot}/${srcPath}")

        UserContext.userContext = new UserContext(Locale.ENGLISH, new User('guoshunw', "Guoshun WU", 'Guoshun.Wu@alcatel-sbell.com.cn'))

        Collection<com.alcatel_lucent.dms.model.Dictionary> dicts = dictionaryService.previewDictionaries(f.absolutePath, f, 1)
        println "About to import dicts: ${dicts}".center(100, '=')

        dictionaryService.importDictionaries(1, dicts, Constants.ImportingMode.DELIVERY)

        daoService.session.clear()

        // There may be dictionaries errors need to be adjust manually here
        Collection<Long> dbIds=daoService.retrieve('select id from Dictionary where base.name in :names', ['names': Util.getObjectProperiesList(dicts, 'name')])
        dictionaryService.generateDictFiles(targetDir, dbIds)

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
