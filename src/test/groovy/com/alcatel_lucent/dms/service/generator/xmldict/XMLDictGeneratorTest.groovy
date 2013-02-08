package com.alcatel_lucent.dms.service.generator.xmldict;

import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import com.alcatel_lucent.dms.service.parser.XMLDictParser
import com.alcatel_lucent.dms.model.Dictionary
import org.junit.runner.RunWith
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.transaction.TransactionConfiguration
import org.junit.Ignore;

/**
 * Created by IntelliJ IDEA.
 * User: guoshunw
 * Date: 12-12-21
 * Time: 下午4:29
 * To change this template use File | Settings | File Templates.
 */
//@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = ["/spring.xml"])
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
public class XMLDictGeneratorTest {

    @Autowired
    private XMLDictParser xmlDictParser;

    @Autowired
    private XMLDictGenerator xmlDictGenerator;


//    @Test
    void testGenerateDict() throws Exception {
        File file = new File("D:/testxdct/xdct")
        file = new File("D:/testxdct/test")
        ArrayList<Dictionary> dictionaries = xmlDictParser.parse('', file, [] as Collection<File>)


        int i = 0
        dictionaries.each { dict ->
            xmlDictGenerator.generateDict(new File("D:/testxdct/test"), dict)
            ++i
        }
    }
}
