/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 12-9-28
 * Time: 下午10:09
 * command in current dir: r -o app.build.js optimize=none
 */

({
    appDir:'../',
    mainConfigFile:'config.js',
    dir:'../../../../target/dms_build',

    modules:[
        //First set up the common build layer.
        {
            //module names are relative to baseUrl
            name:'../config',
            //List common dependencies here. Only need to list
            //top level dependencies, "include" will find
            //nested dependencies.
            include:['jquery',
                'appmng/apptree',
                'transmng/layout'
            ]
        }
        ,
        {
            //module names are relative to baseUrl/paths config
            name:'appmng/appmng',
            exclude:['../config']
        }
//        ,{
//            //module names are relative to baseUrl
//            name:'transmng/transmng',
//            exclude:['../config']
//        }
    ]
})
