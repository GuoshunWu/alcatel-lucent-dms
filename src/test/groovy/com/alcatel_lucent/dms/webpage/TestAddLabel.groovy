package com.alcatel_lucent.dms.webpage

import com.alcatel_lucent.dms.util.WebPageUtil
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import org.openqa.selenium.WebElement

import static com.alcatel_lucent.dms.util.WebPageUtil.login

/**
 * Created by guoshunw on 2014/4/21.
 */
class TestAddLabel {

    private static WebElement testApp

    @BeforeClass
    static void beforeClass() {
        login TestImportDictionary.TARGET_URL, "admin", "alcatel123"
        //import single dictionary first

    }

    @AfterClass
    static void afterClass() {
        WebPageUtil.driver.quit()
        WebPageUtil.driver = null
        testApp = null
    }


    @Test
    testNormalAddLabel() {

    }

    @Test
    void testTemp() {
    }
}
