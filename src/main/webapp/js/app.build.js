({
    /*
     java command line:
     set M2_REP=D:\M2_repository
     java -cp %M2_REP%/org/mozilla/rhino/1.7R3/rhino-1.7R3.jar;%M2_REP%/com/google/javascript/closure-compiler/v20131014/closure-compiler-v20131014.jar ../tools/r.js -o app.build.js

     coffee -bc app.build.coffee
     remember to remove the last semicolon after you compile
     */

    appDir: "../",
    mainConfigFile: "config.js",
    optimize: "uglify2",
//    optimize: "none",

    preserveLicenseComments: false,
    generateSourceMaps: false,
    useSourceUrl: false,

    uglify2: {
        output: {
            beautify: false,
            comments: false
        },
        compress: {
            sequences: true,
            global_defs: {
                DEBUG: false
            }
        },
        warnings: true,
        mangle: true
    },
    fileExclusionRegExp: "^(.*\.(coffee|map|src|cmd|build\.js)|.*r(.min)?.js)$",
    optimizeCss: "standard.keepLines",
//    optimizeCss: "none",

    pragmasOnSave: {
        excludeCoffeeScript: true
    },
    keepBuildDir: true,

    //A function that is called for each JS module bundle that has been
    //completed. This function is called after all module bundles have
    //completed, but it is called for each bundle. A module bundle is a
    //"modules" entry or if just a single file JS optimization, the
    //optimized JS file.
    //Introduced in r.js version 2.1.6

    onModuleBundleComplete: function (data) {
        /*
         data.name: the bundle name.
         data.path: the bundle path relative to the output directory.
         data.included: an array of items included in the build bundle.
         If a file path, it is relative to the output directory. Loader
         plugin IDs are also included in this array, but dependending
         on the plugin, may or may not have something inlined in the
         module bundle.
         */
        console.log("========Module %s(path=%s) build complete========", data.name, data.path);
    },
    modules: [
        {
            name: "../loginEntry",
            include: ['requireLib', 'login/main']
        },

//        {
//            name: "../entry",
//            include: ['main', 'almond']
//        }
        {
            name: "../entry",

            include: ['requireLib', 'main']
        }


    ]
})
