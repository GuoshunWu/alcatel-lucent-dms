/**
 * Created by IntelliJ IDEA.
 * User: Guoshun Wu
 * Date: -8-
 * Time: 下午7:
 * To change this template use File | Settings | File Templates.
 */

define(['jquery'], function ($) {
    //Do setup work here
    /**
     * format json string to pretty.
     * */
    function formatJonString(jsonString) {
        var retval = '';
        var str = jsonString;
        var pos = 0;
        var strLen = str.length;
        var indentStr = '    ';
        var newLine = '\n';
        var char = '';

        for (var i = 0; i < strLen; i++) {

            char = str.substring(i, i + 1);
            if (char == '}' || char == ']') {
                retval = retval + newLine;
                pos = pos - 1;
                for (var j = 0; j < pos; j++) {
                    retval = retval + indentStr;
                }
            }
            retval = retval + char;
            if (char == '{' || char == '[' || char == ',') {
                retval = retval + newLine;
                if (char == '{' || char == '[') {
                    pos = pos + 1;
                }
                for (var k = 0; k < pos; k++) {
                    retval = retval + indentStr;
                }
            }
        }
        return retval;
    }

    return {
        json2string:function (jsonObj) {
            return formatJonString(JSON.stringify(jsonObj));
        },
        getDependencies:function getDependencies(moduleName, dependenciesArray) {
            return $(dependenciesArray).map(function () {
               return  moduleName + '/' + this;
            }).get();
        }
    }
});
