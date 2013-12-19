package com.alcatel_lucent.dms.service.parser

import org.junit.*
import org.mozilla.javascript.*
import org.springframework.beans.factory.annotation.Autowired


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
                console.log("p1="+p1+", p2="+p2);
            }

            testFun();
       """
        String filePath = "${System.getenv('NODE_PATH')}/coffee-script/lib/coffee-script/coffee-script.js"
        String requireJS = "C:/Users/guoshunw.AD4/Desktop/temp/r.js"
//        js = FileUtils.readFileToString(new File(requireJS))

        Context ctx = Context.enter()
        Scriptable scope = ctx.initStandardObjects()
        Object result = ctx.evaluateString(scope, js, "<cmd>", 1, null)
        println Context.toString(result)
        ctx.exit()

//        Invocable invokable = engine as Invocable
//        invokable.invokeFunction "testFun", "I am ", "here"

    }
}
