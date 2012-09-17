/**
 * Created by IntelliJ IDEA.
 * User: SYSTEM
 * Date: 12-9-12
 * Time: 下午10:24
 * To change this template use File | Settings | File Templates.
 */
//require({},['http://localhost/scripts/configjs.groovy','require','../lib/util'], function (c, require,util) {
require({}, ['../config'], function (c) {
    var dependencies = convertDependencies('transmng', ['cs!layout', 'cs!grid.colmodel', 'cs!trans_grid']);
//    console.log(dependencies);
    require(dependencies);
}, function (err) {
    console.log("load module err: " + err);
});