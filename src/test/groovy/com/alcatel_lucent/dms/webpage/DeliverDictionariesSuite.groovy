package com.alcatel_lucent.dms.webpage

import junit.framework.Test
import junit.framework.TestSuite
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Created by Administrator on 2014/4/19 0019.
 */

@RunWith(Suite.class)
@Suite.SuiteClasses([TestOthers, TestImportDictionary])
class DMSTestSuite{

    @BeforeClass public static void setUpClass() {
        // Common initialization done once for Test1 + Test2
    }
    @AfterClass public static void tearDownClass() {
        // Common cleanup for all tests
    }

}
