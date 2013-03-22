package com.alcatel_lucent.dms.service.generator

import com.alcatel_lucent.dms.model.Dictionary
import com.alcatel_lucent.dms.service.parser.ICEJavaAlarmParser
import org.apache.commons.collections.MapUtils
import org.apache.commons.collections.Transformer
import org.apache.commons.collections.TransformerUtils
import org.apache.commons.collections.map.TransformedMap
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
    private ICEJavaAlarmParser iceJavaAlarmParser

    @Autowired
    private ICEJavaAlarmGenerator iceJavaAlarmGenerator


    private Map<String, DictionaryGenerator> generators

    @Autowired
    void setGenerators(Map<String, DictionaryGenerator> generators) {
        this.generators = generators

        Map<String, DictionaryGenerator> tmpGeneratorMap= new HashMap<String, DictionaryGenerator>()

        Collection<DictionaryGenerator> tmpGenerators = generators.values()
        tmpGenerators.each {generator->
            tmpGeneratorMap.put generator.format,generator
        }
        this.generators.clear()
        this.generators= tmpGeneratorMap
    }

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
    void testMe() {
        MapUtils.debugPrint(System.out, "Generator Map", generators)
    }
}
