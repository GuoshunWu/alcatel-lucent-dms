/**
 * Created by IntelliJ IDEA.
 * User: Guoshun Wu
 * Date: -8-
 * Time: 下午7:
 * To change this template use File | Settings | File Templates.
 */

define(['jquery'], function ($) {
//    prototype enhancement
    String.prototype.endWith = function (str) {
        if (!str || str.length > this.length) return false;
        return (this.substring(this.length - str.length) == str)
    }

    String.prototype.startWith = function (str) {
        if (!str || str.length > this.length) return false;
        return (this.substr(0, str.length) == str);
    }

    function isArray(obj) {
        return Object.prototype.toString.call(obj) === "[object Array]";
    }


    /**
     * insert elem at pos in array.
     * */
    Array.prototype.insert = function (pos, elem) {
        var e, newarray, _i, _j, _len, _len1;
        newarray = this.slice(0, pos);
        if (isArray(elem)) {
            for (_i = 0, _len = elem.length; _i < _len; _i++) {
                e = elem[_i];
                newarray.push(e);
            }
        } else {
            newarray.push(elem);
        }
        newarray = newarray.concat(this.slice(pos, this.length));
        this.length = 0;
        for (_j = 0, _len1 = newarray.length; _j < _len1; _j++) {
            elem = newarray[_j];
            this.push(elem);
        }
        return this;
    };


    /**
     * remove the element at pos in array, return the removed element.
     * */
    Array.prototype.remove = function (pos) {
        var delElem, elem, newarray, _i, _len;
        newarray = this.slice(0, pos);
        newarray = newarray.concat(this.slice(pos + 1, this.length));
        delElem = this[pos];
        this.length = 0;
        for (_i = 0, _len = newarray.length; _i < _len; _i++) {
            elem = newarray[_i];
            this.push(elem);
        }
        return delElem;
    };


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

    function getLocaleCallback(json) {
        return json.message;
    }

    return {
        json2string:function (jsonObj) {
            return formatJonString(JSON.stringify(jsonObj));
        },
        getDependencies:function getDependencies(moduleName, dependenciesArray) {
            return $(dependenciesArray).map(
                function () {
                    return  moduleName + '/' + this;
                }).get();
        },
        getLocale:function () {
            var locale = 'en-us';
            $.ajax({url:'app/get-locale', type:'json',
                async:false,
                success:function (json) {
                    locale = json.message;
                    //todo: we can set js locale configuration here.
//                    console.log('get struts locale successful, locale: ' + locale);
                }, error:function (XMLHttpRequest, textStatus, errorThrown) {
                    console.log('get struts locale error: ' + textStatus + ", use default; " + locale);
                }});
            return locale;
        }
    }
});
