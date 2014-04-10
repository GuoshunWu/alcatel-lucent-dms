package com.alcatel_lucent.dms.service.parser

import com.alcatel_lucent.dms.util.CoffeeScript
import org.apache.commons.io.IOUtils
import org.junit.*
import org.junit.runner.RunWith
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
public class ParserTest {

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
    void testRhinoJS() throws Exception {
        println CoffeeScript.compile("a =234")
        CoffeeScript.run("console.info('This is a test.');")
    }

//    @Test
    void testJAVA6JS() {
        String wrappedJs = IOUtils.toString(getClass().getResourceAsStream("/js/lib/coffee-script.js"), "UTF-8")

        // create a script engine manager
        ScriptEngineManager factory = new ScriptEngineManager();
        // create JavaScript engine
        ScriptEngine engine = factory.getEngineByName("JavaScript");
        // evaluate JavaScript code from given file - specified by first argument
        engine.eval(wrappedJs);

        // get script object on which we want to call the method
        Object coffeeScript = engine.get("CoffeeScript")
        Invocable inv = (Invocable) engine

        String coffeeJSTest = """
           a = 2345
           #console.log 'This is my coffee script!'
       """
        println inv.invokeMethod(coffeeScript, "compile", coffeeJSTest)
    }
}
