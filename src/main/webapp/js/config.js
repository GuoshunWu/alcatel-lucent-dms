/**
 * Created by IntelliJ IDEA.
 * User: Guoshun Wu
 * Date: 12-9-14
 * Time: 下午12:08
 * To change this template use File | Settings | File Templates.
 */

// a ajax tool to request struts locale info

var HTTP = {};
HTTP._factories = [
    function () {
        return new XMLHttpRequest();
    },
    function () {
        return new ActiveXObject("Msxml2.XMLHTTP");
    },
    function () {
        return new ActiveXObject("Microsoft.XMLHTTP");
    }
];

//Wehn we find a factory that works store it here
HTTP._factory = null;
HTTP.newRequest = function () {
    if (HTTP._factory) return HTTP._factory();
    for (var i = 0; i < HTTP._factories.length; ++i) {
        try {
            var factory = HTTP._factories[i];
            var request = factory();
            if (request != null) {
                HTTP._factory = factory;
                return request;
            }
        } catch (e) {
            continue;
        }
    }

    HTTP._factory = function () {
        throw new Error("XMLHttpRequest not supportted");
    }
    HTTP._factory();
};
HTTP._getResponse = function (request) {
    var contentType = request.getResponseHeader("Content-Type");

    if (contentType.indexOf("json") != -1 || contentType.indexOf("javascript") != -1) {
        return eval("(" + request.responseText + ")");
    }
    return request.responseText;

};
HTTP.get = function (url, callback, errorHandler) {
    var request = HTTP.newRequest();
    request.onreadystatechange = function () {
        if (request.readyState == 4) {
            if (request.status = 200) {
                callback(HTTP._getResponse(request));
            }
            else {
                if (errorHandler)errorHandler(request.status, request.statusText);
            }
        }
    };
    // false is synchronous method, will blocked
    request.open("GET", url, false);
    request.send(null);
};

gridI18NMap = {
    ar:'ar', bg:'bg', bg1251:'bg1251', cat:'cat', 'zh-cn':'cn',
    cs:'cs', da:'da', de:'de', dk:'dk', el:'el',
    'en-us':'en', es:'es', fa:'fa', fi:'fi',
    fr:'fr', gl:'gl', he:'he', hr:'hr', hr1250:'hr1250',
    hu:'hu', is:'is', it:'it', ja:'ja', lt:'lt',
    mne:'mne', nl:'nl', no:'no', pl:'pl', pt:'pt',
    'pt-br':'pt-br', ro:'ro', ru:'ru', sk:'sk',
    sr:'sr', 'sr-latin':'sr-latin', sv:'sv',
    th:'th', tr:'tr', ua:'ua'
};
var param = {};
HTTP.get('app/get-locale', function (json) {
    var locale = json.message.replace('_', '-').toLocaleLowerCase();
    param.locale = locale;
    param.i18ngridfile = 'i18n/grid.locale-' +
        (gridI18NMap[param.locale] ? gridI18NMap[param.locale] : param.locale);
//    console.log(param);
}, function (errStatus, errText) {
    console.log("Error:" + errText + "(" + errStatus + ")");
});

/**
 * Convert module dependencies to its correct name
 * */
function convertDependencies(moduleName, array) {
    if (typeof array == "string") return moduleName + "/" + array;
    var newArray = [];
    for (var i = 0; i < array.length; ++i) {
        var dependency=moduleName + "/"+array[i];
        if (array[i].indexOf('!') != -1) {
            var splitParts=array[i].split('!');
            dependency=splitParts[0]+"!"+moduleName+"/"+splitParts[1];
        }
        newArray.push(dependency);
    }
    return newArray;
}


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
//        modules
        appmng:'../appmng',
        transmng:'../transmng',
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
            deps:['jqueryui', param.i18ngridfile],
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
        themeswitchertool:{
            deps:['jquery'],
            exports:'jQuery'
        }
    },
    config:{
        //Set the config for the i18n
        //module ID
        i18n:{
            locale:param.locale
        }
    },
    waitSeconds:5
});
