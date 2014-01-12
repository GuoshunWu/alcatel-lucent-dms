package com.alcatel_lucent.dms.webpage

import junit.framework.Assert
import org.junit.Test
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.support.ui.ExpectedCondition
import org.openqa.selenium.support.ui.WebDriverWait

/**
 * Created by Administrator on 14-1-12.
 * Reference: http://docs.seleniumhq.org/docs/03_webdriver.jsp#internet-explorer-driver
 */
class TestExample {

    @Test
    void TestDemo() {
        // Create a new instance of the Firefox driver
        // Notice that the remainder of the code relies on the interface,
        // not the implementation.
        WebDriver driver = new FirefoxDriver()
//        driver = new ChromeDriver()
//        driver = new InternetExplorerDriver()

        driver.get("https://www.google.com.hk")
        WebElement element = driver.findElement(By.name('q'))
        element.sendKeys('Cheese!')
        element.submit()
        println("Page title is: ${driver.title}")

        // Google's search is rendered dynamically with JavaScript.
        // Wait for the page to load, timeout after 10 seconds
        // Should see: "cheese! - Google Search"
        new WebDriverWait(driver, 10).until({ WebDriver webDriver ->
            return webDriver.title.toLowerCase().startsWith("cheese!")
        } as ExpectedCondition<Boolean>)

        Assert.assertTrue driver.title.contains("Cheese")

        driver.quit()
    }
}
