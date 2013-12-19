package com.alcatel_lucent.dms.service.parser

import net.sf.json.JSONObject
import org.apache.commons.io.FileUtils
import org.junit.*
import org.junit.runner.RunWith
import org.mozilla.javascript.Context
import org.mozilla.javascript.Scriptable
import org.mozilla.javascript.tools.shell.Global
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

//@Ignore
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations = ["/spring.xml"])
//@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)

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
    void testJS() {
        File jsFile = new File("C:\\Users\\Administrator\\Desktop\\Test\\coffee-script.js")
        String jsFileStr = FileUtils.readFileToString(jsFile, "UTF-8")

        Context ctx = Context.enter()
        ctx.setOptimizationLevel(-1); // Without this, Rhino hits a 64K bytecode limit and fails

        final Scriptable globalScope = ctx.initStandardObjects()
        // Add some global function in the global scope see: http://stackoverflow.com/questions/12399462/rhino-print-function
        Global global = new Global(ctx)
        ctx.evaluateString(global, jsFileStr, "coffee-script.js", 0, null)

        ctx.evaluateString(global, "print('Where is the dog?');var a='This is my test;'", "Test", 1, null)
        globalScope.put("b", globalScope, "My test here")
        ctx.evaluateString(global, "print('a=' + a+', b='+b);", "Test", 1, null)
        ctx.exit()

    }
}
