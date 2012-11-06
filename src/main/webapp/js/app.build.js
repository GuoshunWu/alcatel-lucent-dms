/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 12-9-28
 * Time: 下午10:09
 * command in current dir: r -o app.build.js
 */

({
//    baseUrl:'js/lib',
    appDir:'../',
    mainConfigFile:'config.js',
//    dir:'../../../../target/dms_build',
    dir:'D:/ProgramTools/apache-tomcat-7.0.30/webapps/dms',

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
    optimize:"none",

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
