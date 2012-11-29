<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<link rel="shortcut icon" href="favicon.ico" type="image/x-icon"/>
<%@ page contentType="text/html;charset=utf-8" %>
<script type="text/javascript">
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
                if (200 == request.status) {
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
    HTTP.get('<s:url value="/"/>app/get-locale', function (json) {

        var locale = json.message.replace('_', '-').toLocaleLowerCase();
        param.locale = locale;
//    param.locale = 'zh-cn'

        param.i18ngridfile = 'i18n/grid.locale-' +
                (gridI18NMap[param.locale] ? gridI18NMap[param.locale] : param.locale);

    }, function (errStatus, errText) {
        console.log("Error:" + errText + "(" + errStatus + ")");
    });
    param.buildNumber = '1';


</script>