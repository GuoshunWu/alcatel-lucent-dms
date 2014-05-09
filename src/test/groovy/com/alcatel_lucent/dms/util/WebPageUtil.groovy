package com.alcatel_lucent.dms.util

import org.apache.commons.io.IOUtils
import org.intellij.lang.annotations.Language
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
import static java.util.concurrent.TimeUnit.MICROSECONDS
import static java.util.concurrent.TimeUnit.MILLISECONDS
import static java.util.concurrent.TimeUnit.SECONDS
import static org.junit.Assert.assertNotNull

/**
 * Created by Administrator on 2014/4/19 0019.
 */
public class WebPageUtil {

    private static WebDriver driver
    public static final String HISTORY_SUFFIX = "_histories"
    public static final String TD_COLUMN_FILTER = "aria-describedby"

    static WebDriver getDriver() {
        return driver
    }

    private static Logger log = LoggerFactory.getLogger(WebPageUtil)

    static {

        // Create a new instance of the Firefox driver
        // Notice that the remainder of the code relies on the interface,
        // not the implementation.     see: http://chromedriver.storage.googleapis.com/index.html
//        driver = new ChromeDriver()
//        driver = new RemoteWebDriver(new URL("http://localhost:9515"), DesiredCapabilities.chrome())
//        driver = new InternetExplorerDriver()
        driver = new FirefoxDriver()
//        driver = new RemoteWebDriver(new URL("http://localhost:9515"), DesiredCapabilities.chrome())
        driver.manage().timeouts().setScriptTimeout(30, SECONDS)
    }

    public static List getDictionariesInDictGrid() {
        @Language("JavaScript 1.6") String jsCode = "return \$('#dictionaryGridList').getRowData()";
        return (driver as JavascriptExecutor).executeScript(jsCode)
    }

    public static void openTranslationDetailDialog(String dictName, String languageName, String status) {
        String transGridId = "transGrid"
        String selector = "#${transGridId} tr:not(.jqgfirstrow) td[${TD_COLUMN_FILTER}='${transGridId}_dictionary'][title='${dictName}'] ~" +
                " td[${TD_COLUMN_FILTER}='${transGridId}_${languageName}.${status}'] > a"
        getWebElementToBeClickable(By.cssSelector(selector)).click()
    }

    public static String populateGridCellSelector(String gridId, String columnName = null, String title = null,
                                                  String siblingColumnName = null, String siblingTitle = null,
                                                  String childTag = null) {
        String selector = "#${gridId} tr:not(.jqgfirstrow)"
        if (null != columnName) selector += " td[${TD_COLUMN_FILTER}='${gridId}_${columnName}']"
        if (null != title) selector += "[title='${title}']"
        if (null != siblingColumnName) selector += " ~ td[${TD_COLUMN_FILTER}='${gridId}_${siblingColumnName}']"
        if (null != siblingTitle) selector += "[${TD_COLUMN_FILTER}='${siblingTitle}']"
        if (null != childTag) selector += " > ${childTag}"
//        log.info("selector = {}", selector)
        selector
    }

    public static List<Map> getHistoriesInTranslationDetail(String reference, boolean autoCloseDialog = true) {
        String transGridDetailId = "transDetailGridList"

        String historyGridId = "detailViewTranslationHistoryGrid"

        String selector = populateGridCellSelector(transGridDetailId, 'reflang', reference, 'history', null, "img")
        WebElement historyImg = getWebElementToBeClickable(By.cssSelector(selector))
        historyImg.click()

        // select page size to 100
        Select select = new Select(getWebElement(By.cssSelector("#${historyGridId}Pager_center select.ui-pg-selbox")))
        select.selectByValue(100 + "")
        MICROSECONDS.sleep(200)
        List<Map> histories = getGridRowData(historyGridId)
        if (autoCloseDialog) clickButtonOnDialog('translationHistoryDialogInDetailView', 'Close')
        histories
    }

    /**
     *  Deliver file to DMS
     *  Precondition: login DMS and open test application in test product util the upload button presence
     *
     * @param file the file to deliver
     * @param uploadButton the web upload button
     * */
    public
    static void deliverDictionaries(String resourceFilePath = "/sampleFiles/cms-sample.zip", Map<String, String> dictRenameTo = [:], int previewTimeOut = 60 * 2) {
        File file = new File(this.getClass().getResource(resourceFilePath).toURI())
        JavascriptExecutor jsExecutor = driver
//        if (null == testApp) testApp = getTestApp()
//        testApp.click()
        WebElement uploadButton = new WebDriverWait(driver, 10).until(ExpectedConditions.presenceOfElementLocated(By.id("dctFileUpload")))
        // sleep 1 second to wait for upload button loaded
        SECONDS.sleep(1)
        uploadButton.sendKeys(file.absolutePath)
        getWebElement(By.id("dictListPreviewDialog"), previewTimeOut)
        new WebDriverWait(driver, previewTimeOut).until(
                ExpectedConditions.visibilityOfElementLocated(By.id("dictListPreviewDialog"))
        )
        // rename the dictionary name for each rename entry
        String gridId = "dictListPreviewGrid"
        dictRenameTo.each { oldName, newName ->
            String selector = "#${gridId} tr:not(.jqgfirstrow) td[${TD_COLUMN_FILTER}='${gridId}_name']"
            List<WebElement> elements = driver.findElements(By.cssSelector(selector))
            elements.each { element ->
                log.info("Rename dictionary from ${oldName} to ${newName}")
                if (null != dictRenameTo[element.text]) {
                    element.click()
                    WebElement currentElem = driver.switchTo().activeElement()
                    currentElem.clear()
                    currentElem.sendKeys("${newName}\n")
                }
            }
            MICROSECONDS.sleep(100)
        }
        //execute
        getWebElementByJQuerySelector("#dictListPreviewDialog + div button").click()
        new WebDriverWait(driver, 60 * 30).until(
                ExpectedConditions.visibilityOfElementLocated(By.id("importReportDialog"))
        )
        SECONDS.sleep(2)
        jsExecutor.executeScript("\$('#importReportDialog').dialog(\"close\")")
    }

    /**
     * Add a label and return the label info collection from web UI
     * */
    public static Map addLabel(String dictName, String newLabelKey, String newLabelReference, String newLabelContext) {

        String gridId = "stringSettingsGrid"
        WebElement lockElem = getWebElementToBeClickable(By.id("custom_lock_${gridId}"))

        if (lockElem.text.contains("Unlock")) {
            lockElem.click()
        }

        getWebElementToBeClickable(By.id("custom_add_${gridId}")).click()
        String dialogId = "addLabelDialog"

        getWebElement(By.cssSelector("#${dialogId} #key")).sendKeys(newLabelKey)
        getWebElement(By.cssSelector("#${dialogId} #reference")).sendKeys(newLabelReference)
        WebElement contextInput = getWebElement(By.cssSelector("#${dialogId} #context"))
        // contextInput.sendKeys  Keys.chord(Keys.CONTROL, "a", newLabelContext)

        clickButtonOnDialog(dialogId, 'Add & Close');
        return getLabelDataInDict(dictName, [newLabelKey], false)[newLabelKey]
    }

    /**
     * Login target test system
     * @param url the target test system url
     * @param userName userName to login
     * @param password password to login
     * @param validCredential is valid credential
     *
     * @return Application tree of the web element or error message web element if valid credential is false
     * */
    public static WebElement login(String url,
                                   String userName = "admin", String password = "alcatel123", boolean validCredential = true) {
        log.info "userName={}, password={}, validCredential={}", userName, password, validCredential
        driver.get url
        driver.findElement(By.id('idLoginName')).sendKeys(userName)
        WebElement pwdBtn = driver.findElement(By.id('idPassword'))
        pwdBtn.sendKeys(password)
        pwdBtn.submit()
        if (!validCredential) {
            //return login fail WebElement
            return getWebElement(By.cssSelector("div#loginStatus li > span"))
        }
        // close tip of day dialog
        SECONDS.sleep(1)
        WebElement tipOfDayClose = getWebElementByJQuerySelector("#tipOfTheDayDialog + div button:contains('Close')")
        if (null != tipOfDayClose) tipOfDayClose.click()

        return getWebElement(By.id("appTree"))
    }

    public static WebElement logout() {
        getWebElementToBeClickable(By.cssSelector("a[href\$='logout.action']")).click()
        return getWebElement(By.id("loginForm"))
    }

    /**
     * Open the test application at target test System, test product and application must be created
     * before the test start.
     * @param testProductName test product name
     * @param testApplicationName test application name
     * */
    public
    static WebElement getTestApp(String productName = "DMS", String appName = "TestSuite") {
        // test if test product and application exists, if not create them
        String productVersion = "V2"
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
            WebElement testProd = getWebElement(By.partialLinkText(productName)).findElement(By.xpath("preceding-sibling::ins")).click()
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

    public static WebElement getWebElementToBeClickable(By by, int timeOut = 10) {
        return new WebDriverWait(driver, timeOut).until(
                ExpectedConditions.elementToBeClickable(by)
        )
    }

    public static boolean clickButtonOnDialog(String dialogId, String buttonText) {
        WebElement button = getWebElementByJQuerySelector("#${dialogId} ~ div.ui-dialog-buttonpane button:contains('${buttonText}')")
        if (null == button) return false
        button.click()
        return true
    }

    public static WebElement getWebElement(By by, int timeOutInSeconds = 10, visible = true) {
        if (visible) {
            return new WebDriverWait(driver, timeOutInSeconds).until(
                    ExpectedConditions.visibilityOfElementLocated(by)
            )
        }

        return new WebDriverWait(driver, timeOutInSeconds).until(
                ExpectedConditions.presenceOfElementLocated(by)
        )
    }

    public static WebElement getWebElementByJQuerySelector(String selector) {
        List<WebElement> webElements = getWebElementsByJQuerySelector(selector)
        if (webElements.size() > 0) return webElements.get(0)
        return null
    }

    public static List<WebElement> getWebElementsByJQuerySelector(String selector) {
        @Language("JavaScript 1.6") String jsCode = """
          var elements = \$("${selector}");
          if(elements.length)return elements.get();
          return [];
       """
        return (driver as JavascriptExecutor).executeScript(jsCode)
    }

    public static void createGlossary(String glossary, autoApply = true) {
        getWebElement(By.id("naviadminTab")).click()
        getWebElementToBeClickable(By.cssSelector("div#adminTabs li[aria-controls='glossary'] > a")).click()

        //check if glossary already exists
        String glossaryGridId = "glossaryGrid"
        String selector = "#${glossaryGridId} tr:not(.jqgfirstrow) td[${TD_COLUMN_FILTER}='${glossaryGridId}_text']"
        List<WebElement> glossaryElements = getWebElementsByJQuerySelector(selector)
        boolean glossaryExists = glossaryElements.collect({ it.text }).contains(glossary)
        if (glossaryExists) {
            log.info("Glossary ${glossary} exists, skip creation.")
            return
        }
        getWebElementToBeClickable(By.id("custom_add_glossaryGrid")).click()

        getWebElement(By.id("glossaryText")).sendKeys(glossary)
        getWebElement(By.id("glossaryDescription")).sendKeys("Test glossary for DMS test cases.")
        getWebElementByJQuerySelector("#createGlossaryDialog + div button:contains('OK')").click()

        if (autoApply) {
            //apply glossary
            getWebElement(By.id("custom_apply_glossaryGrid")).click()
            //until dialog
            getWebElement(By.id("msgBoxHiddenDiv"), 30)
            getWebElementByJQuerySelector("#msgBoxHiddenDiv ~ div.ui-dialog-buttonpane button:contains('OK')").click()
        }
    }

    /**
     * Collect grid data as a List
     * @param gridId grid id
     * @param filter if it is not empty, only the row match all entries in the filter will be added in the result list
     *
     * @return result list
     * */

    public static List<Map> getGridRowData(String gridId) {
        String selector = "#${gridId} tr:not(.jqgfirstrow)"
        List<Map> result = []
        List<WebElement> rows = getWebElementsByJQuerySelector(selector)
        for (WebElement row : rows) {
            Map mapRow = [:]
            List<WebElement> columns = row.findElements(By.cssSelector("td"))
            for (WebElement column : columns) {
                String text = column.text
                String colName = column.getAttribute(TD_COLUMN_FILTER).substring(gridId.length() + 1)
                if ("cb" == colName) continue
                mapRow[colName] = text
            }
            result.add(mapRow)
        }
        result
    }


    public static Object getDictionaryLanguageCount(String dictionaryName, int timeOut = 2000) {
        String jsCode = getAsyncJSCode("dictionaryLanguageCount")
        return (driver as JavascriptExecutor).executeAsyncScript(jsCode, dictionaryName, timeOut)
    }

    /**
     *  Load the external coffee script files and compile the merged files to javascript
     * @param coffeeFile main coffee script file
     * @param path coffee script file directory path
     * @param utilCoffee util coffee script files to merge
     * @return compiled javascript ready for executeAsyncScript to call
     * */
    public static String getAsyncJSCode(String coffeeFile, String path = "/com/alcatel_lucent/dms/webpage/js",
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

    public static void openDictionaryStringsDialog(String dictName, String dictGridId = "dictionaryGridList") {
        //Get Dictionary action buttons
        WebElement action = new WebDriverWait(driver, 10).until(
                ExpectedConditions.elementToBeClickable(
                        By.cssSelector("#${dictGridId} tr td[title='${dictName}'] ~ td[${TD_COLUMN_FILTER}='${dictGridId}_action'] > a"
                        )
                )
        )
        action.click()
    }

    /**
     * Collect data in dictionary dictName, if labelKeyInclude is not empty, only those
     * label key in the list will be collected.
     *
     * @param dictName dictionary name to collect
     * @param labelKeyInclude collect label key in this list only if this list is not empty
     *
     * @return a JSON style map include labels data
     *    example:
     *{
     *        labelKey1:
     *{
     *            reference: reference
     *            context: context
     *            description: description
     *            translation:
     *{
     *                 langCode1: translation1
     *                 langCode2: translation2
     *}*            translation_histories: [
     *{
     *                  operationTime: operationTime
     *                  translation: translation
     *                  status: status
     *                  memo: memo
     *}*            ]
     *
     *}*        labelKey2:
     *{
     *             ....
     *}*}*                        */
    public
    static Map<String, Object> getLabelDataInDict(String dictName, List labelKeyInclude = [], boolean autoCloseStringDialog = true) {
        openDictionaryStringsDialog(dictName)
        //collect Label Data
        Map<String, Object> labels = [:]
        String stringSettingsGridId = "stringSettingsGrid"
        String stringSettingsTranslationGridId = "stringSettingsTranslationGrid"

        String selector = "#${stringSettingsGridId} tr:not(.jqgfirstrow)"
        String transSelector = "#${stringSettingsTranslationGridId} tr:not(.jqgfirstrow)"
        MILLISECONDS.sleep(500)
        List<WebElement> rows = getWebElementsByJQuerySelector(selector)

        for (WebElement row : rows) {
            WebElement keyCell = row.findElement(By.cssSelector("td[${TD_COLUMN_FILTER}='${stringSettingsGridId}_key']"))
            if (!labelKeyInclude.isEmpty() && !labelKeyInclude.contains(keyCell.text)) continue
            Map<String, Object> label = labels.get(keyCell.text)
            if (null == label) {
                label = [:]
                labels[keyCell.text] = label
            }
            label['context'] = row.findElement(By.cssSelector("td[${TD_COLUMN_FILTER}='${stringSettingsGridId}_context']")).text
            label['reference'] = row.findElement(By.cssSelector("td[${TD_COLUMN_FILTER}='${stringSettingsGridId}_reference']")).text
            label['maxLength'] = row.findElement(By.cssSelector("td[${TD_COLUMN_FILTER}='${stringSettingsGridId}_maxLength']")).text
            label['description'] = row.findElement(By.cssSelector("td[${TD_COLUMN_FILTER}='${stringSettingsGridId}_description']")).text

//          populate label translation
            List<WebElement> transActs = row.findElements(By.cssSelector("td[${TD_COLUMN_FILTER}='${stringSettingsGridId}_t'] > a"))
            if (transActs.size() > 0) {
                new WebDriverWait(driver, 30).until(ExpectedConditions.elementToBeClickable(transActs[0]))
                transActs[0].click()
                List<WebElement> transRows = getWebElementsByJQuerySelector(transSelector)
                label['translation'] = [:]
                transRows.each { transRow ->
                    Map translation = [:]
                    WebElement transCode = transRow.findElement(By.cssSelector("td[${TD_COLUMN_FILTER}='${stringSettingsTranslationGridId}_code']"))
                    WebElement translationElem = transRow.findElement(By.cssSelector("td[${TD_COLUMN_FILTER}='${stringSettingsTranslationGridId}_ct.translation']"))
//                    WebElement languageElem = row.findElement(By.cssSelector("td[${TD_COLUMN_FILTER}='${stringSettingsTranslationGridId}_language']"))
                    label.translation[transCode.text] = translationElem.text

                    //obtain translation history
                    label.translation[transCode.text + HISTORY_SUFFIX] = getTranslationHistories(transRow)
                }
                getWebElement(By.id("stringSettingsTranslationDialog"))
                clickButtonOnDialog('stringSettingsTranslationDialog', 'Close')
            }
        }

        //close dialog
        if (autoCloseStringDialog) clickButtonOnDialog('stringSettingsDialog', 'Close')
        return labels
    }


    private
    static List getTranslationHistories(WebElement transRow, String stringSettingsTranslationGridId = "stringSettingsTranslationGrid") {
        WebElement translationElem = transRow.findElement(By.cssSelector("td[${TD_COLUMN_FILTER}='${stringSettingsTranslationGridId}_history'] > img"))
        translationElem.click()

        String gridId = "stringSettingsTranslationHistoryGrid"
        String historySelector = "#${gridId} tr:not(.jqgfirstrow)"
        List<WebElement> histories = getWebElementsByJQuerySelector(historySelector)
        List result = []
        histories.each { historyRow ->
            Map history = [:]
            WebElement tempElement = historyRow.findElement(By.cssSelector("td[${TD_COLUMN_FILTER}='${gridId}_operationTime']"))
            history.operationTime = tempElement.text
            tempElement = historyRow.findElement(By.cssSelector("td[${TD_COLUMN_FILTER}='${gridId}_operationType']"))
            history.operationType = tempElement.text
            tempElement = historyRow.findElement(By.cssSelector("td[${TD_COLUMN_FILTER}='${gridId}_translation']"))
            history.translation = tempElement.text
            tempElement = historyRow.findElement(By.cssSelector("td[${TD_COLUMN_FILTER}='${gridId}_status']"))
            history.status = tempElement.text
            tempElement = historyRow.findElement(By.cssSelector("td[${TD_COLUMN_FILTER}='${gridId}_memo']"))
            history.memo = tempElement.text

            result.add(history)
        }


        WebElement historyDialogClose = getWebElementByJQuerySelector("#stringSettingsTranslationHistoryDialog + div button:contains('Close')")
        if (null != historyDialogClose) historyDialogClose.click()
        return result
    }

    private static List getHistoriesFromLabels(Map labels, Map filter = [:]) {
        List translationHistories = []
        labels.each { String k, Map v ->
            if (null != v.translation) {
                v.translation.each { String tk, tv ->
                    if (tk.endsWith(HISTORY_SUFFIX)) {
                        if (filter.isEmpty()) {
                            translationHistories.addAll(tv as List)
                        } else {
                            tv.each { Map history ->
                                for (Map.Entry filterItem : filter.entrySet()) {
                                    if (history[filterItem.key] != filterItem.value) {
                                        continue
                                    }
                                    translationHistories.add(history)
                                }
                            }
                        }
                    }
                }
            }
        }
        return translationHistories
    }
}
