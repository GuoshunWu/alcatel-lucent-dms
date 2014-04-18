package com.alcatel_lucent.dms.webpage

import org.apache.commons.collections.CollectionUtils
import org.apache.commons.collections.PredicateUtils
import org.apache.commons.io.IOUtils
import org.hamcrest.CoreMatchers
import org.intellij.lang.annotations.Language
import org.junit.AfterClass
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test
import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.interactions.Actions
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.Select
import org.openqa.selenium.support.ui.WebDriverWait
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import static com.alcatel_lucent.dms.util.CoffeeScript.compile
import static java.util.concurrent.TimeUnit.*
import static org.hamcrest.CoreMatchers.allOf
import static org.hamcrest.CoreMatchers.not
import static org.hamcrest.Matchers.hasKey
import static org.junit.Assert.*
import static org.junit.Assert.assertTrue
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
    private static WebDriver driver
    private static JavascriptExecutor jsExecutor
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
        driver = new FirefoxDriver()

        driver.manage().timeouts().setScriptTimeout(30, SECONDS)

        jsExecutor = driver

        login()
    }

    @AfterClass
    static void afterClass() {
        driver.quit()
        jsExecutor = driver = null
        testApp = null
    }

    /**
     * Login target test system
     * @param url the target test system url
     * @param userName userName to login
     * @param password password to login
     *
     * @return Application tree of the web element
     * */
    private static WebElement login(String url = TARGET_URL,
                                    String userName = "admin", String password = "alcatel123") {
        driver.get url
        driver.findElement(By.id('idLoginName')).sendKeys(userName)
        WebElement pwdBtn = driver.findElement(By.id('idPassword'))
        pwdBtn.sendKeys(password)
        pwdBtn.submit()

        return new WebDriverWait(driver, 10).
                until(ExpectedConditions.presenceOfElementLocated(By.id("appTree")))
    }

    /**
     * Open the test application at target test System, test product and application must be created
     * before the test start.
     * @param testProductName test product name
     * @param testApplicationName test application name
     * */
    private static WebElement getTestApp(String productName = "DMSTestCases", String appName = "DMSTestApp") {
        // test if test product and application exists, if not create them
        String productVersion = "1.0"
        String appVersion = "1.0"
        //if product not found create new product
        WebElement productElement = getWebElementByJQuerySelector("li[type='product']:contains('${productName}')")
        Actions actions = new Actions(driver)

        boolean createProductVersion = true
        if (null == productElement) {
            // right click products
            actions.contextClick(getWebElement(By.partialLinkText("Products"))).perform()

            getWebElement(By.partialLinkText("New product")).click()
            driver.switchTo().activeElement().sendKeys("${productName}\n")

            getWebElement(By.partialLinkText(productName)).click()
        } else {
            getWebElement(By.partialLinkText(productName)).click() //select product
            MICROSECONDS.sleep(500)
            Select select = new Select(getWebElement(By.id("selVersion")))
            select.selectByVisibleText(productVersion)
            String verText = select.getFirstSelectedOption().text
            createProductVersion = verText == null || !verText.equals(productVersion)
        }

        if (createProductVersion) {
            // create version for product
            getWebElement(By.id("newVersion")).click()

            getWebElement(By.id("versionName")).sendKeys(productVersion)
            WebElement okButton = getWebElementByJQuerySelector("#newProductReleaseDialog + div button:contains('OK')")
            assertNotNull(okButton)
            okButton.click()
        }
        //create new application for Product

        boolean createAppVersion = true
        WebElement testApp = getWebElementByJQuerySelector("li[type='product']:contains('${productName}') a:contains('${appName}')")
        if (null == testApp) {
            actions.contextClick(getWebElement(By.partialLinkText(productName))).perform()
            getWebElement(By.partialLinkText("New application")).click()
            driver.switchTo().activeElement().sendKeys("${appName}\n")
            getWebElement(By.partialLinkText(appName)).click()
        } else {
            MICROSECONDS.sleep(500)
            //expand product element
            final WebElement testProd = getWebElement(By.partialLinkText(productName)).findElement(By.xpath("preceding-sibling::ins")).click()
            getWebElement(By.partialLinkText(appName)).click()

            Select select = new Select(getWebElement(By.id("selAppVersion")))
            select.selectByVisibleText(appVersion)
            String verText = select.getFirstSelectedOption().text

            createAppVersion = null == verText || !verText.equals(appVersion)
        }

        if (createAppVersion) {
            // create version for product
            getWebElement(By.id("newAppVersion")).click()
            getWebElement(By.id("appVersionName")).sendKeys(appVersion)
            WebElement okForAppVersion = getWebElementByJQuerySelector("#newApplicationVersionDialog + div button:contains('OK')")
            assertNotNull(okForAppVersion)
            okForAppVersion.click()
            //wait for addNewApplicationVersionToProductVersionDialog to show
            getWebElement(By.id("addNewApplicationVersionToProductVersionDialog"))
            getWebElementByJQuerySelector("#addNewApplicationVersionToProductVersionDialog + div button:contains('OK')").click()
        }

        String xPathOfTestApp = "//div[@id='appTree']/descendant::a[contains(.,'${productName}')]/" +
                "following-sibling::ul/descendant::a[contains(.,'${appName}')]"

        return new WebDriverWait(driver, 10).until(ExpectedConditions.elementToBeClickable(By.xpath(xPathOfTestApp)))
    }

    /**
     *  Deliver file to DMS
     *  Precondition: login DMS and open test application in test product util the upload button presence
     *
     * @param file the file to deliver
     * @param uploadButton the web upload button
     * */
    private void deliverDictionaries(File file = new File("dct_test_files/sampleFiles", "cms-sample.zip"), int previewTimeOut = 60 * 2) {
        if (null == testApp) testApp = getTestApp()
        testApp.click()
        WebElement uploadButton = new WebDriverWait(driver, 10).until(ExpectedConditions.presenceOfElementLocated(By.id("dctFileUpload")))
        // sleep 1 second to wait for upload button loaded
        SECONDS.sleep(1)
        uploadButton.sendKeys(file.absolutePath)
        getWebElement(By.id("dictListPreviewDialog"), previewTimeOut)
        new WebDriverWait(driver, previewTimeOut).until(
                ExpectedConditions.visibilityOfElementLocated(By.id("dictListPreviewDialog"))
        )
        jsExecutor.executeScript("\$('#dictListPreviewDialog').next().find(\"button\").click()")
        new WebDriverWait(driver, 60 * 30).until(
                ExpectedConditions.visibilityOfElementLocated(By.id("importReportDialog"))
        )
        SECONDS.sleep(2)
        jsExecutor.executeScript("\$('#importReportDialog').dialog(\"close\")")
    }

    /**
     * Deliver dictionaries test
     * */
//    @Test
    void testDeliverMultipleDictionaries() {
        getWebElement(By.id("naviappmngTab")).click()
        MILLISECONDS.sleep(500)
        //Upload multiple file test case(default parameter)
        deliverDictionaries()
        getWebElement(By.id("dictionaryGridList"), 60 * 3)
        @Language("JavaScript 1.6") String jsCode = "return \$('#dictionaryGridList').getRowData().map(function(item,index,array){return item.name})";
        List<List<Object>> dictionaryNames = jsExecutor.executeScript(jsCode)
//        Expect dictionaries are imported without error.
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
        MILLISECONDS.sleep(500)

        deliverDictionaries(new File("dct_test_files/sampleFiles", "dms-test.xlsx"))

        //        @Language("CoffeeScript") String coffeeCode = "return (row.name for row in \$('#dictionaryGridList').getRowData())"
        String jsCode = "return \$('#dictionaryGridList').getRowData().map(function(item,index,array){return item.name})";
        List<List<Object>> dictionaryNames = jsExecutor.executeScript(jsCode)
        String expectedDictName = 'dms-test.xlsx'
        // assert import success
        Assert.assertThat(dictionaryNames, hasItem(expectedDictName))
//         1. Dictionary "dms-test" contains 8 labels and 6 languages.
        int titleLength = 150
        int index = 1

        println "${index++}. Dictionary ${expectedDictName} contains 8 labels and 6 languages".padRight(titleLength, '=')
        jsCode = "return \$('#dictionaryGridList').getRowData().filter(function(item,index,array){return item.name=='dms-test.xlsx'})"
        List<Object> dmsTestRow = jsExecutor.executeScript(jsCode)

        int expectedNum = 8
        assertEquals("Dictionary ${expectedDictName} expected ${expectedNum} labels.", expectedNum, Integer.parseInt(dmsTestRow[0]["labelNum"]))
        expectedNum = 7
        assertEquals("Dictionary ${expectedDictName} expected ${expectedNum} languages.", expectedNum, getDictionaryLanguageCount(expectedDictName))

//      2. Max length and Description of label "DMSTEST1" are correctly saved.
        println "${index++}. Max length and Description of label \"DMSTEST1\" are correctly saved in dictionary \"${expectedDictName}\"".padRight(titleLength, '=')
        String testLabelKey = "DMSTEST1"
        String expectedMaxLength = "50"
        String expectDescription = "First label"

        jsCode = getAsyncJSCode("getLabelInDict")

        Map<String, String> labelData = jsExecutor.executeAsyncScript(jsCode, expectedDictName, testLabelKey, 2000)
        assertEquals("Max length of Label ${testLabelKey} in dictionary ${expectedDictName} expected ${expectedMaxLength}.",
                expectedMaxLength, labelData['maxLength'])
        assertEquals("Description of Label ${testLabelKey} in dictionary ${expectedDictName} expected \"${expectDescription}\".",
                expectDescription, labelData['description'])
//      3. DMSTEST2/3/4 have different contexts (DEFAULT/DICT/LABEL) and different Chinese translations.
        println "${index++}. DMSTEST2/3/4 have different contexts (DEFAULT/DICT/LABEL) and different Chinese translations in dictionary \"${expectedDictName}\"".padRight(titleLength, '=')
        Map labels = getLabelDataInDict expectedDictName
        assertEquals(labels['DMSTEST2'].context, "[DEFAULT]")
        assertEquals(labels['DMSTEST3'].context, "[DICT]")
        assertEquals(labels['DMSTEST4'].context, "[LABEL]")

        //chinese translation
        assertThat labels['DMSTEST2']['translation']['Chinese (China)'], not(labels['DMSTEST3']['translation']['Chinese (China)'])
        assertThat labels['DMSTEST3']['translation']['Chinese (China)'], not(labels['DMSTEST4']['translation']['Chinese (China)'])
        assertThat labels['DMSTEST4']['translation']['Chinese (China)'], not(labels['DMSTEST2']['translation']['Chinese (China)'])

//        log.info("Labels info=>{}",  labels)
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

    }

    private static void createGlossary(String glossary, autoApply = true) {
        getWebElement(By.id("naviadminTab")).click()
        new WebDriverWait(driver, 1).until(ExpectedConditions.elementToBeClickable(By.cssSelector("div#adminTabs li[aria-controls='glossary'] > a"))).click()

        //check if glossary already exists
        String glossaryGridId = "glossaryGrid"
        String selector = "#${glossaryGridId} tr:not(.jqgfirstrow) td[aria-describedby='glossaryGrid_text']"
        List<WebElement> glossaryElements = getWebElementsByJQuerySelector(selector)
        boolean glossaryExists =glossaryElements.collect({it.text}).contains(glossary)
        if (glossaryExists) {
            log.info("Glossary ${glossary} exists, skip creation.")
            return
        }
        new WebDriverWait(driver, 1).until(ExpectedConditions.elementToBeClickable(By.id("custom_add_glossaryGrid"))).click()

        getWebElement(By.id("glossaryText")).sendKeys(glossary)
        getWebElement(By.id("glossaryDescription")).sendKeys("Test glossary for DMS test cases.")
        getWebElementByJQuerySelector("#createGlossaryDialog + div button:contains('OK')").click()

        if(autoApply){
            //apply glossary
            getWebElement(By.id("custom_apply_glossaryGrid")).click()
            //until dialog
            getWebElement(By.id("msgBoxHiddenDiv"), 30)
            getWebElementByJQuerySelector("#msgBoxHiddenDiv ~ div.ui-dialog-buttonpane button:contains('OK')").click()
        }
    }


    private Map<String, Object> getLabelDataInDict(String dictName) {
        //Get Dictionary action buttons
        List<WebElement> actions = getWebElementsByJQuerySelector("#dictionaryGridList tr td[title='${dictName}'] ~ td[aria-describedby='dictionaryGridList_action'] > a")
        actions[0].click()
        //collect Label Data
        Map<String, Object> labels = [:]
//        String selector = "#stringSettingsGrid tr:has(td[title='DMSTEST2']), #stringSettingsGrid tr:has(td[title='DMSTEST3']), #stringSettingsGrid tr:has(td[title='DMSTEST4'])"
        String stringSettingsGridId = "stringSettingsGrid"
        String stringSettingsTranslationGridId = "stringSettingsTranslationGrid"

        String selector = "#${stringSettingsGridId} tr:not(.jqgfirstrow)"
        String transSelector = "#${stringSettingsTranslationGridId} tr:not(.jqgfirstrow)"

        List<WebElement> rows = getWebElementsByJQuerySelector(selector)
        for (WebElement row : rows) {
            WebElement contentCell = row.findElement(By.cssSelector("td[aria-describedby='${stringSettingsGridId}_context']"))
            WebElement keyCell = row.findElement(By.cssSelector("td[aria-describedby='${stringSettingsGridId}_key']"))
            WebElement refCell = row.findElement(By.cssSelector("td[aria-describedby='${stringSettingsGridId}_reference']"))


            Map<String, Object> label = labels.get(keyCell.text)
            if (null == label) {
                label = [:]
                labels[keyCell.text] = label
            }
            label['context'] = contentCell.text
            label['reference'] = refCell.text

//          populate label translation
            List<WebElement> transActs = row.findElements(By.cssSelector("td[aria-describedby='${stringSettingsGridId}_t'] > a"))
            if (transActs.size() > 0) {
                new WebDriverWait(driver, 30).until(ExpectedConditions.elementToBeClickable(transActs[0]))
                transActs[0].click()
                List<WebElement> transRows = getWebElementsByJQuerySelector(transSelector)
                label['translation'] = [:]
                for (WebElement transRow : transRows) {
                    Map translation = [:]
                    WebElement transCode = transRow.findElement(By.cssSelector("td[aria-describedby='${stringSettingsTranslationGridId}_code']"))
                    WebElement translationElem = transRow.findElement(By.cssSelector("td[aria-describedby='${stringSettingsTranslationGridId}_ct.translation']"))
//                    WebElement languageElem = row.findElement(By.cssSelector("td[aria-describedby='${stringSettingsTranslationGridId}_language']"))
                    label['translation'][transCode.text] = translationElem.text
                }
                WebElement transDialogClose = getWebElementByJQuerySelector("#stringSettingsTranslationDialog + div button:contains('Close')")
                if (null != transDialogClose) transDialogClose.click()
            }
        }

        //close dialog
        getWebElementByJQuerySelector("#stringSettingsDialog + div button:contains('Close')").click()
        return labels
    }

//    @Test
    void testTemp() {
//        String jsCode = getAsyncJSCode("dictionaryLanguageCount")
//        Object object = jsExecutor.executeAsyncScript(jsCode, dictionaryName, timeOut)
//        String expectedDictName = 'dms-test.xlsx'
//        println getLabelDataInDict(expectedDictName)
        createGlossary("VoIP")
    }

    private static WebElement getWebElement(By by, int timeOut = 10, visible = true) {
        if (visible) {
            return new WebDriverWait(driver, timeOut).until(
                    ExpectedConditions.visibilityOfElementLocated(by)
            )
        }

        return new WebDriverWait(driver, timeOut).until(
                ExpectedConditions.presenceOfElementLocated(by)
        )
    }

    private static WebElement getWebElementByJQuerySelector(String selector) {
        List<WebElement> webElements = getWebElementsByJQuerySelector(selector)
        if (webElements.size() > 0) return webElements.get(0)
        return null
    }

    private static List<WebElement> getWebElementsByJQuerySelector(String selector) {
        @Language("JavaScript 1.6") String jsCode = """
          var elements = \$("${selector}");
          if(elements.length)return elements.get();
          return [];
       """
        return jsExecutor.executeScript(jsCode)
    }


    private Object getDictionaryLanguageCount(String dictionaryName, int timeOut = 2000) {
        String jsCode = getAsyncJSCode("dictionaryLanguageCount")
//        log.debug("jsCode={}", jsCode)
        // waiting for the languageSettingGrid data to load
        return jsExecutor.executeAsyncScript(jsCode, dictionaryName, timeOut)
    }

/**
 *  Load the external coffee script files and compile the merged files to javascript
 * @param coffeeFile main coffee script file
 * @param path coffee script file directory path
 * @param utilCoffee util coffee script files to merge
 * @return compiled javascript ready for executeAsyncScript to call
 * */
    private String getAsyncJSCode(String coffeeFile, String path = "/com/alcatel_lucent/dms/webpage/js",
                                  List<String> utilCoffee = ["util"]) {
        String cs = IOUtils.toString(getClass().getResourceAsStream("${path}/${coffeeFile}.coffee"))
        String utilPath = '/js/lib'
        String fileSeparator = '\n#' + "=".center(100, '=') + '\n' * 2
        cs = (utilCoffee.collect { utilFile ->
            return IOUtils.toString(getClass().getResourceAsStream("${utilPath}/${utilFile}.coffee"))
        }).join(fileSeparator) + fileSeparator + cs
//        log.info("merged cs = \n{}", cs)
        return compile(cs, true)
    }

}
