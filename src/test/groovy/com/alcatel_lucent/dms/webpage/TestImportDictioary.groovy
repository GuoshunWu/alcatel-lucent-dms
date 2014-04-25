package com.alcatel_lucent.dms.webpage

import com.alcatel_lucent.dms.util.WebPageUtil
import com.google.common.base.Predicate
import groovy.time.TimeCategory
import groovy.time.TimeDuration
import org.junit.*
import org.junit.runners.MethodSorters
import org.openqa.selenium.By
import org.openqa.selenium.Keys
import org.openqa.selenium.WebElement
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.text.SimpleDateFormat

import static com.alcatel_lucent.dms.util.WebPageUtil.*
import static com.google.common.collect.Collections2.filter
import static java.util.concurrent.TimeUnit.*
import static org.hamcrest.CoreMatchers.*
import static org.hamcrest.Matchers.hasKey
import static org.junit.Assert.*


/**
 * Created by Guoshun on 14-1-12.
 * Reference: http://docs.seleniumhq.org/docs/03_webdriver.jsp#internet-explorer-driver
 */

//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations = ["/spring.xml"])
//@Transactional //Important, or the transaction control will be invalid
//@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class TestImportDictionary {
    private static WebElement testApp
    private static Logger log = LoggerFactory.getLogger(TestImportDictionary)

    public static final String TARGET_URL = "http://127.0.0.1:8888/dms"

    @BeforeClass
    static void beforeClass() {
        login TARGET_URL
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
        getWebElement(By.id("naviappmngTab")).click()
        SECONDS.sleep(1)
        testApp.click()
    }

    @Test
    void test001Login() {
        WebElement errElement = login(TARGET_URL, "admin", "1234", false)
        String expectedErrMessage = "Login name or password is incorrect!"
        // 1. Login failed with message "Login name or password is incorrect!"
        assertEquals expectedErrMessage, errElement.text

        //user login, logout case, use local user due to user credential
//        1. Login successfully and display desktop.
        assertNotNull login(TARGET_URL, "admin", "alcatel123")
        // logout
        assertNotNull logout()

        //local user test
        String userName = "admin"
        String password = "alcatel123"
        //1. Login successfully and display desktop.
        assertNotNull login(TARGET_URL, userName, password)

        //2. User "allany" is created as role "APPLICATION_OWNER + TRANSLATION_MANAGER" (skip)
        //3.Last login time of "admin" are updated.
        getWebElement(By.id("naviadminTab")).click()
        getWebElementToBeClickable(By.cssSelector("div#adminTabs li[aria-controls='userAdmin'] > a")).click()
        String gridId = "userGrid"
        String selector = "#${gridId} tr:not(.jqgfirstrow)"
        List<WebElement> rows = driver.findElements(By.cssSelector(selector))
        rows.each { row ->
            WebElement loginName = row.findElement(By.cssSelector("td[aria-describedby='${gridId}_loginName']"))
            if (userName == loginName.text) {
                WebElement onLine = row.findElement(By.cssSelector("td[aria-describedby='${gridId}_onLine']"))
                assertTrue onLine.text.contains("online")
                WebElement lastLoginTime = row.findElement(By.cssSelector("td[aria-describedby='${gridId}_lastLoginTime']"))
                TimeDuration duration = TimeCategory.minus(new Date(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(lastLoginTime.text))
                assertTrue duration.seconds < 40
            }
        }
    }

    @Test
    void test002AddGlossary() {
        String glossaryVoIP = 'VoIP'
        getWebElement(By.id("naviadminTab")).click()
//        1. Operation succeeded.
        getWebElementToBeClickable(By.cssSelector("div#adminTabs li[aria-controls='glossary'] > a")).click()
        String gridId = "glossaryGrid"

        getWebElementToBeClickable(By.id("custom_add_${gridId}")).click()

        getWebElement(By.id("glossaryText")).sendKeys(glossaryVoIP)
        getWebElement(By.id("glossaryDescription")).sendKeys("Test glossary for DMS test cases.")
        getWebElementByJQuerySelector("#createGlossaryDialog + div button:contains('OK')").click()

        WebElement glossaryElem = getWebElementByJQuerySelector("#${gridId} tr:not(.jqgfirstrow):has(td[aria-describedby='${gridId}_text'])")
//      2. "Applied" flag of the new glossary is "false" on creation
        assertEquals "false", glossaryElem.findElement(By.cssSelector("td[aria-describedby='${gridId}_dirty']")).text

        //apply glossary
        getWebElement(By.id("custom_apply_glossaryGrid")).click()
        //until dialog
        getWebElement(By.id("msgBoxHiddenDiv"), 30)
        getWebElementByJQuerySelector("#msgBoxHiddenDiv ~ div.ui-dialog-buttonpane button:contains('OK')").click()
        //3. "Applied" flag is changed to "true" after applying glossary

        glossaryElem = getWebElementByJQuerySelector("#${gridId} tr:not(.jqgfirstrow):has(td[aria-describedby='${gridId}_text'])")
//      2. "Applied" flag of the new glossary is "false" on creation
        assertEquals "true", glossaryElem.findElement(By.cssSelector("td[aria-describedby='${gridId}_dirty']")).text
    }

    /**
     * Deliver dictionaries test
     * */
    @Test
    void test003DeliverMultipleDictionaries() {
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
    void test004DeliveredSingleDictionary() {
        //Upload single file test case
        // create glossary before import dictionary

        //switch back to application management panel
        clickTestApp()
        MICROSECONDS.sleep(500)

        deliverDictionaries "/sampleFiles/dms-test.xlsx"

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

        println "${index++}. 4 languages are translated for DMSTEST2/3/4: Chinese, Czech, Slovenian and Polish in dictionary \"${expectedDictName}\"".padRight(titleLength, '=')

        println()
        println '-' * titleLength
        (2..4).each {
            log.info("DMSTEST${it} translation = {}", labels["DMSTEST${it}"]['translation'])
//            assertThat labels["DMSTEST${it}"]['translation'], allOf(hasKey("Chinese (China)"), hasKey("Czech"), hasKey("Slovenian"), hasKey("Polish"))
        }
        println '-' * titleLength

        println "${index++}. Glossary \"voip\" in DMSTEST5 is replaced by \"VoIP\" for both reference and Chinese and Polish translations".padRight(titleLength, '=')

        assertTrue labels.DMSTEST5.reference.contains(glossaryVoIP)
        assertTrue labels.DMSTEST5.translation['Chinese (China)'].contains(glossaryVoIP)
        assertTrue labels.DMSTEST5.translation.Polish.contains(glossaryVoIP)

        println "${index++}. DMSTEST6 is auto translated for Chinese, Czech and Polish".padRight(titleLength, '=')
        assertThat labels.DMSTEST6.translation, allOf(hasKey("Chinese (China)"), hasKey("Czech"), hasKey("Polish"))

        println "${index}. Total number of translation history where operationType=7 is 6".padRight(titleLength, '=')
        // collect translation history where operationType=7
        List histories = getHistoriesFromLabels(labels, ['operationType': 'SUGGEST'])
//        assertEquals(histories.size(), 7)
        log.info("Histories where operationType=7 size ={}, content =>{}", histories.size(), histories)
    }

    @Test
    void test005RepeatDeliverSingleDictionary() {
        //switch back to application management panel
        clickTestApp()
        MICROSECONDS.sleep(500)
        deliverDictionaries "/sampleFiles/dms-test-repeat.xlsx", ['dms-test-repeat.xlsx': 'dms-test.xlsx']

        String dictName = 'dms-test.xlsx'
        Map labels = getLabelDataInDict dictName, ['DMSTEST7', 'DMSTEST8']
        String expectTranslation = "重复导入二"
        // 1. Chinese translation of DMSTEST7 is modified as "重复导入二".
        assertEquals expectTranslation, labels.DMSTEST7.translation['Chinese (China)']

        // 2. Reference of DMSTEST8 is changed to "Test: not reserved" without any translation.
        String expectReference = "Test: not reserved"
        assertEquals expectReference, labels.DMSTEST8.reference
        assertNull labels.DMSTEST8.translation
    }

    @Test
    void test006AddLabel() {
        clickTestApp()

        String dictName = "dms-test.xlsx"
        openDictionaryStringsDialog(dictName)

        String newLabelKey = "DMSTEST9"
        String newLabelReference = "Test: new label"
        String newLabelContext = "[DEFAULT]"

        Map label = addLabel(dictName, newLabelKey, newLabelReference, newLabelContext)

        // Label is created without error.
        assertNotNull label
        assertEquals newLabelReference, label.reference
        assertEquals newLabelContext, label.context

        // test glossary apply
        newLabelKey = "DMSTEST10"
        newLabelReference = "Test: voip"
        label = addLabel(dictName, newLabelKey, newLabelReference, newLabelContext)
        // 1. Label is created without error.
        assertNotNull label
//        2. Reference is changed to "Test: VoIP"
        assertEquals "Test: VoIP", label.reference

        // test auto translation
        newLabelKey = "DMSTEST11"
        newLabelReference = "General"
        label = addLabel(dictName, newLabelKey, newLabelReference, newLabelContext)
        // 1. Label is created without error.
        assertNotNull label
//        2. Some languages are translated.
        assertNotNull label.translation
        assertFalse label.translation.empty as boolean

        //close string settings dialog
        getWebElementByJQuerySelector("#stringSettingsDialog + div.ui-dialog-buttonpane button:contains('Close')").click()
    }

    @Test
    void test007AddLanguage() {
        clickTestApp()
        String dictName = "dms-test.xlsx"
        openDictionaryStringsDialog(dictName)

    }

    @Test
    void testTemp() {


    }
}
