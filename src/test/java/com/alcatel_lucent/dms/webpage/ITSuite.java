package com.alcatel_lucent.dms.webpage;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Created by Administrator on 2014/5/17 0017.
 */

@RunWith(Suite.class)
@Suite.SuiteClasses({DMSIntegrateTest.class})
public class ITSuite {
    @BeforeClass
    public static void setUpClass() {
        // Common initialization done once for Test1 + Test2
    }

    @AfterClass
    public static void tearDownClass() {
        // Common cleanup for all tests
    }
}
