package com.alcatel_lucent.dms.service.parser

import org.apache.commons.io.FileUtils
import org.junit.*
import org.junit.runner.RunWith

import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.transaction.TransactionConfiguration



import org.mozilla.javascript.*
import javax.script.*

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

        String filePath = "${System.getenv('NODE_PATH')}/coffee-script/lib/extra/coffee-script.js"
//        filePath = "${System.getenv('NODE_PATH')}/coffee-script/lib/coffee-script/coffee-script.js"

        String rJSPath = "${System.getenv('NODE_PATH')}/requirejs/bin/r_rhino.js"


        String rJSPathStr = FileUtils.readFileToString(new File(rJSPath), "UTF-8")
        String jsFileStr = FileUtils.readFileToString(new File(filePath), "UTF-8")

        // Creates and enters a Context. The Context stores information
        // about the execution environment of a script.
        Context ctx = Context.enter()
        try {
            /**
             * see: https://github.com/jashkenas/coffee-script/wiki/Using-CS-with-Java-Rhino
             * there is a 64k byte code limit in Java for compiled code, and as both the full/minimised coffee-script.js and the individual files break this when compiled,
             * you have to go down the interpreted javascript route, which means optimisation level -1 (ie its slower than it could be).
             *
             * the Rhino engine has no require facility, but the library RingoJS is a way to get some CommonsJS facilities
             * */
            ctx.setOptimizationLevel(-1); // Without this, Rhino hits a 64K bytecode limit and fails

            final Scriptable globalScope = ctx.initStandardObjects()
            // Add a global variable "out" that is a JavaScript reflection of System.out
            Object jsOut = Context.javaToJS(System.out, globalScope)
            ScriptableObject.putProperty(globalScope, "out", jsOut)
//            String returnStr =ctx.evaluateString(globalScope, rJSPathStr, "require.js", 0, null)
//            println returnStr
//            return
            ctx.evaluateString(globalScope, jsFileStr, "coffee-script.js", 1, null)

            ScriptableObject coffeeScript = globalScope.get("CoffeeScript", globalScope)
            String coffeeJSTest ="""
(console=
  log:(obj)->out.println(obj)
  info:(obj)->out.println(obj)
)unless console

console.log 'This is my coffee script!'
"""
            //Coffee script compiled java scripts
            Object result = ((Function)coffeeScript.get("compile")).call ctx, globalScope, coffeeScript, [coffeeJSTest] as Object[]
            println "Compiled coffee script(js code)".center(100, '=')
            println ctx.toString(result)

            // Run coffee script directly
            ((Function)coffeeScript.get("run")).call ctx, globalScope, coffeeScript, [coffeeJSTest] as Object[]

        } finally {
            ctx.exit()
        }
    }

//    @Test
    void testJAVA6JS(){
        String filePath = "${System.getenv('NODE_PATH')}/coffee-script/lib/extra/coffee-script.js"
        String jsFileStr = FileUtils.readFileToString(new File(filePath), "UTF-8")

        // create a script engine manager
        ScriptEngineManager factory = new ScriptEngineManager();
        // create JavaScript engine
        ScriptEngine engine = factory.getEngineByName("JavaScript");

        // evaluate JavaScript code from given file - specified by first argument
        engine.eval(jsFileStr);

        // get script object on which we want to call the method
        Object coffeeScript = engine.get("CoffeeScript")
        Invocable inv = (Invocable) engine

        String coffeeJSTest ="""
(console=
  log:(obj)->out.println(obj)
  info:(obj)->out.println(obj)
)unless console

console.log 'This is my coffee script!'
"""

        inv.invokeMethod(coffeeScript, "run", coffeeJSTest)


    }
}
