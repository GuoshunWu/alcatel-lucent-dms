package com.alcatel_lucent.dms.util

import com.alcatel_lucent.dms.model.Glossary
import junit.framework.Assert
import org.apache.commons.collections.*
import org.apache.commons.collections.functors.EqualPredicate
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.apache.commons.io.input.AutoCloseInputStream
import org.apache.commons.io.input.BOMInputStream
import org.junit.Ignore
import org.junit.Test

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by IntelliJ IDEA.
 * User: guoshunw
 * Date: 12-10-12
 * Time: 上午11:14
 * To change this template use File | Settings | File Templates.
 */
//@Ignore
public class UtilTest {

//    @Test
    void testUnzip() throws Exception {
        Util.unzip("D:/test/dms.zip", "d:/tmp");
    }

//    @Test
    void testCreateZip() throws Exception {
        Util.createZip(new File("D:/tmp/MyTest").listFiles(), new File("D:/test/myTest.zip"));
    }

//    @Test
    void testPredicate() {
        Map<String, org.apache.commons.collections.Closure> closureMap = [
                'c1': new org.apache.commons.collections.Closure() {
                    @Override
                    void execute(Object input) {
                        println "In closure c1, input=$input"
                    }
                },
                'c2': new org.apache.commons.collections.Closure() {
                    @Override
                    void execute(Object input) {
                        println "In closure c2, input=$input"
                    }
                }
        ]

        org.apache.commons.collections.Closure mc = ClosureUtils.switchMapClosure(closureMap)
        CollectionUtils.forAllDo(['c1', 'c3', 'c5', 'c2'], mc)
    }

//    @Test
    void testClosure() {
        println 'Test begin...'
        org.apache.commons.collections.Closure cl = new org.apache.commons.collections.Closure() {
            @Override
            void execute(Object input) {
                println "input=$input"
            }
        };
        CollectionUtils.forAllDo(['a', 'b', 'c'], cl)
    }

//    @Test
    void testTransformer() {
        Transformer transformer = new Transformer() {
            @Override
            Object transform(Object input) {
                return "${input}aaa"
            }
        }

        List list = ListUtils.transformedList(['a1', 234, 'c'], transformer)
        println CollectionUtils.collect(['a1', 234, 'c'], transformer)
        println CollectionUtils.exists(['a1', 'b', 'c'], new EqualPredicate('a1'))
    }

//    @Test
    void testString2Map() {
        MapUtils.debugPrint(System.out, "Debug for string2Map:", Util.string2Map("a=b;c=d;e=f"));
        println Util.map2String(Util.string2Map("a=b;c=d;e=f"))
    }

//    @Test
    void testLabelFieldAccess() {
//        println DurationFormatUtils.formatDuration(65450000l, "HH 'hour(s)' mm 'minute(s)' ss 'second(s)'")
        StringWriter temp = new StringWriter()
        PrintWriter pw = new PrintWriter(temp)
        pw.println("Hello, world")
        pw.println("What")
        temp = new StringWriter()
        pw.close()
        pw = new PrintWriter(temp)
        pw.println("ABC")
        println temp

    }

//    @Test
    void testFileFormat() {
        File file = new File("D:/MyDocuments/Alcatel_LucentSBell/DMS/DMSFiles/ACSTextDict/MyTest.txt")
        String encoding = Util.detectEncoding(file)
        InputStream is = new AutoCloseInputStream(new FileInputStream(file));
        println "encoding=${encoding}"
        if (Arrays.asList("UTF-8", "UTF-LE", "UTF-BE").contains(encoding)) {
            is = new BOMInputStream(is)
        }
        ArrayList<String> lines = IOUtils.readLines(is, encoding)
        lines.each { line ->
            println line
        }
    }

//    @Test
    void testAsyncServlet() {

    }
}
