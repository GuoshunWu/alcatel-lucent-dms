package com.alcatel_lucent.dms.webpage

import org.intellij.lang.annotations.Language
import org.junit.After
import org.junit.AfterClass
import org.junit.Assert
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

import static org.hamcrest.CoreMatchers.*
import static org.hamcrest.Matchers.hasEntry;
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


/**
 * Created by Guoshun on 14-1-12.
 * Reference: http://docs.seleniumhq.org/docs/03_webdriver.jsp#internet-explorer-driver
 */
class TestImportDictionary {
    private static WebDriver driver
    private static JavascriptExecutor jsExecutor
    private static WebElement testApp

    @BeforeClass
    static void beforeClass() {
        // Create a new instance of the Firefox driver
        // Notice that the remainder of the code relies on the interface,
        // not the implementation.
//        driver = new ChromeDriver()
//        driver = new InternetExplorerDriver()
        driver = new FirefoxDriver()
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
    private static WebElement login(String url = "http://localhost:8888/dms",
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
        List<List<Object>> dictionaryNames = jsExecutor.executeScript("return \$('#dictionaryGridList').getRowData().map(function(item,index,array){return item.name})")
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
        @Language("JavaScript 1.6") String jsCode = "return \$('#dictionaryGridList').getRowData().map(function(item,index,array){return item.name})";
        dictionaryNames = jsExecutor.executeScript(jsCode)
        String expectedDictName = 'dms-test.xlsx'
        // assert import success
        Assert.assertThat(dictionaryNames, hasItem(expectedDictName))
//         1. Dictionary "dms-test" contains 8 labels and 6 languages.
        jsCode = "return \$('#dictionaryGridList').getRowData().filter(function(item,index,array){return item.name=='dms-test.xlsx'})"
        List<Object> dmsTestRow = jsExecutor.executeScript(jsCode)

//        Assert.assertFalse(dmsTestRow.empty)
        int expectedNum = 8
        Assert.assertEquals("Dictionary ${expectedDictName} expected ${expectedNum} labels.", expectedNum, Integer.parseInt(dmsTestRow[0]["labelNum"]))
        expectedNum = 6
        Assert.assertEquals("Dictionary ${expectedDictName} expected ${expectedNum} languages.",expectedNum, getDictionaryLanguageCount("dms-test.xlsx"))

//        2. Max length and Description of label "DMSTEST1" are correctly saved.
    }

    private Object getDictionaryLanguageCount(String dictionaryName, int timeOut = 2000) {
        @Language("JavaScript 1.6") String jsCode = """
            if (arguments.length <2) return null;
            var dictName = arguments[0];
            var timeOut = arguments[1];

            var dictRows =\$('#dictionaryGridList').getRowData().filter(function(item,index,array){return item.name==dictName});
            if(dictRows.length <1) return null;
            var actionStr = dictRows[0].action;
            var idxLanguageAction = actionStr.indexOf('action_Language_');
            var endIdxLanguageAction =  actionStr.indexOf('\\" style', idxLanguageAction);
            var actionLanguageId  = actionStr.substring(idxLanguageAction, endIdxLanguageAction);
            \$('#'+actionLanguageId, '#dictionaryGridList').click()

            var callback = arguments[arguments.length-1];
              // waiting for the languageSettingGrid data to load
            _arguments = arguments
            setTimeout(function(){
                 callback(\$('#languageSettingGrid').getRowData().length);
            }, timeOut)
        """
        // waiting for the languageSettingGrid data to load
        driver.manage().timeouts().setScriptTimeout(30, SECONDS)
        return jsExecutor.executeAsyncScript(jsCode, dictionaryName, timeOut)
    }
}
