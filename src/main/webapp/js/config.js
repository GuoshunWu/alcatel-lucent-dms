/**
 * Created by IntelliJ IDEA.
 * User: Guoshun Wu
 * Date: 12-9-14
 * Time: 下午12:08
 * To change this template use File | Settings | File Templates.
 */


require.config({
    //By default load any module IDs from js/lib
    baseUrl:'js/lib',
//    enforceDefine: true,
    paths:{
        jquery:'jquery-1.7.2.min',
        jqueryui:'jquery-ui-1.8.22.custom.min',
        jqgrid:'jquery.jqGrid.min',
        jqtree:'jquery.jstree',
        jqlayout:'jquery.layout-latest',
        jqmsgbox:'jquery.msgBox.v1',
        blockui:'jquery.blockUI',
        jqupload:'jsfileuploader/jquery.fileupload',
        iframetransport:'jsfileuploader/jquery.iframe-transport',
//        modules
        appmng:'../appmng',
        transmng:'../transmng',
        taskmng:'../taskmng',
        login:'../login',

        nls:'../nls',
//      for coffee script
        cs:'../cs',
        'coffee-script':'../coffee-script'
    },
    shim:{
        'jqueryui':{
            deps:['jquery'],
            exports:'jQuery'
        },
        'jqupload':{
            deps:['jquery'],
            exports:'jQuery'
        },
        'i18n/grid.locale-en':{
            deps:['jquery'],
            exports:'jQuery'
        },
        'ui.multiselect':{
            deps:['jquery'],
            exports:'jQuery'
        },
        'jqgrid':{
            deps:['jqueryui', typeof param !== "undefined" && param !== null ? param.i18ngridfile : 'i18n/grid.locale-en'],
            exports:'jQuery'
        },
        'jqtree':{
            deps:['jquery'],
            exports:'jQuery'
        },
        'jqlayout':{
            deps:['jquery'],
            exports:'jQuery'
        },
        'jqmsgbox':{
            deps:['jquery'],
            exports:'jQuery'
        },
        'blockui':{
            deps:['jquery'],
            exports:'jQuery'
        },
        themeswitchertool:{
            deps:['jquery'],
            exports:'jQuery'
        }
    },
    config:{
        //Set the config for the i18n
        //module ID
        i18n:{
            locale:typeof param !== "undefined" && param !== null ? param.locale : 'en_us'
        }
    },
    urlArgs:"bust=" + (new Date()).getTime()
});
