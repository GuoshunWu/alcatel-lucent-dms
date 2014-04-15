package com.alcatel_lucent.dms.util

import org.apache.commons.io.IOUtils
import org.intellij.lang.annotations.Language
import org.mozilla.javascript.Context
import org.mozilla.javascript.Scriptable
import org.mozilla.javascript.ScriptableObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory


/**
 * Created by guoshunw on 2014/4/10.
 *
 * Compile the code in Node coffee script example:
 *      cs.compile 'a=1', {sourceMap:true, generatedFile: 'test.map', sourceFiles: ['test.coffee'], inline: true, bare: true}*/
public class CoffeeScript {
    private static CoffeeScript instance
    private static Logger log = LoggerFactory.getLogger(CoffeeScript.class)
    private static String wrappedJs

    private static Context ctx

    private static ScriptableObject coffeeScript
    private static Scriptable globalScope

    static {
        init()
    }

    private static void init() {
        wrappedJs = IOUtils.toString(getClass().getResourceAsStream("/js/lib/coffee-script.js"), "UTF-8")
        ctx = Context.enter()
        try {
            /**
             * see: https://github.com/jashkenas/coffee-script/wiki/Using-CS-with-Java-Rhino
             * there is a 64k byte code limit in Java for compiled code, and as both the full/minimised coffee-script.js and the individual files break this when compiled,
             * you have to go down the interpreted javascript route, which means optimisation level -1 (ie its slower than it could be).
             *
             * the Rhino engine has no require facility, but the library RingoJS is a way to get some CommonsJS facilities
             * */
            ctx.setOptimizationLevel(-1); // Without this, Rhino hits a 64K bytecode limit and fails
            globalScope = ctx.initStandardObjects()

            // Add a global variable "out" that is a JavaScript reflection of System.out
            Object jsConsole = Context.javaToJS(log, globalScope)
            ScriptableObject.putProperty(globalScope, "console", jsConsole)
            ctx.evaluateString(globalScope, wrappedJs, "coffee-script.js", 1, null)

            coffeeScript = globalScope.get("CoffeeScript", globalScope)

            // test Object
            @Language("JavaScript 1.6") String testJS = """
                var testObj = {
                    name: 'testObject',
                    description: 'This is a test object.',

                    showArray: function(arr){
                        for(var i = 0; i< arr.length; ++i){
                            console.info('arr[i]={}',arr[i]);
                        }
                        console.info(
                            'typeof arr='+ typeof arr
                             + ', arr instanceof Array='+ (arr instanceof Array)
                             +  ', [] instanceof Array='+ ([] instanceof Array)
                        );
                    }
                };
            """

            ctx.evaluateString(globalScope, testJS, 'test.js', 2, null)

        } finally {
            ctx.exit()
        }
        instance = new CoffeeScript()

    }

    private CoffeeScript() {}

    /**
     *  Compile coffee script to javascript
     * @param cs coffee script string to compile
     * @param bare if compile without a top-level function wrapper, default false
     * @return output javascript
     * */
    static String compile(String cs, boolean bare = false) {
        ScriptableObject options = ctx.initStandardObjects()
        ScriptableObject.putProperty(options, "bare", bare)

        ScriptableObject.putProperty(options, "sourceMap", false)
        ScriptableObject.putProperty(options, "generatedFile", 'test.map')
        ScriptableObject.putProperty(options, "sourceFiles", ctx.newArray(globalScope, ['test.coffee'] as Object[]))
        ScriptableObject.putProperty(options, "inline", false)

        return ScriptableObject.callMethod(coffeeScript, "compile", [cs, options] as Object[])
    }

    /**
     * run coffee script code
     * @param cs script to run
     * */
    static void run(String cs) {
        ScriptableObject.callMethod(coffeeScript, "run", [cs] as Object[])
    }

    static void main(String... args) {
        ScriptableObject testObj = globalScope.get("testObj")
        println testObj.get("description")
        Object[] params = [
            ctx.newArray(globalScope, ['aaa', 123, new Date()] as Object[]),
            ctx.newObject(globalScope, 'Object', [] as Object[])
        ]
        ScriptableObject.callMethod(testObj, 'showArray', params)
    }
}