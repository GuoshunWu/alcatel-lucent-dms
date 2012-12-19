package com.alcatel_lucent.dms.service.parser;

import org.junit.After;
import org.junit.Before;
import org.junit.Test
import org.junit.Ignore
import org.junit.runner.RunWith
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.transaction.TransactionConfiguration
import org.junit.BeforeClass


/**
 * Created by IntelliJ IDEA.
 * User: guoshunw
 * Date: 12-12-18
 * Time: 上午11:50
 * To change this template use File | Settings | File Templates.
 */

//@Ignore
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations = ["/spring.xml"])
//@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
public class XMLDictParserTest {

    XMLDictParser parser = new XMLDictParser();

    @BeforeClass
    static void setUpBeforeClass() throws Exception {

    }

    @Before
    void setUp() throws Exception {

    }

    @After
    void tearDown() throws Exception {

    }

    @Test
    void testParse() throws Exception {
        File file = new File("D:/testxdct/");
        parser.groupDictionaries(file, [:]).entrySet().toList().each {item ->
            println item.key.center(50, '=')
            item.value.each {f->
                println "${' '*4}$f"
            }
        }

    }
}
