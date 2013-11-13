package com.alcatel_lucent.dms.service

import ch.qos.logback.core.FileAppender
import com.alcatel_lucent.dms.BusinessWarning
import com.alcatel_lucent.dms.Constants
import com.alcatel_lucent.dms.model.*
import org.apache.commons.collections.keyvalue.MultiKey
import org.apache.commons.collections.map.MultiKeyMap
import org.junit.*
import org.junit.runner.RunWith
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.transaction.TransactionConfiguration
import org.springframework.transaction.annotation.Transactional

import static org.apache.commons.lang.StringUtils.join
import static org.hamcrest.Matchers.*
import static org.junit.Assert.*

/**
 * @author Guoshun.Wu
 * Date: 2012-07-22
 *
 */

@org.junit.Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = ["/spring.xml"])
@Transactional
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
class GDictionaryServiceImplTest {

    private static String testFilesPathDir

    @Autowired
    private DictionaryService ds

    @Autowired
    private DaoService dao

    @Autowired
    private DictionaryProp dictProp;

    private static Logger log = LoggerFactory.getLogger(GDictionaryServiceImplTest.class)


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
    void testDictionaryService() {
        println "Just a test..."
    }

//    @Test
    void testSampleAbout_DCT() throws Exception {

        Logger logDictDeliverSuccess = LoggerFactory.getLogger("DictDeliverSuccess")
        Logger logDictDeliverFail = LoggerFactory.getLogger("DictDeliverFail")
        Logger logDictDeliverWarning = LoggerFactory.getLogger("DictDeliverWaning")

        if(null == logDictDeliverSuccess){
            log.error("logDictDeliverSuccess haven't configured in log4j.xml")
            return
        }


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

        String dictName = "CH0/About.dic"
        String version = '1.0'
        String testFile = "CH0/About.dic"
        String updatedTestFile = "CH0/About_Changed.dic"

        String testFilePath = "$testFilesPathDir$testFile"
        println testFilePath

        Collection<BusinessWarning> warnings = []

        /***************************************** Test for deliver DCT ****************************************/
        Collection<Dictionary> dicts = ds.previewDictionaries testFilesPathDir, new File(testFilePath)
        DeliveryReport report = new DeliveryReport()
        Dictionary dbDict = ds.importDictionary appId, dicts[0], version, Constants.DELIVERY_MODE, langCodes, langCharset, warnings, report
        //Dictionary dbDict = ds.deliverDCT dictName, version, testFilePath, appId, Constants.DELIVERY_MODE, encoding, langCodes, langCharset, warnings

        // dictionary check
        dbDict = dao.retrieveOne("from Dictionary where version=:version and base.name=:name", ['name': dictName, 'version': version], ["labels", "dictLanguages"] as String[]) as Dictionary

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

        // prepare expected result data
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

        dicts = ds.previewDictionaries testFilesPathDir, new File(testFilePath)
        dicts[0].setName dictName
        dicts[0].labels.each {
            it.context.name = dictName
        }
        dbDict = ds.importDictionary appId, dicts[0], version, Constants.DELIVERY_MODE, langCodes, langCharset, warnings, new DeliveryReport()
//        dbDict = ds.deliverDCT dictName, version, testFilePath, appId, Constants.DELIVERY_MODE, encoding, langCodes, langCharset, warnings
        // check result

        dbDict = dao.retrieveOne("from Dictionary where version=:version and base.name=:name", ['name': dictName, 'version': version], ["labels", "dictLanguages",] as String[]) as Dictionary

        // check added dictionary language
        assertThat dbDict.allLanguageCodes, hasItem("CH1")

        // check added new label TESTLABEL
        Label dbLabel = dbDict.getLabel("TESTLABEL")
        dbLabel = dao.retrieveOne('from Label where id=:id', ['id': dbLabel.id], ['context'] as String[])


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
        dicts = ds.previewDictionaries testFilesPathDir, new File(testFilePath)
        dicts[0].setName dictName
        dicts[0].labels.each {
            it.context.name = dictName
        }
        dbDict = ds.importDictionary appId, dicts[0], version, Constants.TRANSLATION_MODE, langCodes, langCharset, warnings, new DeliveryReport()

        translatedStringMap[new MultiKey("COPYRIGHT", "CH0")] = "用于测试的改变，2007-2012年阿尔卡特朗讯版权所有。保留所有权力\nAlcatel-Lucent与Alcatel-Lucent标识是阿尔卡特朗讯各自的注册商标和服务标记。"

//        dbLabel = dbDict.getLabel("COPYRIGHT")
        dbLabel = dao.retrieveOne('from Label where dictionary.id=:dictId and key=:key', ['dictId': dbDict.id, "key": "COPYRIGHT"], ['context'] as String[])

        dbText = dao.retrieveOne("from Text where reference=:reference and context.id=:contextid",
                ["reference": dbLabel.reference, "contextid": dbLabel.context.id], ["translations"] as String[]) as Text

        Translation trans = dbText.getTranslation validateTranslationsLangCodeAndIDMap["CH0"]

        assertThat trans.translation, equalTo(translatedStringMap.get(dbLabel.key, "CH0"))

        /*************************** Test generate dct file from dictionary in database *************************/
        String targetFileName = "target/${dictName}_generated.dct"
//        ds.generateDCT targetFileName, dbDict.getId(), encoding, langCodes
//        def generatedFile = new File(targetFileName)
//        assertTrue "Dictionary $generatedFile.name is not generated.", generatedFile.exists()

        /*************************** Test deletel dictionary in database *************************/
//        daoService.delete 'delete from Dictionary where version=:version and base.name=:name', ['version':version, 'name':dictName] as Map<String,String>
        dbDict = dao.retrieveOne("from Dictionary where version=:version and base.name=:name", ['name': dictName, 'version': version], ['base'] as String[]) as Dictionary
        ds.deleteDictionary dbDict.id
        Dictionary origDict = dbDict
        dbDict = dao.retrieveOne("from Dictionary where version=:version and base.name=:name", ['name': dictName, 'version': version]) as Dictionary

        // check dictionary
        assertThat dbDict, is(nullValue())

        // check labels
        List<Label> labels = dao.retrieve "from Label where dictionary.id=:dictId", ['dictId': origDict.id]
        assertTrue "Some label(s): $labels in $origDict.name dictionary was(were) not deleted.", labels.isEmpty()
    }

//    @Test
    void testDeliverDCTFiles() {
        //all language code and package def
        HashMap<String, List<String>> langCodeForPkg = [
                'EN-UK': null,
//                'ZH-CN': ['zh', 'ZH0', 'CH0', 'zh-CN'],
//                'ZH-TW': ['zh-TW', 'CH1', 'TW0', 'zh-HK', 'HK0'],
//                'AR': ['AR0', 'ar'],
//                'CA': ['ES1', 'ca-ES', 'ca'],
//                'CS': ['cs', 'cs-CZ', 'CS0'],
//                'DA': ['DA0', 'da', 'da-DK'],
//                'DE': ['de', 'de-DE', 'DE0'],
//                'DE-AT': ['de-AT', 'DE1'],
//                'DE-CH': ['DE2', 'de-CH'],
//                'EL': ['el-GR', 'GR0', 'el'],
//                'EN-AU': ['AS0', 'en-AU'],
//                'EN-US': ['US0', 'en-US'],
//                'ES': ['ES0', 'es-ES', 'es'],
//                'ET': ['et-EE', 'et', 'EE0'],
//                'FI': ['fi', 'FI0', 'fi-FI'],
//                'FR': ['FR0', 'fr', 'fr-FR'],
//                'FR-BE': ['fr-BE', 'FR2'],
//                'FR-CA': ['fr-CA'],
//                'FR-CH': ['FR1', 'fr-CH'],
//                'HR': ['hr', 'hr-HR', 'HR0'],
//                'HU': ['HU0', 'hu', 'hu-HU'],
//                'IT': ['it', 'IT0', 'it-IT'],
//                'IT-CH': ['IT1', 'it-CH'],
//                'KO': ['KO0', 'ko', 'ko-KR'],
//                'LV': ['lv', 'lv-LV', 'LV0'],
//                'NL': ['NL0', 'nl-NL', 'nl'],
//                'NL-BE': ['nl-BE', 'NL1'],
//                'NO': ['no', 'no-NO', 'NO0'],
//                'PL': ['PL0', 'pl', 'pl-PL'],
//                'PT': ['PT0', 'pt', 'pt-PT'],
//                'PT-BR': ['pt-BR', 'PT1'],
//                'RO': ['RO0', 'ro', 'ro-RO'],
//                'RU': ['ru', 'RU0', 'ru-RU'],
//                'SER': ['sr-YU', 'YU0', 'sr'],
//                'SK': ['sk', 'sk-SK', 'SK0'],
//                'SL': ['sl', 'SI0', 'sl-SI'],
//                'SV': ['sv', 'sv-SE', 'SV0'],
//                'TR': ['TR0', 'tr-TR', 'tr'],
        ]

//        Map<String, String> mdcLangCharset = [:]
//        ['ca-ES', 'cs-CZ', 'da-DK', 'de-AT', 'de-CH', 'de-DE', 'el-GR', 'en-AU', 'en-CA', 'en-CN',
//                'en-GB', 'en-GR', 'en-MA', 'en-RU', 'en-TW', 'en-US', 'es-AR', 'es-ES', 'es-MX', 'et-EE',
//                'fi-FI', 'fr-CA', 'fr-CH', 'fr-FR', 'fr-MA', 'hr-HR', 'hu-HU', 'it-CH', 'it-IT', 'ja-JP',
//                'ko-KR', 'lt-LT', 'lv-LV', 'nl-BE', 'nl-NL', 'no-NO', 'pl-PL', 'pt-BR', 'pt-PT', 'ro-RO',
//                'ru-RU', 'sk-SK', 'sl-SI', 'sr-CS', 'sv-SE', 'tr-TR', 'zh-CN', 'zh-TW'].each {code ->
//            mdcLangCharset.put(code, 'UTF-8')
//        }

        langCodeForPkg.each {subDir, langCodes ->
            int mode = subDir.equals('EN-UK') ? Constants.DELIVERY_MODE : Constants.TRANSLATION_MODE
//            String rootDir = "Z:/$subDir"
//			String rootDir = "D:/temp/prop_test_in"
            String rootDir = "D:/translation/ICS_OAMP/6.6_translated"
//			mode = Constants.TRANSLATION_MODE
            String testFilePath = rootDir
            log.info "Begin to import directory: $testFilePath".center(100, '=')
            log.debug "rootDir=$rootDir"
            log.debug "langCodes=$langCodes"

//            testFilePath = "$rootDir/6.6.000.107.a/data_access_service/dataaccess/WEB-INF/classes/com/alcatel/dataaccess/global/dico/DtaXmlSchema.dct"

//			rootDir = "D:/projects/translation/6.7.1"
//			testFilePath = "$rootDir/6.6.000.107.a/voice_applications/eCC_tui/VoiceApplications/dictionaries/TUI.dct"


            changeLoggerFile subDir, "SUCCESS", logDictDeliverSuccess
            changeLoggerFile subDir, "WARNING", logDictDeliverWarning
            changeLoggerFile subDir, "FAIL", logDictDeliverFail

            Collection<BusinessWarning> warnings = []

            String header = String.format("%s, %s, %s, %s", "Name", "encoding", "Path", "cause")
            // set the log files

            logDictDeliverFail.info header
            logDictDeliverWarning.info header

            long before = System.currentTimeMillis()
            //Collection<Dictionary> dictionaries = ds.deliverDCTFiles rootDir, new File(testFilePath), appId, mode, encoding, langCodes as String[], null, warnings
            Collection<Dictionary> dictionaries = ds.previewDictionaries rootDir, new File(testFilePath)
            dictionaries.each {dict ->
                Map<String, String> langCharset
                if (dict.getFormat().equals("DCT")) {
                    try {
                        langCharset = dictProp.getDictionaryCharsets(dict.getName())
                    } catch (Exception e) {
                        langCharset = [default: 'UTF-8']
                    }
                } else if (dict.getFormat().equals("Dictionary conf")) {
                    langCharset = [default: 'UTF-8']
                } else if (dict.getFormat().equals("XML labels")) {
                    langCharset = [default: 'UTF-8']
                } else if (dict.getFormat().equals("XML properties")) {
                    langCharset = [default: 'UTF-8']
                } else {
                    langCharset = [default: 'ISO-8859-1']
                }
                Long appId = 1
                String dictVersion = "1.0"
                ds.importDictionary appId, dict, dictVersion, mode, langCodes as String[], langCharset, warnings, new DeliveryReport()
                if (!warnings.isEmpty()) {
                    join(warnings, '\n').replace("\"", "\"\"");
                    String forCSV = warnings.toString().replace("\"", "\"\"");
                    forCSV = join(warnings, '\n').replace("\"", "\"\"");
                    logDictDeliverWarning.warn(String.format("%s,%s,%s,\"%s\"",
                            dict.getName(), dict.getEncoding(), dict.getPath(),
                            forCSV));
                    warnings.clear()
                }
            }

//            Collection<Dictionary> dictionaries = ds.deliverMDCFiles rootDir,new File(testFilePath), appId, mode, langCodes as String[], langCharset, warnings

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

//    @Test
    void testGenerateDctFiles() {
        Collection<Long> dictionaryIds = dao.retrieve('select id from Dictionary') as List<Long>
        ds.generateDictFiles("D:/temp/prop_test_out", dictionaryIds)
    }

    /**
     * Change the statistics Logger file on the fly
     * **/
    private void changeLoggerFile(String packageName, String appenderExtName, Logger logger) {
        /**
         * Set logger appender file names
         * */
        FileAppender appender = logger.getAppender("FILE_DIC_DELIVER_$appenderExtName") as FileAppender
        appender.file = "Z:/DictionaryDeliver${appenderExtName.toLowerCase().capitalize()}_${packageName}.csv"
        appender.activateOptions()
    }

//    @Test
    void testDeliverMDC() {
        String rootDir = 'Z:/CA'
        String dictName = '/6.6.000.107.a/smart_prs/prs/smartprs/etc/conf/dictionary.conf'
        String path = "$rootDir$dictName"
        InputStream fis = new FileInputStream(path);
        Collection<BusinessWarning> warnings = new HashSet<BusinessWarning>()

        Dictionary dict = ds.previewMDC dictName, path, fis, 1L, warnings
        fis.close()

        Map<String, String> langCharset = [
                'en-GB': 'UTF-8',
                'fr-FR': 'UTF-8',
                'de-DE': 'UTF-8',
                'es-ES': 'UTF-8',
                'it-IT': 'UTF-8',
                'pt-PT': 'UTF-8',
                'no-NO': 'UTF-8',
                'en-US': 'UTF-8',
                'ca-ES': 'UTF-8',
                'nl-NL': 'UTF-8',
                'fi-FI': 'UTF-8',
                'cs-CZ': 'UTF-8',
                'pl-PL': 'UTF-8',
                'ru-RU': 'UTF-8',
                'zh-CN': 'UTF-8',
                'ko-KR': 'UTF-8',
                'hu-HU': 'UTF-8',
                'zh-TW': 'UTF-8',
                'da-DK': 'UTF-8',
                'de-CH': 'UTF-8',
                'et-EE': 'UTF-8',
                'ja-JP': 'UTF-8',
                'lt-LT': 'UTF-8',
                'nl-BE': 'UTF-8',
                'ro-RO': 'UTF-8',
                'sv-SE': 'UTF-8',
        ]
        String[] langCodes = null
        ds.importDictionary dict, langCodes, langCharset, warnings, new DeliveryReport()

        assertNotNull dict

    }
}
