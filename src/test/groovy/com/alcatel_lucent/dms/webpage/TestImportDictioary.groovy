package com.alcatel_lucent.dms.webpage

import com.alcatel_lucent.dms.util.WebPageUtil
import com.google.common.base.Predicate
import com.google.common.collect.Collections2
import org.intellij.lang.annotations.Language
import org.junit.AfterClass
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import static com.alcatel_lucent.dms.util.WebPageUtil.*
import static com.google.common.collect.Collections2.filter
import static java.util.concurrent.TimeUnit.MICROSECONDS
import static java.util.concurrent.TimeUnit.MILLISECONDS
import static java.util.concurrent.TimeUnit.SECONDS
import static org.hamcrest.CoreMatchers.allOf
import static org.hamcrest.CoreMatchers.not
import static org.hamcrest.Matchers.hasKey
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.hasItem

/**
 * Created by Guoshun on 14-1-12.
 * Reference: http://docs.seleniumhq.org/docs/03_webdriver.jsp#internet-explorer-driver
 */

//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations = ["/spring.xml"])
//@Transactional //Important, or the transaction control will be invalid
//@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)

class TestImportDictionary {
    private static WebElement testApp
    private static Logger log = LoggerFactory.getLogger(TestImportDictionary)


    public static final String TARGET_URL = "http://localhost:8888/dms"
//    public static final String TARGET_URL = "http://127.0.0.1:8888/dms"

    @BeforeClass
    static void beforeClass() {
        // Create a new instance of the Firefox driver
        // Notice that the remainder of the code relies on the interface,
        // not the implementation.
//        driver = new ChromeDriver()
//        driver = new InternetExplorerDriver()
        login TARGET_URL, "admin", "alcatel123"
    }

    @AfterClass
    static void afterClass() {
        WebPageUtil.driver.quit()
        WebPageUtil.driver = null
        testApp = null
    }

    private static clickTestApp() {
        if (null == testApp) {
            testApp = getTestApp()
        }
        testApp.click()
    }

    /**
     * Deliver dictionaries test
     * */
    @Test
    void testDeliverMultipleDictionaries() {
        getWebElement(By.id("naviappmngTab")).click()
        clickTestApp()
        MILLISECONDS.sleep(500)

        //Upload multiple file test case(default parameter)
        deliverDictionaries()
        getWebElement(By.id("dictionaryGridList"), 60 * 3)
        List dictionaryNames = getDictionariesInDictGrid().collect { dictionary -> return dictionary.name }
//                Expect dictionaries are imported without error.
        Assert.assertThat(dictionaryNames, allOf(
                hasItem("cmsadministrator_labels_GAE.xml"),
                hasItem("cmsbarringprofile_labels_GAE.xml"),
                hasItem("cmscallserver_exceptions_GAE.xml"),
                hasItem("cmscallserver_labels_GAE.xml"),
                hasItem("cmsldapserver_labels_GAE.xml"),
                hasItem("cmsuser_labels_GAE.xml"),
        ))

    }

    @Test
    void testDeliveredSingleDictionary() {
        //Upload single file test case
        // create glossary before import dictionary
        String glossaryVoIP = 'VoIP'
        createGlossary(glossaryVoIP)

        //switch back to application management panel
        getWebElement(By.id("naviappmngTab")).click()
        SECONDS.sleep(2)
        clickTestApp()
        MICROSECONDS.sleep(500)

        deliverDictionaries(new File("dct_test_files/sampleFiles", "dms-test.xlsx"))

        List dictionaries = dictionariesInDictGrid
        List dictionaryNames = dictionaries.collect { dictionary -> return dictionary.name }
        String expectedDictName = 'dms-test.xlsx'
        // assert import success
        assertThat(dictionaryNames, hasItem(expectedDictName))
//         1. Dictionary "dms-test" contains 8 labels and 6 languages.
        int titleLength = 150
        int index = 1

        println "${index++}. Dictionary ${expectedDictName} contains 8 labels and 6 languages".padRight(titleLength, '=')
        List<Map> dmsTestRow = filter(dictionaries, { dict -> expectedDictName == dict['name'] } as Predicate<Map>) as List


        int expectedNum = 8
        assertEquals("Dictionary ${expectedDictName} expected ${expectedNum} labels.", expectedNum, Integer.parseInt(dmsTestRow[0]["labelNum"]))
        expectedNum = 7
        assertEquals("Dictionary ${expectedDictName} expected ${expectedNum} languages.", expectedNum, getDictionaryLanguageCount(expectedDictName))
        SECONDS.sleep(2)
        Map labels = getLabelDataInDict expectedDictName
//        log.info("Labels info=>{}", labels)

//      2. Max length and Description of label "DMSTEST1" are correctly saved.
        println "${index++}. Max length and Description of label \"DMSTEST1\" are correctly saved in dictionary \"${expectedDictName}\"".padRight(titleLength, '=')
        String testLabelKey = "DMSTEST1"
        String expectedMaxLength = "50"
        String expectDescription = "First label"

        assertEquals("Max length of Label ${testLabelKey} in dictionary ${expectedDictName} expected ${expectedMaxLength}.",
                expectedMaxLength, labels[testLabelKey]['maxLength'])
        assertEquals("Description of Label ${testLabelKey} in dictionary ${expectedDictName} expected \"${expectDescription}\".",
                expectDescription, labels[testLabelKey]['description'])
//      3. DMSTEST2/3/4 have different contexts (DEFAULT/DICT/LABEL) and different Chinese translations.
        println "${index++}. DMSTEST2/3/4 have different contexts (DEFAULT/DICT/LABEL) and different Chinese translations in dictionary \"${expectedDictName}\"".padRight(titleLength, '=')
        assertEquals(labels['DMSTEST2'].context, "[DEFAULT]")
        assertEquals(labels['DMSTEST3'].context, "[DICT]")
        assertEquals(labels['DMSTEST4'].context, "[LABEL]")

        //chinese translation
        assertThat labels['DMSTEST2']['translation']['Chinese (China)'], not(labels['DMSTEST3']['translation']['Chinese (China)'])
        assertThat labels['DMSTEST3']['translation']['Chinese (China)'], not(labels['DMSTEST4']['translation']['Chinese (China)'])
        assertThat labels['DMSTEST4']['translation']['Chinese (China)'], not(labels['DMSTEST2']['translation']['Chinese (China)'])

        println "${index++}. languages are translated for DMSTEST2/3/4: Chinese, Czech, Slovenian and Polish in dictionary \"${expectedDictName}\"".padRight(titleLength, '=')
        (2..4).each {
            assertThat labels["DMSTEST${it}"]['translation'], allOf(hasKey("Chinese (China)"), hasKey("Czech"), hasKey("Slovenian"), hasKey("Polish"))
        }

        println "${index++}. Glossary \"voip\" in DMSTEST5 is replaced by \"VoIP\" for both reference and Chinese and Polish translations".padRight(titleLength, '=')

        assertTrue labels.DMSTEST5.reference.contains(glossaryVoIP)
        assertTrue labels.DMSTEST5.translation['Chinese (China)'].contains(glossaryVoIP)
        assertTrue labels.DMSTEST5.translation.Polish.contains(glossaryVoIP)

        println "${index++}. DMSTEST6 is auto translated for Chinese, Czech and Polish".padRight(titleLength, '=')
        assertThat labels.DMSTEST6.translation, allOf(hasKey("Chinese (China)"), hasKey("Czech"), hasKey("Polish"))

        println "${index}. Total number of translation history where operationType=7 is 6".padRight(titleLength, '=')
        // collect translation history where operationType=7
        List histories = getHistoriesFromLabels(labels, ['operationType': 'SUGGEST'])
        log.info("Histories ={}", histories)
    }

//    @Test
    void testTemp() {

    }
}
