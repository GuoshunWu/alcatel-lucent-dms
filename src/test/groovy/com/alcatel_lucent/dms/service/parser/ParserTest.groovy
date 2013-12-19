package com.alcatel_lucent.dms.service.parser

import net.sf.json.JSONObject
import org.apache.commons.io.FileUtils
import org.junit.*
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scripting.ScriptFactory
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
public class ParserTest {

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

    @Test
    void testParse() throws Exception {
        String js = """
            function testFun(p1, p2){
                print("p1="+p1+", p2="+p2);
            }
       """
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("javascript")
        engine.eval(js)
        Invocable invokable = engine as Invocable

        invokable.invokeFunction "testFun", "I am ", "here"

    }
}
