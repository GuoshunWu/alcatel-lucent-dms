package com.alcatel_lucent.dms.webpage

import org.apache.commons.io.IOUtils
import org.intellij.lang.annotations.Language
import org.junit.After
import org.junit.AfterClass
import org.junit.Assert
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import static org.hamcrest.CoreMatchers.*
import static org.hamcrest.Matchers.hasEntry
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertEquals;
import static org.junit.matchers.JUnitMatchers.*;

import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait

import static java.util.concurrent.TimeUnit.MILLISECONDS
import static java.util.concurrent.TimeUnit.SECONDS
import static com.alcatel_lucent.dms.util.CoffeeScript.compile


/**
 * Created by Guoshun on 14-1-12.
 * Reference: http://docs.seleniumhq.org/docs/03_webdriver.jsp#internet-explorer-driver
 */
class TestImportDictionary {
    private static WebDriver driver
    private static JavascriptExecutor jsExecutor
    private static WebElement testApp
    private static Logger log = LoggerFactory.getLogger(TestImportDictionary)

    public static final String TARGET_URL = "http://localhost:8888/dms"

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
        testApp = getTestApp()
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
    private static WebElement getTestApp(String testProductName = "TestProd", String testApplicationName = "TestApp") {
        WebElement appTree = login()
        final WebElement testProd = new WebDriverWait(driver, 10).until(
                ExpectedConditions.presenceOfElementLocated(By.partialLinkText(testProductName)
                )
        )
        //expand the test product
        testProd.findElement(By.xpath("preceding-sibling::ins")).click()
        String xPathOfTestApp = "//div[@id='appTree']/descendant::a[contains(.,'${testProductName}')]/" +
                "following-sibling::ul/descendant::a[contains(.,'${testApplicationName}')]"
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
        WebElement uploadButton = new WebDriverWait(driver, 10).until(ExpectedConditions.presenceOfElementLocated(By.id("dctFileUpload")))
        // sleep 1 second to wait for upload button loaded
        SECONDS.sleep(1)
        uploadButton.sendKeys(file.absolutePath)
        new WebDriverWait(driver, previewTimeOut).until(
                ExpectedConditions.visibilityOfElementLocated(By.id("dictListPreviewDialog"))
        )
        jsExecutor.executeScript("\$('#dictListPreviewDialog').next().find(\"button\").click()")
        new WebDriverWait(driver, 60 * 3).until(
                ExpectedConditions.visibilityOfElementLocated(By.id("importReportDialog"))
        )
        SECONDS.sleep(2)
        jsExecutor.executeScript("\$('#importReportDialog').dialog(\"close\")")
    }

    /**
     * Deliver dictionaries test
     * */
    @Test
    void testDeliverDictionaries() {
        MILLISECONDS.sleep(500)
        testApp.click()
        //Upload multiple file test case(default parameter)
//        deliverDictionaries()

        new WebDriverWait(driver, 60 * 3).until(
                ExpectedConditions.visibilityOfElementLocated(By.id("dictionaryGridList"))
        )
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

        //Upload single file test case
//        deliverDictionaries(new File("dct_test_files/sampleFiles", "dms-test.xlsx"))

//        @Language("CoffeeScript") String coffeeCode = "return (row.name for row in \$('#dictionaryGridList').getRowData())"
        jsCode = "return \$('#dictionaryGridList').getRowData().map(function(item,index,array){return item.name})";
        dictionaryNames = jsExecutor.executeScript(jsCode)
        String expectedDictName = 'dms-test.xlsx'
        // assert import success
        Assert.assertThat(dictionaryNames, hasItem(expectedDictName))
//         1. Dictionary "dms-test" contains 8 labels and 6 languages.

        jsCode = "return \$('#dictionaryGridList').getRowData().filter(function(item,index,array){return item.name=='dms-test.xlsx'})"
        List<Object> dmsTestRow = jsExecutor.executeScript(jsCode)

        int expectedNum = 8
        assertEquals("Dictionary ${expectedDictName} expected ${expectedNum} labels.", expectedNum, Integer.parseInt(dmsTestRow[0]["labelNum"]))
        expectedNum = 7
        assertEquals("Dictionary ${expectedDictName} expected ${expectedNum} languages.", expectedNum, getDictionaryLanguageCount(expectedDictName))

//      2. Max length and Description of label "DMSTEST1" are correctly saved.
        String testLabelKey = "DMSTEST1"
        String expectedMaxLength = "50"
        String expectDescription = "First label"

        jsCode = getAsyncJSCode("getLabelInDict")

        Map<String,String> labelData = jsExecutor.executeAsyncScript(jsCode, expectedDictName, testLabelKey, 2000)
        assertEquals("Max length of Label ${testLabelKey} in dictionary ${expectedDictName} expected ${expectedMaxLength}.",
                expectedMaxLength, labelData['maxLength'])
        assertEquals("Description of Label ${testLabelKey} in dictionary ${expectedDictName} expected \"${expectDescription}\".",
                expectDescription, labelData['description'])
//      3. DMSTEST2/3/4 have different contexts (DEFAULT/DICT/LABEL) and different Chinese translations.

    }


//    @Test
    void testJSCompile() {
        String jsCode = getAsyncJSCode("getLabelInDict")
        log.debug("jsCode={}", jsCode)
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
