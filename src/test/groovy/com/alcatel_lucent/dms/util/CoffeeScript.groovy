package com.alcatel_lucent.dms.util

import org.apache.commons.io.IOUtils
import org.mozilla.javascript.Context
import org.mozilla.javascript.Scriptable
import org.mozilla.javascript.ScriptableObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory


/**
 * Created by guoshunw on 2014/4/10.
 */
public class CoffeeScript {
    private static CoffeeScript instance
    private static Logger log = LoggerFactory.getLogger(CoffeeScript.class)
    private static String wrappedJs

    private static Context ctx

    private static ScriptableObject coffeeScript

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
            final Scriptable globalScope = ctx.initStandardObjects()

            // Add a global variable "out" that is a JavaScript reflection of System.out
            Object jsConsole = Context.javaToJS(log, globalScope)
            ScriptableObject.putProperty(globalScope, "console", jsConsole)
            ctx.evaluateString(globalScope, wrappedJs, "coffee-script.js", 1, null)

            coffeeScript = globalScope.get("CoffeeScript", globalScope)


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
    public static String compile(String cs, boolean bare = false) {
        ScriptableObject options = ctx.initStandardObjects()
        ScriptableObject.putProperty(options, "bare", bare)
        return ScriptableObject.callMethod(coffeeScript, "compile", [cs, options] as Object[])
    }

    /**
     * run coffee script code
     * @param cs script to run
     * */
    public static void run(String cs) {
        ScriptableObject.callMethod(coffeeScript, "run", [cs] as Object[])
    }
}