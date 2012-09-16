/**
 * Created by IntelliJ IDEA.
 * User: Guoshun Wu
 * Date: 12-9-14
 * Time: 下午12:08
 * To change this template use File | Settings | File Templates.
 */

require(['../config'], function (c) {
//    require(convertDependencies('appmng', ['largemodule']));
    require(convertDependencies('appmng', ['cs!layout', 'cs!dialogs', 'cs!apptree'
        , 'cs!product_panel', 'cs!application_grid']));
});