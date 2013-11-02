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
    fileExclusionRegExp: "^(.*\.(coffee|map|src|cmd|build\.js)|.*r(.min)?.js)$",
    optimizeCss: "standard.keepLines",
    pragmasOnSave: {
        excludeCoffeeScript: true
    },
    keepBuildDir: true,

    modules: [
        {
            name: "../entry",
            include: ['main']
//            exclude: ['../config']
        }, {
            name: "login/main"
        }
    ]
})
