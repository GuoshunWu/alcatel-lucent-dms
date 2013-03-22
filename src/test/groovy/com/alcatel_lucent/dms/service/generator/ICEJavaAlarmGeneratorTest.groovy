package com.alcatel_lucent.dms.service.generator

import com.alcatel_lucent.dms.model.Dictionary
import com.alcatel_lucent.dms.service.generator.xmldict.XMLDictGenerator
import com.alcatel_lucent.dms.service.parser.ICEJavaAlarmParser
import com.alcatel_lucent.dms.service.parser.XMLDictParser
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.transaction.TransactionConfiguration

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
public class ICEJavaAlarmGeneratorTest {

    @Autowired
    private ICEJavaAlarmParser iceJavaAlarmParser;

    @Autowired
    private ICEJavaAlarmGenerator iceJavaAlarmGenerator;

    @Autowired
    private List<DictionaryGenerator> generators;

    @Autowired
    private Map<String, DictionaryGenerator> generatorMap;

//    @Test
    void testGenerateDict() throws Exception {
        File file = new File("D:/MyDocuments/Alcatel_LucentSBell/DMS/DMSFiles/ICEJavaAlarm/catalog-builder-plugin-1.3.000.000-schemas/")
        ArrayList<Dictionary> dictionaries = iceJavaAlarmParser.parse(file.absolutePath, file, [] as Collection<File>)

        int i = 0
//        dictionaries.each { dict ->
//            iceJavaAlarmGenerator.generateDict(new File("D:/test/icejavaalarm"), dict)
//            ++i
//        }
    }

    @Test
    void testMe(){
        println "Generator List".center(100, '=')
        generators.each {generator->
            println generator
        }
        println "Generators List Ends".center(100, '=')

        println "Generator Map".center(100, '=')
        generatorMap.each {generator->
            println generator
        }
        println "Generators List Map".center(100, '=')

    }
}
