/**
 * Created by IntelliJ IDEA.
 * User: SYSTEM
 * Date: 12-9-12
 * Time: 下午10:24
 * To change this template use File | Settings | File Templates.
 */

require(['../config', '../lib/util'], function (config,util) {
    var dependencies=util.getDependencies('transmng',['layout','grid.colmodel','trans_grid']);
//    console.log(dependencies);
    require(dependencies);
});