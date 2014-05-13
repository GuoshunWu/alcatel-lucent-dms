package com.alcatel_lucent.dms.webpage

import com.alcatel_lucent.dms.util.WebPageUtil
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory

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
    }

    @Test
    void testTemp() {
    }
}
