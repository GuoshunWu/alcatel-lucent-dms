package com.alcatel_lucent.dms.webpage

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait

/**
 * Created by Guoshun on 14-1-12.
 * Reference: http://docs.seleniumhq.org/docs/03_webdriver.jsp#internet-explorer-driver
 */
class TestExample {
    private WebDriver driver
    private JavascriptExecutor jsExecutor

    private String userName
    private String password

    private String testProdName
    private String testAppName

    private String testFilePath

    @Before
    void setUp() {
        // Create a new instance of the Firefox driver
        // Notice that the remainder of the code relies on the interface,
        // not the implementation.
//        driver = new ChromeDriver()
//        driver = new InternetExplorerDriver()
        driver = new FirefoxDriver()
        jsExecutor = driver

        userName = "admin"
        password = "alcatel123"

        testProdName = "TestProd"
        testAppName = "TestApp"

        testFilePath = "D:\\Documents\\Alcatel_Lucent\\DMS\\Test\\material\\cms-sample.zip"
    }

    @After
    void tearDown() {
        driver.quit()
        jsExecutor = driver = null
        userName = password = null
        testProdName = testAppName = null
    }

    private WebElement loginAsAdmin() {
        driver.get "http://localhost:8888/dms"
        driver.findElement(By.id('idLoginName')).sendKeys(userName)
        WebElement pwdBtn = driver.findElement(By.id('idPassword'))
        pwdBtn.sendKeys(password)
        pwdBtn.submit()

        return new WebDriverWait(driver, 10).
                until(ExpectedConditions.presenceOfElementLocated(By.id("appTree")))
    }

    private WebElement getTestApp() {
        WebElement appTree = loginAsAdmin()
        final WebElement testProd = new WebDriverWait(driver, 10).until(
                ExpectedConditions.presenceOfElementLocated(By.partialLinkText(testProdName)
                )
        )
        //expand the test product
        testProd.findElement(By.xpath("preceding-sibling::ins")).click()
        String xPathOfTestApp = "//div[@id='appTree']/descendant::a[contains(.,'${testProdName}')]/" +
                "following-sibling::ul/descendant::a[contains(.,'${testAppName}')]"
        return new WebDriverWait(driver, 10).until(ExpectedConditions.elementToBeClickable(By.xpath(xPathOfTestApp)))
    }

    @Test
    void TestDeliverDictionaries() {
        WebElement testApp = getTestApp()
        Thread.sleep(500)
        testApp.click()
        WebElement elemUploadDict = new WebDriverWait(driver, 10).until(ExpectedConditions.presenceOfElementLocated(By.id("dctFileUpload")))
        Thread.sleep(500)
        elemUploadDict.sendKeys(testFilePath)

        new WebDriverWait(driver, 60 * 2).until(
                ExpectedConditions.visibilityOfElementLocated(By.id("dictListPreviewDialog"))
        )
        jsExecutor.executeScript("\$('#dictListPreviewDialog').next().find(\"button\").click()")
        new WebDriverWait(driver, 60 * 3).until(
                ExpectedConditions.visibilityOfElementLocated(By.id("importReportDialog"))
        )
    }
}
