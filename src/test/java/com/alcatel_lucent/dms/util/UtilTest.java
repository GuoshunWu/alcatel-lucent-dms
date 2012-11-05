package com.alcatel_lucent.dms.util;

import org.junit.Test;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: guoshunw
 * Date: 12-10-12
 * Time: 上午11:14
 * To change this template use File | Settings | File Templates.
 */
public class UtilTest {
//    @Test
    public void testUnzip() throws Exception {
        Util.unzip("D:/test/dms.zip","d:/tmp");
    }

    @Test
    public void testCreateZip() throws Exception {
        Util.createZip(new File("D:/tmp/MyTest").listFiles(),new File("D:/test/myTest.zip"));
    }

}
