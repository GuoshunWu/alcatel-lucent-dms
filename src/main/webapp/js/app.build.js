/**
 *
 * java command line:
 set M2_REP=D:\M2_repository
 java -cp %M2_REP%/org/mozilla/rhino/1.7R3/rhino-1.7R3.jar;%M2_REP%/com/google/javascript/closure-compiler/v20131014/closure-compiler-v20131014.jar ../tools/r.js -o app.build.js

 */

({
    appDir: '../',
    mainConfigFile: 'config.js',

    //How to optimize all the JS files in the build output directory.
    //Right now only the following values
    //are supported:
    //- "uglify": (default) uses UglifyJS to minify the code.
    //- "closure": uses Google's Closure Compiler in simple optimization
    //mode to minify the code. Only available if running the optimizer using
    //Java.
    //- "closure.keepLines": Same as closure option, but keeps line returns
    //in the minified files.
    //- "none": no minification will be done.
    optimize: "uglify2",
//    optimize:"none",

    preserveLicenseComments: false,
    generateSourceMaps: false,
    uglify2: {
        output: {
            beautify: false
        },
        compress: {
            sequences: true,
            global_defs: {
                DEBUG: false
            }
        },
        warnings: true,
        mangle: false

    },


    fileExclusionRegExp: "^(.*\.(coffee|map|src|cmd)|.*r\(.min)?\.js)$",

    optimizeCss: "standard.keepLines",
    pragmasOnSave: {
        excludeCoffeeScript: true
    },
    keepBuildDir: true,

    modules: [
        //First set up the common build layer.
        {
            //module names are relative to baseUrl
            name: '../config',
            //List common dependencies here. Only need to list
            //top level dependencies, "include" will find
            //nested dependencies.
            include: [
//                'jquery',
            ]
        },

        {
            //module names are relative to baseUrl
            name: 'main',
            exclude: ['../config']
        }
        ,
        {
            //module names are relative to baseUrl
            name: 'login/main',
            exclude: ['../config'],
            include: ['login/main']
        }
    ]
})
