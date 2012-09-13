/**
 * Created by IntelliJ IDEA.
 * User: Guoshun Wu
 * Date: 12-9-10
 * Time: 下午5:05
 * To change this template use File | Settings | File Templates.
 */

//    all the application element id
var PANEL_PREFIX = 'DMS';

var ids = {
    panel:{
        welcome:PANEL_PREFIX + "_welcomePanel",
        product:PANEL_PREFIX + "_productPanel",
        application:PANEL_PREFIX + "_applicationPanel"
    },
    dialog:{
        new_product:'newProductDialog',
        new_product_release:'newProductReleaseDialog',
        new_or_add_application:'newOrAddApplicationDialog'
    },
    button:{
        new_product:'newProduct'
    },
    container:{
        page:'optional-container',
        center:'ui_center'
    },
    navigateTree:'appTree'
};

// all the application request url
var URL = {
//    product tree initialize data url
    navigateTree:'rest/products?nocache=' + new Date().getTime(),
//    create product url
    create_product:'app/create-product',
//    get product by it id url, append product id to this url
    get_product_by_base_id: 'rest/products/',
//    get applications in application base by application id
    get_application_in_base_by_app_id:'rest/applications/appssamebase/',
//    get application in product by product id
    get_application_by_product_id:'rest/applications/'

};

var exports = {
    layout:{
        showCenterPanel:null
    },
    product_panel:{
        refresh:null,
        select_product_version_id:null
    },
    application_panel:{
        refresh:null
    },
    application_grid:{
        application_grid_id:null
    }
};