package com.alcatel_lucent.dms.service.parser;

import org.junit.After;
import org.junit.Before;
import org.junit.Test

import org.junit.BeforeClass
import org.springframework.test.context.transaction.TransactionConfiguration
import org.springframework.test.context.ContextConfiguration
import org.junit.runner.RunWith
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.beans.factory.annotation.Autowired
import com.alcatel_lucent.dms.model.Dictionary

/**
 * Created by IntelliJ IDEA.
 * User: guoshunw
 * Date: 12-12-18
 * Time: 上午11:50
 * To change this template use File | Settings | File Templates.
 */

//@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = ["/spring.xml"])
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
public class XMLDictParserTest {

    @Autowired
    private XMLDictParser xmlDictParser = new XMLDictParser();

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
        File file = new File("D:/testxdct")
        ArrayList<Dictionary> dictionaries= xmlDictParser.parse('', file, [] as Collection<File>)
        dictionaries.each {dict->
            println dict
        }
    }

//    @Test
    void testGroupDictionaries() throws Exception {
        File file = new File("D:/testxdct/")

        Set entries = xmlDictParser.groupDictionaries(file, [:], acceptedFiles).entrySet().toSet()

        int width = 50
        entries.each {entry ->
            println "Dictionary: ${entry.key}".center(width, '=')
            Collection<File> files = entry.value
            files.each {f ->
                println "${' '*4}$f"
           }
            println "Total: ${files.size()} file(s)".center(width, '=')
        }

    }
}
