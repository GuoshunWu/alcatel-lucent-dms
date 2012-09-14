/**
 * Created by IntelliJ IDEA.
 * User: Guoshun Wu
 * Date: 12-9-8
 * Time: 下午4:21
 * To change this template use File | Settings | File Templates.
 */
define(['jquery'], function ($) {
    var panelId = {
        welcomeId:'DMS_welcomePanel',
        productId:'DMS_productPanel',
        applicationId:'DMS_applicationPanel'
    }

    function refresh(){

    }

    //init panels
    $('#ui_center').children("div[id^='DMS']").addClass("ui-layout-content ui-corner-bottom").css({paddingBottom:'1em', borderTop:0});
    return {
        panels:{
            welcome:$('#'+panelId.welcomeId),
            product:$('#'+panelId.productId),
            application:$('#'+panelId.applicationId)
        }
    }
});