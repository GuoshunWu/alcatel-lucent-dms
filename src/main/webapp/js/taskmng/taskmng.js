/**
 * Created by IntelliJ IDEA.
 * User: SYSTEM
 * Date: 12-9-12
 * Time: 下午10:24
 * To change this template use File | Settings | File Templates.
 */
//require({},['http://localhost/scripts/configjs.groovy','require','../lib/util'], function (c, require,util) {
require({}, ['../config'], function (c) {
    require(['taskmng/layout']);
}, function (err) {
    console.log("load module err: " + err);
});