/**
 * Created by IntelliJ IDEA.
 * User: SYSTEM
 * Date: 12-9-12
 * Time: 下午10:24
 * To change this template use File | Settings | File Templates.
 */

requirejs.config({
    shim:{
        'backbone':{
            //These script dependencies should be loaded before loading
            //backbone.js
            deps:['underscore', 'jquery'],
            //Once loaded, use the global 'Backbone' as the
            //module value.
            exports:'Backbone'
        },
        'foo':{
            deps:['bar'],
            //A function can be used to generate the exported value.
            //"this" for the function will be the global object.
            //The dependencies will be passed in as function arguments.
            exports:function (bar) {
                //Using a function allows you to call noConflict for libraries
                //that support it. However, be aware that plugins for those
                //libraries may still want a global.
                return this.Foo.noConflict();
            }
        }
    }
});