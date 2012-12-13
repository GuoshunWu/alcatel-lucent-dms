/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 12-9-28
 * Time: 下午10:09
 * command in current dir: r -o app.build.js
 *
 * java command line:
       set M2_REP=D:/MyDocuments/mavenRepository
      java -cp %M2_REP%/org/mozilla/rhino/1.7R3/rhino-1.7R3.jar;%M2_REP%/com/google/javascript/closure-compiler/r1352/closure-compiler-r1352.jar org.mozilla.javascript.tools.shell.Main ../tools/r.js -o app.build.js

    set LIBPATH=D:/360CloudyDisk\Programing\dmslib\jars
    java -cp %LIBPATH%/js.jar;%LIBPATH%/compiler.jar org.mozilla.javascript.tools.shell.Main ../tools/r.js -o app.build.js

 * java -classpath path/to/rhino/js.jar;path/to/closure/compiler.jar org.mozilla.javascript.tools.shell.Main r.js -o path/to/buildconfig.js
 */

({
//    baseUrl:'js/lib',
    appDir:'../',
//    appDir:'${basedir}',
    mainConfigFile:'config.js',
//    dir:'../../../../target/dms_build',
//    dir:'D:/ProgramTools/apache-tomcat-7.0.30/webapps/dms',

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
//    optimize:"none",

    //When the optimizer copies files from the source location to the
    //destination directory, it will skip directories and files that start
    //with a ".". If you want to copy .directories or certain .files, for
    //instance if you keep some packages in a .packages directory, or copy
    //over .htaccess files, you can set this to null. If you want to change
    //the exclusion rules, change it to a different regexp. If the regexp
    //matches, it means the directory will be excluded. This used to be
    //called dirExclusionRegExp before the 1.0.2 release.
    //As of 1.0.3, this value can also be a string that is converted to a
    //RegExp via new RegExp().
    fileExclusionRegExp:"/^\./|.*\.coffee",

    modules:[
        //First set up the common build layer.
        {
            //module names are relative to baseUrl
            name:'../config',
            //List common dependencies here. Only need to list
            //top level dependencies, "include" will find
            //nested dependencies.
            include:[
//                'jquery',
            ]
        },
        {
            //module names are relative to baseUrl/paths config
            name:'appmng/navigatetree',
            exclude:['../config'],
            include:['appmng/navigatetree']
        },
        {
            //module names are relative to baseUrl
            name:'transmng/layout',
            exclude:['../config'],
            include:['transmng/layout']
        },
        {
            //module names are relative to baseUrl
            name:'taskmng/layout',
            exclude:['../config'],
            include:['taskmng/layout']
        },
        {
            //module names are relative to baseUrl
            name:'login/main',
            exclude:['../config'],
            include:['login/main']
        }
    ]
})
