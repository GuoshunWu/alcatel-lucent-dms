package com.alcatel_lucent.dms.webpage

import com.alcatel_lucent.dms.util.WebPageUtil
import com.google.common.base.Predicate
import groovy.time.TimeCategory
import groovy.time.TimeDuration
import org.junit.*
import org.junit.runners.MethodSorters
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.Select
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
class DMSIntegrateTest {
    private static WebElement testApp
    private static Logger log = LoggerFactory.getLogger(DMSIntegrateTest)

    public static final String TARGET_URL = "http://127.0.0.1:8888/dms"

    public static final String APP_NAME = "TestSuite"
    public static final String PROD_NAME = "DMS"


    @BeforeClass
    static void beforeClass() {
//        WebPageUtil.login TARGET_URL
    }

    @AfterClass
    static void afterClass() {
        WebPageUtil.driver.quit()
        testApp = null
    }

    private static clickTestApp() {
        getWebElement(By.id("naviappmngTab")).click()
        if (null == testApp) {
            testApp = getTestApp PROD_NAME, APP_NAME
        }
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
            WebElement loginName = row.findElement(By.cssSelector("td[${TD_COLUMN_FILTER}='${gridId}_loginName']"))
            if (userName == loginName.text) {
                WebElement onLine = row.findElement(By.cssSelector("td[${TD_COLUMN_FILTER}='${gridId}_onLine']"))
                assertTrue onLine.text.contains("online")
                WebElement lastLoginTime = row.findElement(By.cssSelector("td[${TD_COLUMN_FILTER}='${gridId}_lastLoginTime']"))
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
        // wait for grid refreshed
        SECONDS.sleep(1)
        WebElement glossaryElem = getWebElementByJQuerySelector("#${gridId} tr:not(.jqgfirstrow):has(td[${TD_COLUMN_FILTER}='${gridId}_text'])")
//      2. "Applied" flag of the new glossary is "false" on creation
        assertEquals "false", glossaryElem.findElement(By.cssSelector("td[${TD_COLUMN_FILTER}='${gridId}_dirty']")).text

        //apply glossary
        getWebElement(By.id("custom_apply_glossaryGrid")).click()
        //until dialog show
        getWebElement(By.id("msgBoxHiddenDiv"), 30)
        getWebElementByJQuerySelector("#msgBoxHiddenDiv ~ div.ui-dialog-buttonpane button:contains('OK')").click()

        //3. "Applied" flag is changed to "true" after applying glossary
        glossaryElem = getWebElementByJQuerySelector("#${gridId} tr:not(.jqgfirstrow):has(td[${TD_COLUMN_FILTER}='${gridId}_text'])")
//      2. "Applied" flag of the new glossary is "false" on creation
        assertEquals "true", glossaryElem.findElement(By.cssSelector("td[${TD_COLUMN_FILTER}='${gridId}_dirty']")).text
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
        String glossaryVoIP = "VoIP"

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
        SECONDS.sleep 1
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
        clickButtonOnDialog('stringSettingsDialog', 'Close')
    }

    @Test
    void test007AddLanguage() {
        clickTestApp()
        String dictName = "dms-test.xlsx"
        SECONDS.sleep(1)
        String dictId = "dictionaryGridList"
//        open language dialog
        getWebElementByJQuerySelector("#${dictId} td[title='${dictName}'] ~ td[${TD_COLUMN_FILTER}='${dictId}_action'] a:last").click()
        getWebElementToBeClickable(By.id("custom_add_languageSettingGrid")).click()
//        until add language dialog open
        WebElement addLangDialog = getWebElement(By.id("addLanguageDialog"))
        Select select = new Select(addLangDialog.findElement(By.id("languageName")))

        String newLanguage = "French (France)"
        select.selectByVisibleText(newLanguage)
//        wait for charset to load
        MICROSECONDS.sleep(500)
        clickButtonOnDialog('addLanguageDialog', 'Add')
        //until dialog show
        getWebElement(By.id("msgBoxHiddenDiv"), 30)
        clickButtonOnDialog('msgBoxHiddenDiv', 'OK')

        String langSettingGridId = "languageSettingGrid"

        List<Map> languages = getGridRowData(langSettingGridId)
        Map language = languages.find { Map lang -> newLanguage == lang.languageId }
//        1. Language is created without error.
        assertNotNull language

        //close dialog
        clickButtonOnDialog('languageSettingsDialog', 'Close')
        MICROSECONDS.sleep(500)

//        2. Label DMSTEST6 and DMSTEST11 are translated.
        Map labels = getLabelDataInDict(dictName, ['DMSTEST6', 'DMSTEST11'])
        assertNotNull labels.DMSTEST6.translation[language.code]
        assertNotNull labels.DMSTEST11.translation[language.code]
    }

    @Test
    void test008ChangeReference() {
        clickTestApp()
        SECONDS.sleep(1)
        String dictName = "dms-test.xlsx"
        openDictionaryStringsDialog(dictName)

        String gridId = "stringSettingsGrid"
        WebElement lockElem = getWebElementToBeClickable(By.id("custom_lock_${gridId}"))

        if (lockElem.text.contains("Unlock")) {
            lockElem.click()
        }
        String labelKey = "DMSTEST9"
        By refSelector = By.cssSelector("#${gridId} tr:not(.jqgfirstrow) td[${TD_COLUMN_FILTER}='${gridId}_key'][title=${labelKey}] + td")
        WebElement refElement = getWebElementToBeClickable(refSelector)
        refElement.click()
        refElement = driver.switchTo().activeElement()
        String newRef = "Test: changed new label"
        refElement.clear();
        refElement.sendKeys(newRef + "\n")
        MILLISECONDS.sleep(500)
//      1. Reference text is changed without error.
        assertEquals newRef, getWebElement(refSelector).text

        labelKey = "DMSTEST10"
        refSelector = By.cssSelector("#${gridId} tr:not(.jqgfirstrow) td[${TD_COLUMN_FILTER}='${gridId}_key'][title=${labelKey}] + td")
        newRef = "Test: voip in new label"
        refElement = getWebElementToBeClickable(refSelector)
        refElement.click()
        refElement = driver.switchTo().activeElement()
        refElement.clear();
        refElement.sendKeys(newRef + "\n")
        MILLISECONDS.sleep(500)
//        1. Reference text is changed to "Test: VoIP in new label"
        String expectRef = "Test: VoIP in new label"
        assertEquals expectRef, getWebElement(refSelector).text

//        2. Translations are same than the reference.
        Map labels = getLabelDataInDict(dictName, [labelKey], false)
        if (null != labels[labelKey].translation) {
            labels[labelKey].translation.each { String langCode, String trans ->
                if (!langCode.endsWith(HISTORY_SUFFIX)) {
                    assertEquals expectRef, trans
                }
            }
        }

        labelKey = "DMSTEST9"
        labels = getLabelDataInDict(dictName, [labelKey], false)
        int translatedNum = null == labels[labelKey].translation ? 0 :
                (labels[labelKey].translation.findAll { String k, v -> !k.endsWith(HISTORY_SUFFIX) }).size()

        refSelector = By.cssSelector("#${gridId} tr:not(.jqgfirstrow) td[${TD_COLUMN_FILTER}='${gridId}_key'][title=${labelKey}] + td")
        refElement = getWebElementToBeClickable(refSelector)
        refElement.click()
        refElement = driver.switchTo().activeElement()
        newRef = "call record"
        refElement.clear();
        refElement.sendKeys(newRef + "\n")
        MILLISECONDS.sleep(1000)
//        1. Reference text is changed without error.
        assertEquals newRef, getWebElement(refSelector).text

        labels = getLabelDataInDict(dictName, [labelKey])
//        2. 3 languages are translated.
        Map lblTranslations = labels[labelKey].translation.findAll { String k, v -> !k.endsWith(HISTORY_SUFFIX) }
        assertEquals 3, lblTranslations.size() - translatedNum
    }

    @Test
    void test009ChangeContext() {
        clickTestApp()
//        wait for dictionary grid to reload
        SECONDS.sleep(1)
        String dictName = "dms-test.xlsx"
        openDictionaryStringsDialog(dictName)

        String gridId = "stringSettingsGrid"
        WebElement lockElem = getWebElementToBeClickable(By.id("custom_lock_${gridId}"))

        if (lockElem.text.contains("Unlock")) {
            lockElem.click()
        }

        String labelKey = "DMSTEST7"
        By refSelector = By.cssSelector("#${gridId} tr:not(.jqgfirstrow) td[${TD_COLUMN_FILTER}='${gridId}_key'][title=${labelKey}] ~ td[${TD_COLUMN_FILTER}='${gridId}_context']")
        WebElement refElement = getWebElementToBeClickable(refSelector)
        refElement.click()
        refElement = driver.switchTo().activeElement()
        String newCtx = "[DEFAULT]"
        refElement.clear();
        refElement.sendKeys(newCtx + "\n")
        MILLISECONDS.sleep(500)
//        1. Context is changed without error.
        assertEquals newCtx, getWebElement(refSelector).text

//        auto close string settings dialog after obtain label datas
        Map labels = getLabelDataInDict(dictName, [labelKey])
        Map lblTranslations = labels[labelKey].translation.findAll { String k, v -> !k.endsWith(HISTORY_SUFFIX) }
//        2. Chinese translation is changed to "重复导入"
        String langCode = "Chinese (China)"
        String expectedTranslation = "重复导入"

        assertEquals expectedTranslation, lblTranslations[langCode]
    }

    @Test
    void test010Capitalize() {
        clickTestApp()
//        wait for dictionary grid to reload
        SECONDS.sleep(1)
        String dictGridId = "dictionaryGridList"
        String dictName = "dms-test.xlsx"
        String selector = "#${dictGridId} tr:not(.jqgfirstrow):has(td[${TD_COLUMN_FILTER}='${dictGridId}_name'][title='${dictName}']) > td:first > input:checkbox"
        WebElement element = getWebElementByJQuerySelector(selector)
        MICROSECONDS.sleep 100
        if (!element.selected) element.click()
        getWebElementToBeClickable(By.id("dictCapitalize")).click()
        MICROSECONDS.sleep(200)

        getWebElementByJQuerySelector("#dictCapitalizeMenu > li > a:contains('all words in lower case')").click()

        getWebElement(By.id("capitalizationDialog"))
        clickButtonOnDialog("capitalizationDialog", 'OK')

        //until dialog show
        getWebElement(By.id("msgBoxHiddenDiv"), 30)
        clickButtonOnDialog('msgBoxHiddenDiv', 'OK')

        //Waiting for the grid refresh
        SECONDS.sleep(2)

//        1. All strings of reference and translation are changed to lower case except "VoIP" in DMSTEST5 and DMSTEST10
        List exceptLabelKeys = ['DMSTEST5', 'DMSTEST10']

        Map labels = getLabelDataInDict(dictName)
        //collect translations
        exceptLabelKeys.each { labelKey -> labels.remove(labelKey) }

        labels.each { String labelKey, label ->
            assertEquals label.reference.toLowerCase(), label.reference
            if (null != label.translation) {
                Map lblTranslations = label.translation.findAll { String k, v -> !k.endsWith(HISTORY_SUFFIX) }
                lblTranslations.each { langCode, trans ->
                    assertEquals trans, trans.toLowerCase()
                }
            }
        }
    }

    @Test
    void test011UpdateTranslation() {
        //Switch to Translation view
        clickTestApp()
        //        wait for dictionary grid to reload
        SECONDS.sleep(1)
        getWebElement(By.id("navitransmngTab")).click()
        String transGridId = "transGrid"

        String selector = populateGridCellSelector(transGridId, 'application', APP_NAME)
        // waiting for the translation data load
        getWebElement(By.cssSelector(selector), 20)

        String dictName = 'dms-test.xlsx'
        String languageName = 'Chinese (China)'
        String status = 'T'

        String reference = "general"
        String changedToTranslation = "总体"
        String transGridDetailId = "transDetailGridList"

        //Find and open 8 translated Chinese string of dictionary "dms-test.xlsx"
        openTranslationDetailDialog dictName, languageName, status

        //get label histories first
        selector = populateGridCellSelector(transGridDetailId, 'reflang', reference, 'history', null, "img")
        WebElement historyImg = getWebElementToBeClickable(By.cssSelector(selector))
        historyImg.click()

        String historyGridId = "detailViewTranslationHistoryGrid"
        // select page size to 100
        Select select = new Select(getWebElement(By.cssSelector("#${historyGridId}Pager_center select.ui-pg-selbox")))
        select.selectByValue(100 + "")

        selector = populateGridCellSelector(historyGridId, 'operationType', 'INPUT')
//        waiting for histories record load
        MICROSECONDS.sleep(500)
        int currentInputHistorySize = getWebElementsByJQuerySelector(selector).size()
        clickButtonOnDialog('translationHistoryDialogInDetailView', 'Close')

        //Modify translation of "General" from "常规" to "总体"

        selector = populateGridCellSelector(transGridDetailId, 'reflang', reference, 'translation')
        WebElement transCellElement = getWebElementToBeClickable(By.cssSelector(selector))
        transCellElement.click()
        transCellElement = driver.switchTo().activeElement()
        transCellElement.clear()
        transCellElement.sendKeys(changedToTranslation + "\n")
//
//        //wait until apply other dialog open
        String transUpdateDialogId = "transmngTranslationUpdate"
        getWebElement(By.id(transUpdateDialogId))
        //collect used dictionaries and chose yes and close the dialog
        List<String> usedDictionaries = getWebElementsByJQuerySelector("#${transUpdateDialogId} ul > li").collect { WebElement element -> element.text }
        log.info("Used dictionaries: {}", usedDictionaries)
        clickButtonOnDialog(transUpdateDialogId, 'Yes')
        //=================================Check apply to all other labels result ======================================
//        for the grid to reload
        SECONDS.sleep 1
        selector = populateGridCellSelector(transGridDetailId, 'reflang', reference, 'translation')
//        1. Translation is changed to "总体"
        assertEquals changedToTranslation, getWebElement(By.cssSelector(selector)).text
//        2. "Trans. Src" is changed to "Manual"
        selector = populateGridCellSelector(transGridDetailId, 'translation', changedToTranslation, 'transtype')
        assertEquals "Manual", getWebElement(By.cssSelector(selector)).text
//      3. "Last updated" is renewed
        selector = populateGridCellSelector(transGridDetailId, 'translation', changedToTranslation, 'lastUpdate')
        String lastUpdatedString = getWebElement(By.cssSelector(selector)).text
        TimeDuration duration = TimeCategory.minus(new Date(), new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(lastUpdatedString))
        assertTrue duration.minutes < 1
//      4. Click history icon of the translation and find an entry of type "INPUT" added.
        selector = populateGridCellSelector(transGridDetailId, 'reflang', reference, 'history', null, "img")
        historyImg = getWebElementToBeClickable(By.cssSelector(selector))
        historyImg.click()

        select = new Select(getWebElement(By.cssSelector("#${historyGridId}Pager_center select.ui-pg-selbox")))
        select.selectByValue(100 + "")

        //        waiting for histories record load
        MICROSECONDS.sleep(500)
        selector = populateGridCellSelector(historyGridId, 'operationType', 'INPUT')
        assertEquals 1, getWebElementsByJQuerySelector(selector).size() - currentInputHistorySize
        currentInputHistorySize = getWebElementsByJQuerySelector(selector).size()
        clickButtonOnDialog('translationHistoryDialogInDetailView', 'Close')

        String transDetailDialogId = 'translationDetailDialog'

        clickButtonOnDialog(transDetailDialogId, 'Close')

//        5. "General" are also translated to "总体" in other dictionaries
        //get reference language for other dictionaries check


        usedDictionaries.each { String usedDictName ->
            openTranslationDetailDialog usedDictName, languageName, status
            MICROSECONDS.sleep 500
            String translationSelector = populateGridCellSelector(transGridDetailId, 'reflang', reference, 'translation')
            assertEquals changedToTranslation, getWebElement(By.cssSelector(translationSelector)).text
            clickButtonOnDialog(transDetailDialogId, 'Close')
        }

        //-----------------------------------apply to current label only  case-----------------------------------------
        openTranslationDetailDialog dictName, languageName, status

        //get label histories first
        selector = populateGridCellSelector(transGridDetailId, 'reflang', reference, 'history', null, "img")
        historyImg = getWebElementToBeClickable(By.cssSelector(selector))
        historyImg.click()

        // select page size to 100
        select = new Select(getWebElement(By.cssSelector("#${historyGridId}Pager_center select.ui-pg-selbox")))
        select.selectByValue(100 + "")

        selector = populateGridCellSelector(historyGridId, 'operationType', 'INPUT')
//        waiting for histories record load
        MICROSECONDS.sleep(500)
        currentInputHistorySize = getWebElementsByJQuerySelector(selector).size()
        clickButtonOnDialog('translationHistoryDialogInDetailView', 'Close')

        changedToTranslation = "常规"
        selector = populateGridCellSelector(transGridDetailId, 'reflang', reference, 'translation')

        transCellElement = getWebElementToBeClickable(By.cssSelector(selector))
        transCellElement.click()
        transCellElement = driver.switchTo().activeElement()
        transCellElement.clear()
        transCellElement.sendKeys(changedToTranslation + "\n")
//
//        //wait until apply other dialog open
        getWebElement(By.id(transUpdateDialogId))
        //collect used dictionaries and chose yes and close the dialog
        usedDictionaries = getWebElementsByJQuerySelector("#${transUpdateDialogId} ul > li").collect { WebElement element -> element.text }
        log.info("Used dictionaries: {}", usedDictionaries)
        clickButtonOnDialog(transUpdateDialogId, 'No')

        //===================================Check apply to current label only result =======================================
//        wait for grid reload
        SECONDS.sleep 1
//        1. Translation is changed to "常规"
        selector = populateGridCellSelector(transGridDetailId, 'reflang', reference, 'translation')
        WebElement referenceElem = getWebElement(By.cssSelector(selector))
        assertEquals changedToTranslation, referenceElem.text
//        2. Context of the label is changed to "[LABEL]"
        WebElement ctxElem = referenceElem.findElement(By.xpath("preceding-sibling::td[@aria-describedby='${transGridDetailId}_context']"))
        assertEquals "[LABEL]", ctxElem.text
//      3. "Trans. Src" is changed to "Manual"
        selector = populateGridCellSelector(transGridDetailId, 'translation', changedToTranslation, 'transtype')
        assertEquals "Manual", getWebElement(By.cssSelector(selector)).text
//        4. "Last updated" is renewed
        selector = populateGridCellSelector(transGridDetailId, 'translation', changedToTranslation, 'lastUpdate')
        lastUpdatedString = getWebElement(By.cssSelector(selector)).text
        duration = TimeCategory.minus(new Date(), new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(lastUpdatedString))
        assertTrue duration.minutes < 1

//      5. Click history icon of the translation and find an entry of type "INPUT" added.
        selector = populateGridCellSelector(transGridDetailId, 'reflang', reference, 'history', null, "img")
        historyImg = getWebElementToBeClickable(By.cssSelector(selector))
        historyImg.click()

        select = new Select(getWebElement(By.cssSelector("#${historyGridId}Pager_center select.ui-pg-selbox")))
        select.selectByValue(100 + "")

        //        waiting for histories record load
        MICROSECONDS.sleep(500)
        selector = populateGridCellSelector(historyGridId, 'operationType', 'INPUT')
//        because the new created label, here must be 1
        assertEquals 1, getWebElementsByJQuerySelector(selector).size()
        clickButtonOnDialog('translationHistoryDialogInDetailView', 'Close')

        clickButtonOnDialog(transDetailDialogId, 'Close')
//      6. "General" are still translated as "总体" in other dictionaries
        usedDictionaries.each { String usedDictName ->
            openTranslationDetailDialog usedDictName, languageName, status
            MICROSECONDS.sleep 500
            String translationSelector = populateGridCellSelector(transGridDetailId, 'reflang', reference, 'translation')
            assertEquals '总体', getWebElement(By.cssSelector(translationSelector)).text
            clickButtonOnDialog(transDetailDialogId, 'Close')
        }
    }

    @Test
    void test012UpdateStatus() {
//        clickTestApp()
//        //        wait for dictionary grid to reload
//        SECONDS.sleep(1)
        getWebElement(By.id("navitransmngTab")).click()

        String transDetailDialogId = 'translationDetailDialog'
        String dictName = 'dms-test.xlsx'
        String languageName = 'Chinese (China)'
        String status = 'N'

        String transGridDetailId = "transDetailGridList"

        openTranslationDetailDialog dictName, languageName, status
        SECONDS.sleep 1
//      Modify 3 Chinese strings of status "N" to "T"
        //remember the 3 chinese string reference
        List references = getWebElementsByJQuerySelector(populateGridCellSelector(transGridDetailId, 'reflang')).collect({ WebElement elem -> elem.text })
        log.info("references = {}", references)
        getWebElementToBeClickable(By.cssSelector("#cb_${transGridDetailId}")).click()
        MICROSECONDS.sleep 200
        getWebElementToBeClickable(By.id("makeDetailLabelTranslateStatus")).click()
        MICROSECONDS.sleep 200
        getWebElementByJQuerySelector("#detailTranslationStatus a:contains('Translated')").click()
        clickButtonOnDialog(transDetailDialogId, 'Close')
        MICROSECONDS.sleep 500
//      1. Translation status are changed without error.
        String selector = populateGridCellSelector('transGrid', 'dictionary', dictName, "${languageName}.N")
        Assert.assertEquals "", getWebElement(By.cssSelector(selector)).text.trim()

//      2. Click history icon of the translation and find an entry of type "STATUS" added
        openTranslationDetailDialog dictName, languageName, 'T'
        references.each { String reference ->
            List histories = getHistoriesInTranslationDetail(reference).collect { Map history -> 'STATUS' == history.operationType }
            assertTrue histories.size() > 0
        }
        clickButtonOnDialog(transDetailDialogId, 'Close')
        String transGridId = "transGrid"
        selector = populateGridCellSelector transGridId, 'dictionary', dictName
        getWebElementToBeClickable(By.cssSelector(selector)).click()
        MICROSECONDS.sleep 200
        getWebElementToBeClickable(By.id("makeLabelTranslateStatus")).click()
        getWebElementByJQuerySelector("#translationStatus a:contains('Not translated')").click()

        // until message dialog show
        getWebElement(By.id("msgBoxHiddenDiv"))
        clickButtonOnDialog('msgBoxHiddenDiv', 'OK')
//      1. All Chinese strings are set to Not Translated

        selector = populateGridCellSelector(transGridId, 'dictionary', dictName, "${languageName}.T")
        Assert.assertEquals "", getWebElement(By.cssSelector(selector)).text.trim()
    }

    @Test
    void test013ExportTranslationSummary() {
//        getWebElementToBeClickable(By.id('exportExcel')).click()
    }

    @Test
    void test014ExportTranslationDetail() {
//        String selector = populateGridCellSelector 'transGrid', 'dictionary', 'dms-test.xlsx'
//        getWebElementToBeClickable(By.cssSelector(selector)).click()


    }

//    @Test
    void testTemp() {
        login TARGET_URL
        test012UpdateStatus()
    }
}
