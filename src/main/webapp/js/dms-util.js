/**
 * Created by IntelliJ IDEA.
 * User: Guoshun Wu
 * Date: -8-
 * Time: 下午7:
 * To change this template use File | Settings | File Templates.
 */

// formatJson() :: formats and indents JSON string
function jsonToString(jsonObj){
    return formatJson(JSON.stringify(jsonObj));
}

function formatJson(val) {
    var retval = '';
    var str = val;
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

/**
 * Log a info to console if console is supported
 * */
function log(info){
    if(console && console.log){
        console.log(info);
    }
}

/**
 * init a select by json data from url
 * the original options in select will be removed.
 * */
function initSelect(select, url){
    select.length=0;
    $.get(url,{},function(json){
        if(typeof(json)=="string") {
            json=eval(json);
        }
        for(var i=0; i<json.length; ++i){
            select.options.add(new Option(json[i].text, json[i].value));
        }
    });
}
