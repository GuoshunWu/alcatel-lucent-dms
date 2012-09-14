/**
 * Created by IntelliJ IDEA.
 * User: Guoshun Wu
 * Date: 12-9-8
 * Time: 下午2:26
 * To change this template use File | Settings | File Templates.
 */

define(['jquery','appmng/layout', 'jquery.jstree'],
    function ($,layout, jstree) {

        //private variable
        var tree;

        //load init treeData from this url
//            var url= 'json/tree.json';
        var url = 'rest/products?nocache=' + new Date().getTime();

        $.getJSON(url, {}, function (data, textStatus, jqXHR) {
            //data is the tree initialize data from server
            //appTree
            $.jstree._themes = "css/jstree/themes/";

            tree = $('#appTree').jstree({
                json_data:{
                    data:data
                },
                core:{
//                animation:500
                },
                ui:{
                    select_limit:1
                },
                themes:{
                  url:false
//                theme:'default-rtl'
//                theme:'apple'
//                theme:'classic'
                },
                plugins:[ "themes", "json_data", "ui", "core"]
            });
            tree.bind("select_node.jstree", function (event, data) {
                var appTree = $.jstree._reference("#appTree");
                var text = appTree.get_text(data.rslt.obj);
                var parent = appTree._get_parent(data.rslt.obj);
                var id = data.rslt.obj.attr("id");

                if (-1 == parent) { //it is a Product
                    layout.showProductPanel({
                        productBase:{
                            id:id,
                            name: text
                        }
                    });



                } else { //it is a Application
                    layout.showApplicationPanel();
//                    pageStatus.selectedPanel = showCenterPanel('DMS_applicationPanel');
//                    pageStatus.selectedProductBase = {id:parent.attr("id"), name:appTree.get_text(parent)};
//                    //TODO: Initialize Application panel elements
//                    $('#appDispProductName', pageStatus.selectedPanel).text(pageStatus.selectedProductBase.name);
//                    $('#appDispAppName', pageStatus.selectedPanel).text(text);
//
//                    // query application version
//                    var url = 'rest/applications/apps/' + id;
//                    $.getJSON(url, {}, function (data, textStatus, jqXHR) {
//                        var productVersions = $("#selAppVersion").get(0).options;
//                        productVersions.length = 0;
//                        $(data).each(function (index, product) {
//                            productVersions.add(new Option(product.version, product.id));
//                        });
//                        // update dictGridList
////                        jQuery("#applicationGridList").jqGrid('setGridParam', {url:"rest/applications/" + pageStatus.selectedProduct.id, datatype:"json"}).trigger("reloadGrid");
////
////                        var newCaption = 'Applications for Product ' + pageStatus.selectedProductBase.name + ' version ' + pageStatus.selectedProduct.version
////                        jQuery("#applicationGridList").jqGrid('setCaption', newCaption);
//
//                    });
                }
            });

        });

        return tree;
    });