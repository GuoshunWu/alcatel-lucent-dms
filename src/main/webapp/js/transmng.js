/**
 * Created by IntelliJ IDEA.
 * User: SYSTEM
 * Date: 12-9-12
 * Time: 下午10:24
 * To change this template use File | Settings | File Templates.
 */

requirejs.config({
    //By default load any module IDs from js/lib
    baseUrl:'js',
    //except, if the module ID starts with "app",
    //load it from the js/app directory. paths
    //config is relative to the baseUrl, and
    //never includes a ".js" extension since
    //the paths config could be for a directory.
    paths:{
//        app: '../app'
//        transmng: '../transmng',
        jquery:'lib/jquery-1.7.2.min',
        jqueryui:'lib/jquery-ui-1.8.22.custom.min',
        jqgrid:'lib/jquery.jqGrid.min'
    },
    shim:{
        'jqueryui':{
            //These script dependencies should be loaded before loading
            //backbone.js
            deps:['jquery'],
            //Once loaded, use the global 'Backbone' as the
            //module value.
            exports:'jQuery'
        },
        'lib/i18n/grid.locale-en':{
            deps:['jquery'],
            exports:'jQuery'
        },
        'jqgrid':{
            //These script dependencies should be loaded before loading
            //backbone.js
            deps:['lib/i18n/grid.locale-en', 'jquery', 'jqueryui'],
            //Once loaded, use the global 'Backbone' as the
            //module value.
            exports:'jQuery'
        }
    },
    config:{
        //Set the config for the i18n
        //module ID
        i18n:{
            locale:'zh-cn'
        }
    }

});

requirejs(['transmng/lamps', 'jqgrid'], function (lamps, $) {

    alert(lamps.testMessage);
    if (typeof($.ui) === 'undefined') {
        console.log("ERROR: jquery-ui could not be found");
    } else {
        console.log("JQuery UI is " + $.ui.version);
    }
    var testTable = $('#testTable').jqGrid({
        url:'json/taskgrid.json',
        editurl:"",
        datatype:'json',
        width:'auto',
        height:'auto',
//        shrinkToFit:false,
        rownumbers:true,
        loadonce:false,
        pager:'#taskPager',
        rowNum:10,
        rowList:[10, 20, 30],
        sortname:'name',
        sortorder:'asc',
        viewrecords:true,
        gridview:true,
        caption:'Translation Task List', colNames:['ID', 'Application', 'Dictionary', 'Encoding', 'Format', 'Num of String',
            'T', 'N', 'I', 'T', 'N', 'I', 'T', 'N', 'I'
        ],
        colModel:[
            {name:'id', index:'id', width:55, align:'center', hidden:true, frozen:true},
            {name:'application', index:'application', width:100, editable:true, stype:'select', edittype:'select', align:'center', editoptions:{value:"All:All;0.00:0.00;12:12.00"}, frozen:true},
            {name:'dictionary', index:'dictionary', width:90, editable:true, align:'center', frozen:true},
            {name:'encoding', index:'encoding', width:90, editable:true, align:'center', frozen:true},
            {name:'format', index:'format', width:90, editable:true, align:'center', frozen:true},
            {name:'numOfString', index:'NumOfString', width:80, align:'center', frozen:true},
            {name:'Arabic.T', index:'T', width:20, align:'center'},
            {name:'Arabic.N', index:'N', width:20, editable:true, align:'center'},
            {name:'Arabic.I', index:'I', width:20, editable:true, align:'center'},
            {name:'Czech.T', index:'T', width:20, align:'center'},
            {name:'Czech.N', index:'N', width:20, editable:true, align:'center'},
            {name:'Czech.I', index:'I', width:20, editable:true, align:'center'},
            {name:'Chinese.T', index:'T', width:20, align:'center'},
            {name:'Chinese.N', index:'N', width:20, editable:true, align:'center'},
            {name:'Chinese.I', index:'I', width:20, editable:true, align:'center'}
        ]
    });

    testTable.navGrid('#taskPager', {edit:true, add:true, true:true, true:false, view:true});

});