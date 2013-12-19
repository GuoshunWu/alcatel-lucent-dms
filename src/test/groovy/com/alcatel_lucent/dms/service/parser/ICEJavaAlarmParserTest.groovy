package com.alcatel_lucent.dms.service.parser

import net.sf.json.JSONObject
import org.apache.commons.io.FileUtils
import org.junit.*
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.transaction.TransactionConfiguration

import javax.script.Invocable
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager

/**
 * Created by IntelliJ IDEA.
 * User: guoshunw
 * Date: 12-12-18
 * Time: 上午11:50
 * To change this template use File | Settings | File Templates.
 */

@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = ["/spring.xml"])
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)

public class ICEJavaAlarmParserTest {

    @Autowired
    private ICEJavaAlarmParser iceJavaAlarmParser = new ICEJavaAlarmParser();


    @BeforeClass
    static void setUpBeforeClass() throws Exception {

    }

    @AfterClass
    static void tearDownAfterClass() throws Exception {
    }

    @Before
    void setUp() throws Exception {

    }

    @After
    void tearDown() throws Exception {
    }

//    @Test
    void testParse() throws Exception {
        new File("C:\\Users\\Administrator\\Desktop\\Test\\en-us\\").eachFile {
            String testStr = FileUtils.readFileToString it, "UTF-8"
            String json = OTCWebParser.getJSONContent(testStr)
//        json = StringEscapeUtils.escapeEcmaScript(json)
//            println json
            println JSONObject.fromObject(json)

        }
    }

    @Test
    void testJS(){
        File jsFile = new File("C:\\Users\\Administrator\\Desktop\\Test\\coffee-script.js")
        String jsFileStr = FileUtils.readFileToString(jsFile, "UTF-8")
        // create a script engine manager
        ScriptEngineManager factory = new ScriptEngineManager();
        // create a JavaScript engine
        ScriptEngine engine = factory.getEngineByName("JavaScript");
        // evaluate JavaScript code from String
        engine.eval(jsFileStr);
        Object coffeeScript=engine.get("CoffeeScript")
        Invocable inv = (Invocable) engine;
        inv.invokeMethod(coffeeScript, "compile")
    }
}
