package com.alcatel_lucent.dms.util;

import org.junit.Ignore;
import org.junit.Test;

import java.io.File
import org.apache.commons.collections.PredicateUtils
import org.apache.commons.collections.ClosureUtils
import org.apache.commons.collections.CollectionUtils
import org.apache.commons.collections.list.PredicatedList
import org.apache.commons.collections.functors.NotNullPredicate
import org.apache.commons.collections.ListUtils
import org.apache.commons.collections.Transformer
import org.apache.commons.collections.Predicate
import org.apache.commons.collections.functors.EqualPredicate
import org.apache.commons.collections.MapUtils;

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
        println CollectionUtils.exists(['a1','b','c'], new EqualPredicate('a1'))
    }

    @Test void testString2Map(){
        MapUtils.debugPrint(System.out,"Debug for string2Map:" ,Util.string2Map("a=b;c=d;e=f"));
        println Util.map2String(Util.string2Map("a=b;c=d;e=f"))
    }

}
