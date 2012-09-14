/**
 * Created by IntelliJ IDEA.
 * User: Guoshun Wu
 * Date: 12-9-14
 * Time: 下午12:08
 * To change this template use File | Settings | File Templates.
 */


requirejs.config({
    //By default load any module IDs from js/lib
    baseUrl:'js/lib',
//    enforceDefine: true,
    paths:{
        jquery:'jquery-1.7.2.min',
        jqueryui:'jquery-ui-1.8.22.custom.min',
        jqgrid:'jquery.jqGrid.min',
        jstree:'jquery.jstree',
        jqlayout:'jquery.layout-latest',
        jqmsgbox:'jquery.msgBox.v1',
//        modules
        appmng:'../appmng',
        transmng:'../transmng',
//        for i18n
//        i18n:'../i18n',
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
        'i18n/grid.locale-en':{
            deps:['jquery'],
            exports:'jQuery'
        },
        'jqgrid':{
            deps:['jqueryui', 'i18n/grid.locale-en' ],
            exports:'jQuery'
        },
        'jstree':{
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
        themeswitchertool:{
            deps:['jquery'],
            exports:'jQuery'
        }
    },
    config:{
        i18n:{
            locale: 'fr-fr'
        }
    }
//    waitSeconds:5
});
