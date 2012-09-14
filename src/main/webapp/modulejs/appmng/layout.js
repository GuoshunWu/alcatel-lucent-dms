/**
 * Created by IntelliJ IDEA.
 * User: Guoshun Wu
 * Date: 12-9-8
 * Time: 下午2:40
 * To change this template use File | Settings | File Templates.
 */


define(['jquery', 'jquery.layout-latest.min','appmng/dialogs'], function ($, jqlayout,dialogs) {
    //private variable
    var panelId = {
        welcome:'DMS_welcomePanel',
        product:'DMS_productPanel',
        application:'DMS_applicationPanel'
    }
    var pageLayout = $('#optional-container').layout({
        resizable:true,
        closable:true
    });

    function initProductPanel() {
        //init elements in ProductPanel
        //buttons

        $("#newVersion").button({
            text:false,
            icons:{
                primary:"ui-icon-plus"
            }
        }).click(function () {
                dialogs.new_product_release.dialog("open");
//                $("#newProductReleaseDialog").dialog("open");
//                var productVersions = $("#selVersion")[0].options;
//                var newVersions = $("#dupVersion")[0].options;
//
//                newVersions.length = 0;
//                newVersions.add(new Option("", -1));
//                var opt, newOpt;
//                for (var i = 0; i < productVersions.length; ++i) {
//                    opt = productVersions[i];
//                    newOpt = new Option(opt.text, opt.value);
//                    if (i == productVersions.length - 1) {
//                        newOpt.selected = true;
//                    }
//                    newVersions.add(newOpt);
//                }
            });

        //  create all the buttons;
        $("#newProduct").button().click(function () {
            $("#newProductDialog").dialog("open");
        });

        $("#newApp").button().click(function () {
            $("<input>").insertAfter($(this));
            alert("to be implemented.");
        });
        $("#addApp").button().click(function () {
            alert("test trigger");
        });
        $("#removeApp").button().click(function () {
            alert("to be implemented.");
        });
        $("#download").button().click(function () {
            alert("to be implemented.");
            jQuery("#applicationGridList").jqGrid('setGridParam', {url:"http://localhost:2000/" + pageStatus.selectedProduct.id, datatype:"json"}).trigger("reloadGrid");

        });
        //selected
        $("#selVersion").change(function (e, param) {
//            jQuery("#applicationGridList").jqGrid('setGridParam', {url:"rest/applications/" + param.product.id, datatype:"json"}).trigger("reloadGrid");
            var caption = 'Applications for Product '+ param.productBase.name + ' version ' + param.product.version;
//            jQuery("#applicationGridList").jqGrid('setCaption', caption);
        });
    }

    //init panels
    var dmsPanels = $('#ui_center').children("div[id^='DMS']").addClass("ui-layout-content ui-corner-bottom").css({paddingBottom:'1em', borderTop:0});
    //init productPanel
    initProductPanel();


    function refreshProductPanel(param) {
        $('#dispProductName', '#' + panelId.product).html(param.productBase.name);
        // query product version
        $.getJSON('rest/products/' + param.productBase.id, {}, function (data, textStatus, jqXHR) {
            $("#selVersion").empty();
            $(data).map(
                function () {
                    return new Option(this.version, this.id)
                }).appendTo($("#selVersion"));
            $("#selVersion").trigger("change", {
                product:{
                    id:$("#selVersion").val(),
                    version:$("#selVersion").children(":selected").text()
                },
                productBase:{
                    id:param.productBase.id,
                    name:param.productBase.name
                }
            });
        });
    }

    function refreshApplicationPanel(param) {

    }

    var showCenterPanel = function (panelId) {
        var switchedPanel = undefined;
        dmsPanels.each(function (index, element) {
            if ($(this).attr('id') == panelId) {
                switchedPanel = $(this).show();
            } else {
                $(this).hide();
            }
        });
        return switchedPanel;
    }

    showCenterPanel('DMS_welcomePanel');
    return {
        showWelcomePanel:function () {
            showCenterPanel(panelId.welcome);
        },
        showProductPanel:function (param) {
            refreshProductPanel(param);
            showCenterPanel(panelId.product);
        },
        showApplicationPanel:function () {
            refreshApplicationPanel(param);
            showCenterPanel(panelId.application);
        },

        panelId:panelId
    };
});