package com.alcatel_lucent.dms.test

import static com.alcatel_lucent.dms.service.DictionaryServiceImpl.logDictDeliverWarning
import static com.alcatel_lucent.dms.service.DictionaryServiceImpl.logDictDeliverFail
import static com.alcatel_lucent.dms.service.DictionaryServiceImpl.logDictDeliverSuccess

import com.alcatel_lucent.dms.BusinessWarning
import com.alcatel_lucent.dms.service.DaoService
import com.alcatel_lucent.dms.service.DictionaryService
import org.apache.commons.collections.keyvalue.MultiKey
import org.apache.commons.collections.map.MultiKeyMap
import org.apache.log4j.Logger
import org.apache.log4j.FileAppender

import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.transaction.TransactionConfiguration
import com.alcatel_lucent.dms.model.*
import static org.hamcrest.Matchers.*
import org.junit.*
import static org.junit.Assert.*

/**
 * @author Guoshun.Wu
 * Date: 2012-07-22
 *
 */

//@org.junit.Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = ["/spring.xml"])
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
class GDictionaryServiceImplTest {

    private static String testFilesPathDir

    @Autowired
    private DictionaryService ds

    @Autowired
    private DaoService dao

    private static Logger log = Logger.getLogger(GDictionaryServiceImplTest.class)

    @BeforeClass
    static void setUpBeforeClass() throws Exception {
        def testFilePath = new File(GDictionaryServiceImplTest.class.getResource("/").toURI())
        testFilePath = testFilePath.parentFile.parentFile
        testFilesPathDir = "${new File(testFilePath, 'dct_test_files').absolutePath}/"
        log.info "Test file path is: $testFilesPathDir"
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
	@Ignore
    void testSampleAbout_DCT() throws Exception {

        Long appId = 1L
        String encoding = null

        // langCharset mapping of language code and its source charset name
        Map<String, String> langCharset = [:]
        "CHK, GAE, FR0, EN0, DE0, IT0, PT0, ES0, US0, PL0, KO0, NO0, NL0, RU0, CH0, FI0, ES1, CS0, HU0, CH1, SV0, AR0, DA0, HE0".split("\\s*,\\s*").each {
            langCharset[it] = 'GBK'
        }
        //		print langCharset

        // langCodes Alcatel code of languages to import, null if all languages
        // should be imported
        String[] langCodes = null

        String dictName = "About.dic"

        String testFile = "CH0/About.dic"
        String updatedTestFile = "CH0/About_Changed.dic"

        String testFilePath = "$testFilesPathDir$testFile"
        println testFilePath

        Collection<BusinessWarning> warnings = []

        /***************************************** Test for deliver DCT ****************************************/

        //		com.alcatel_lucent.dms.model.Dictionary dbDict = ds.deliverDCT(dictName, testFilePath, appId,
        //				encoding, langCodes, langCharset, warnings)
        Dictionary dbDict = ds.deliverDCT(dictName, testFilePath, appId,
                encoding, langCodes, langCharset, warnings)

        // dictionary check
        dbDict = dao.retrieveOne("from Dictionary where name=:name", ['name': dictName], ["labels", "dictLanguages"] as String[]) as Dictionary
        assertThat dbDict, is(notNullValue())

        // dictionary language check
        HashSet dbLangCodes = dbDict.allLanguageCodes

        // CHK is not saved in database.
        List<String> filelangCodes = "FR0, EN0, DE0, IT0, PT0, ES0, US0, PL0, KO0, NO0, NL0, RU0, CH0, FI0, ES1, CS0, HU0, CH1, SV0, AR0, DA0, HE0"
                .split(",\\s*")
        assertTrue(dbLangCodes.containsAll(filelangCodes))
        // labels check

        Context dbCtx = dao.retrieveOne("from Context where name=:name", ['name': dictName]) as Context
        assertNotNull(dbCtx)

        //				 prepare expected result data
        List<Label> validateLabels = [
                new Label(
                        'dictionary': dbDict,
                        'reference': 'Warning: This computer program is protected by copyright law and international treaties. Unauthorized reproduction or distribution of this program, or any portion of it may result in severe civil and criminal penalties, and will be prosecuted to the maximum extent possible under the law.',
                        'key': 'WARNING',
                        'maxLength': '399'
                ),
                new Label(
                        'dictionary': dbDict,
                        'reference': 'Copyright 2007-2012 by Alcatel-Lucent. All rights reserved.\nAlcatel-Lucent and Alcatel-Lucent Logo are respectively registered\ntrademark and service mark of Alcatel-Lucent.',
                        'key': 'COPYRIGHT',
                        'maxLength': '79,86,97'
                ),
                new Label(
                        'dictionary': dbDict,
                        'reference': 'My Instant Communicator client software version ',
                        'key': 'MPC_VERSION',
                        'maxLength': '57'
                )
        ]

        MultiKeyMap translatedStringMap = new MultiKeyMap()
        translatedStringMap[new MultiKey('WARNING', 'EN0')] = 'Warning: This computer program is protected by copyright law and international treaties. Unauthorized reproduction or distribution of this program, or any portion of it may result in severe civil and criminal penalties, and will be prosecuted to the maximum extent possible under the law.'
        translatedStringMap[new MultiKey('WARNING', 'CH0')] = '警告：本计算机程序受到版权法和国际条约的保护。未经授权而复制或披露本程序或其任何部分程序，可能会受到严重的民事或刑事处罚，并将依法进行起诉'
        translatedStringMap[new MultiKey('WARNING', 'US0')] = 'Warning: This program is protected by copyright law and international treaties. Unauthorized reproduction or distribution of this program, or any portion of it may result in severe civil and criminal penalties, and violators will be prosecuted to the maximum extent possible under the law.'

        translatedStringMap[new MultiKey('COPYRIGHT', 'EN0')] = 'Copyright 2007-2012 by Alcatel-Lucent. All rights reserved.\nAlcatel-Lucent and Alcatel-Lucent Logo are respectively registered\ntrademark and service mark of Alcatel-Lucent.'
        translatedStringMap[new MultiKey('COPYRIGHT', 'CH0')] = '2007-2012年阿尔卡特朗讯版权所有。保留所有权力。\nAlcatel-Lucent与Alcatel-Lucent标识是阿尔卡特朗讯各自的注册商标和服务标记。'
        translatedStringMap[new MultiKey('COPYRIGHT', 'US0')] = 'Copyright 2007-2012 by Alcatel-Lucent. All rights reserved.\nAlcatel-Lucent and Alcatel-Lucent Logo are respectively registered\ntrademark and service mark of Alcatel-Lucent.'

        translatedStringMap[new MultiKey('MPC_VERSION', 'EN0')] = 'My Instant Communicator client software version '
        translatedStringMap[new MultiKey('MPC_VERSION', 'CH0')] = '我的即时通客户端软件版本 '
        translatedStringMap[new MultiKey('MPC_VERSION', 'US0')] = 'My Instant Communicator client software version '

        /*
           * check if there are ('EN0','CH0','US0') language codes related
           * translations
           */
        Map<String, Long> validateTranslationsLangCodeAndIDMap = [:]

        dao.retrieve("select al.language.id,al.code from AlcatelLanguageCode al where code in ('EN0','CH0','US0')").each {
            def idAndCode = it as Object[]
            validateTranslationsLangCodeAndIDMap[idAndCode[1]] = idAndCode[0]
        }


        validateLabels.each {label ->
            Label dbLabel = dbDict.getLabel(label.key)
            assertNotNull(dbLabel)

            label.context = dbCtx

            assertEquals(label.reference, dbLabel.reference)
            assertEquals(label.maxLength, dbLabel.maxLength)

            Text dbText = dao.retrieveOne("from Text where reference=:reference and context.id=:contextid",
                    ["reference": label.reference, "contextid": label.context.id], ["translations"] as String[]) as Text

            assertNotNull(dbText)

            Translation trans = null
            log.info "validating if there are ('EN0','CH0','US0') translation in database."

            validateTranslationsLangCodeAndIDMap.each {code, id ->
                trans = dbText.getTranslation(id)
                assertNotNull "Translation item for $code not found.", trans
                log.info "label key: ${label.key}, language code: $code"
                assertThat trans.translation, equalTo(translatedStringMap[new MultiKey(label.key, code)])
            }
        }

        /***************************************** Test updated test file deliver DCT ****************************************/
        testFilePath = "$testFilesPathDir$updatedTestFile"
        // re deliver the updated DCT file
        langCharset.CH1 = 'Big5'

        dbDict = ds.deliverDCT dictName, testFilePath, appId, encoding, langCodes, langCharset, warnings
        // check result

        dbDict = dao.retrieveOne("from Dictionary where name=:name", ['name': dictName], ["labels", "dictLanguages"] as String[]) as Dictionary

        // check added dictionary language
        assertThat dbDict.allLanguageCodes, hasItem("CH1")

        // check added new label TESTLABEL
        Label dbLabel = dbDict.getLabel("TESTLABEL")
        assertThat dbLabel, is(notNullValue())

        Text dbText = dao.retrieveOne("from Text where reference=:reference and context.id=:contextid",
                ["reference": dbLabel.reference, "contextid": dbLabel.context.id], ["translations"] as String[]) as Text
        // check translations

        // added translation
        translatedStringMap[new MultiKey("TESTLABEL", "EN0")] = "Test"
        translatedStringMap[new MultiKey("TESTLABEL", "US0")] = "Test"
        translatedStringMap[new MultiKey("TESTLABEL", "CH0")] = "测试"


        validateTranslationsLangCodeAndIDMap.each {code, id ->
            Translation trans = dbText.getTranslation(id)
            assertNotNull "Translation item for $code not found.", trans
            log.info "label key: $dbLabel.key, language code: $code"
            assertThat trans.translation, equalTo(translatedStringMap[new MultiKey(dbLabel.key, code)])
        }

        // updated translation

        translatedStringMap[new MultiKey("COPYRIGHT", "CH0")] = "用于测试的改变，2007-2012年阿尔卡特朗讯版权所有。保留所有权力\nAlcatel-Lucent与Alcatel-Lucent标识是阿尔卡特朗讯各自的注册商标和服务标记。"

        dbLabel = dbDict.getLabel("COPYRIGHT")
        dbText = dao.retrieveOne("from Text where reference=:reference and context.id=:contextid",
                ["reference": dbLabel.reference, "contextid": dbLabel.context.id], ["translations"] as String[]) as Text

        Translation trans = dbText.getTranslation validateTranslationsLangCodeAndIDMap["CH0"]

        assertThat trans.translation, equalTo(translatedStringMap.get(dbLabel.key, "CH0"))

        /*************************** Test generate dct file from dictionary in database *************************/
        String targetFileName = "target/${dictName}_generated.dct"
        ds.generateDCT targetFileName, dbDict.getId(), encoding, langCodes, langCharset
        def generatedFile = new File(targetFileName)
        assertTrue "Dictionary $generatedFile.name is not generated.", generatedFile.exists()

        /*************************** Test deletel dictionary in database *************************/
        ds.deleteDCT dictName
        def origDict = dbDict
        dbDict = dao.retrieveOne("from Dictionary where name=:name", ['name': dictName], ["labels", "dictLanguages"] as String[]) as Dictionary

        // check dictionary
        assertThat dbDict, is(nullValue())

        // check labels
        List<Label> labels = dao.retrieve "from Label where dictionary.id=:dictId", ['dictId': origDict.id]
        assertTrue "Some label(s): $labels in $origDict.name dictionary is(are) not deleted.", labels.isEmpty()
    }

    @Test
    void testDeliverDCTFiles() {
        Long appId = 1L
        String encoding = null

        //all language code and package def
        HashMap<String,List<String>> langCodeForPkg = [
//                'AR': ['AR0', 'ar'],
//                'CA': ['ES1', 'ca-ES', 'ca'],
//                'CS': ['cs', 'cs-CZ', 'CS0'],
                'DA': ['DA0', 'da', 'da-DK'],
                'DE': ['de', 'de-DE', 'DE0'],
                'DE-AT': ['de-AT', 'DE1'],
                'DE-CH': ['DE2', 'de-CH'],
                'EL': ['el-GR', 'GR0', 'el'],
                'EN-AU': ['AS0', 'en-AU'],
//                'EN-UK': null,
                'EN-US': ['US0', 'en-US'],
                'ES': ['ES0', 'es-ES', 'es'],
                'ET': ['et-EE', 'et', 'EE0'],
                'FI': ['fi', 'FI0', 'fi-FI'],
                'FR': ['FR0', 'fr', 'fr-FR'],
                'FR-BE': ['fr-BE', 'FR2'],
                'FR-CA': ['fr-CA'],
                'FR-CH': ['FR1', 'fr-CH'],
                'HR': ['hr', 'hr-HR', 'HR0'],
                'HU': ['HU0', 'hu', 'hu-HU'],
                'IT': ['it', 'IT0', 'it-IT'],
                'IT-CH': ['IT1', 'it-CH'],
//                'KO': ['KO0', 'ko', 'ko-KR'],
                'LV': ['lv', 'lv-LV', 'LV0'],
                'NL': ['NL0', 'nl-NL', 'nl'],
                'NL-BE': ['nl-BE', 'NL1'],
                'NO': ['no', 'no-NO', 'NO0'],
                'PL': ['PL0', 'pl', 'pl-PL'],
                'PT': ['PT0', 'pt', 'pt-PT'],
                'PT-BR': ['pt-BR', 'PT1'],
                'RO': ['RO0', 'ro', 'ro-RO'],
//                'RU': ['ru', 'RU0', 'ru-RU'],
                'SER': ['sr-YU', 'YU0', 'sr'],
                'SK': ['sk', 'sk-SK', 'SK0'],
                'SL': ['sl', 'SI0', 'sl-SI'],
                'SV': ['sv', 'sv-SE', 'SV0'],
                'TR': ['TR0', 'tr-TR', 'tr'],
//                'ZH-CN': ['zh', 'ZH0', 'CH0', 'zh-CN'],
//                'ZH-TW': ['zh-TW', 'CH1', 'TW0', 'zh-HK', 'HK0'],
//                'NULL': null
        ]

        langCodeForPkg.each {subDir, langCodes->
            String rootDir = "Z:/$subDir"
//			rootDir = "D:/tmp/$subDir"
            String testFilePath = rootDir
            log.info "Begin to import directory: $testFilePath".center(100,'=')
            log.debug "rootDir=$rootDir"
            log.debug "langCodes=$langCodes"

//            testFilePath = "$rootDir/6.6.000.107.a/adaptation_layer/rms/AdaptationLayer/Util/UtAl.dic"

            changeLoggerFile subDir,"SUCCESS",logDictDeliverSuccess
            changeLoggerFile subDir,"WARNING",logDictDeliverWarning
            changeLoggerFile subDir,"FAIL",   logDictDeliverFail

            Collection<BusinessWarning> warnings = []

            String header = String.format("%s, %s, %s, %s", "Name", "encoding", "Path", "cause")
            // set the log files

            logDictDeliverFail.info header
            logDictDeliverWarning.info header

            long before = System.currentTimeMillis()
            Collection<Dictionary> dictionaries = ds.deliverDCTFiles rootDir, new File(testFilePath), appId, encoding, langCodes as String[], null, warnings
            long after = System.currentTimeMillis()
            log.info "Using a total of ${after - before} millisecond to perform delivering."

            assertThat dictionaries, is(notNullValue())

            String format = "%s, %s, %s, %s"
            logDictDeliverSuccess.info String.format(format, "ID", "name", "encoding", "path")

            dictionaries.each {dict ->
                logDictDeliverSuccess.info String.format(format, dict.id, dict.name, dict.encoding, dict.path)
            }

        }
    }


    /**
     * Change the statistics Logger file on the fly
     * **/
    private void changeLoggerFile(String packageName, String appenderExtName, Logger logger) {
        /**
         * Set logger appender file names
         * */
        FileAppender appender = logger.getAppender("FILE_DIC_DELIVER_$appenderExtName") as FileAppender
        String originalFileName = new File(appender.file).name;
        String[] fileAndExt = originalFileName.split("\\.")
        appender.file = "Z:/${fileAndExt[0]}_${packageName}.${fileAndExt[1]}"
        appender.activateOptions()
    }
}
