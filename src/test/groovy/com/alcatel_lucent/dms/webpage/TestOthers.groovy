package com.alcatel_lucent.dms.webpage

import com.alcatel_lucent.dms.util.WebPageUtil
import groovy.time.TimeCategory
import groovy.time.TimeDuration
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.text.SimpleDateFormat

import static com.alcatel_lucent.dms.util.WebPageUtil.*
import static com.alcatel_lucent.dms.webpage.TestImportDictionary.TARGET_URL
import static org.junit.Assert.*

/**
 * Created by guoshunw on 2014/4/21.
 */
class TestOthers {

    private static Logger log = LoggerFactory.getLogger(TestOthers)

    @BeforeClass
    static void beforeClass() {
    }

    @AfterClass
    static void afterClass() {
        WebPageUtil.driver.quit()
        WebPageUtil.driver = null

    }

    @Test
    void testTemp() {
    }
}
