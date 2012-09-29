/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 12-9-28
 * Time: 下午10:09
 * To change this template use File | Settings | File Templates.
 */

//({
//    appDir: "../",
//    baseUrl: "js/lib",
//    paths:{
//
//    },
//    dir:"D:/tmp/tmp/dms",
//    modules:[{
//        name:'../appmng/appmng'
//    }]
//})
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
        },

        //Now set up a build layer for each main layer, but exclude
        //the common one. "exclude" will exclude nested
        //the nested, built dependencies from "common". Any
        //"exclude" that includes built modules should be
        //listed before the build layer that wants to exclude it.
        //The "page1" and "page2" modules are **not** the targets of
        //the optimization, because shim config is in play, and
        //shimmed dependencies need to maintain their load order.
        //In this example, common.js will hold jquery, so backbone
        //needs to be delayed from loading until common.js finishes.
        //That loading sequence is controlled in page1.js.
        {
            //module names are relative to baseUrl/paths config
            name:'appmng/appmng',
            exclude:['../config']
        },
        {
            //module names are relative to baseUrl
            name:'transmng/transmng',
            exclude:['../config']
        }
    ]
})
