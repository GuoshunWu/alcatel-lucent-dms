<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%--
  Created by IntelliJ IDEA.
  User: guoshunw
  Date: 12-8-7
  Time: 上午11:00
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<c:set scope="page" var="pageTitle">Application Management</c:set>

<title>DMS->${pageTitle}</title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<meta http-equiv="Pragma" content="no-cache">

<link rel="stylesheet" type="text/css" href="css/themes/base/jquery.ui.base.css">
<link rel="stylesheet" type="text/css" href="css/themes/base/jquery.ui.all.css">

<link rel="stylesheet" type="text/css" href="css/layout-default-latest.css">
<link rel="stylesheet" type="text/css" media="screen" href="css/jqgrid/ui.jqgrid.css"/>

<style type="text/css">
        /* Using an 'optional-container' instead of 'body', so need body to have a 'height' */
    html, body {
        width: 100%;
        height: 100%;
        min-height: 100%;
        padding: 0 0 20px;
        margin: 0;

        /*font-family: "Lucida Grande", "Arial", "Helvetica", "Verdana", "sans-serif";*/
        font-family: "Arial", "Helvetica", "Verdana", "sans-serif";
        font-size: 10px;

        overflow: hidden !important;
    }

    #optional-container {
        /*margin-top: 2%;*/
        /*margin-left: 2%;*/
        width: 100%;
        height: 100%;

    }

    .ui-layout-center {
        overflow: hidden;
    }
</style>

<script type="text/javascript" src="js/jquery-1.7.2.min.js"></script>
<script type="text/javascript" src="js/jquery-ui-1.8.22.custom.min.js"></script>
<%--<script type="text/javascript" src="js/themeswitchertool.js"></script>--%>


<script type="text/javascript" src="js/i18n/grid.locale-en.js"></script>
<script type="text/javascript" src="js/jquery.jqGrid.min.js"></script>

<script type="text/javascript" src="js/jquery.jstree.js"></script>
<script type="text/javascript" src="js/jquery.cookie.js"></script>
<script type="text/javascript" src="js/jquery.hotkeys.js"></script>

<script type="text/javascript" src="js/jquery.layout-latest.js"></script>

<script type="text/javascript" src="js/dms-util.js"></script>

<script type="text/javascript">
var pageLayout;


$(function () {

    pageLayout = $('#optional-container').layout({
        resizable:true, closable:true
//           ,spacing_open: 0
//           ,north__paneSelector:	"#page_header"	// page header


    });

    var pageStatus = {};

    var dmsPanels = $('#ui_center').find("div[id^='DMS']");
    dmsPanels.addClass("ui-layout-content ui-corner-bottom"); //ui-widget-content
    dmsPanels.css({paddingBottom:'1em', borderTop:0});
    pageStatus.selectedPanel = showCenterPanel('DMS_welcomePanel');

    function showCenterPanel(panelId) {
        dmsPanels.each(function (index, element) {
            if ($(this).attr('id') == panelId) {
                pageStatus.selectedPanel = $(this).show();
            } else {
                $(this).hide();
            }
        });
        return pageStatus.selectedPanel;
    }

    $(".header-footer").hover(
            function () {
                $(this).addClass('ui-state-hover');
            }
            , function () {
                $(this).removeClass('ui-state-hover');
            }
    );

//
    //load init treeData from this url
//            var url= 'json/tree.json';
    var url = 'rest/products';
    url += '?nocache=' + new Date().getTime();

    $.getJSON(url, {}, function (data, textStatus, jqXHR) {
        //data is the tree initialize data from server
        //appTree
        $('#appTree').jstree({
            json_data:{
                data:data
            },
            plugins:[ "themes", "json_data", "ui", "core"]
        }).bind("select_node.jstree", function (event, data) {
                    var appTree = $.jstree._reference("#appTree");
                    var text = appTree.get_text(data.rslt.obj);
                    var parent = appTree._get_parent(data.rslt.obj);

                    var id = data.rslt.obj.attr("id");
                    pageStatus.selectedProductBase = {id:id, name:text};
                    if (-1 == parent) { //it is a Product
                        pageStatus.selectedPanel = showCenterPanel('DMS_productPanel');
                        $('#dispProductName', pageStatus.selectedPanel).text(text);
                        // query product version base on Product and its Id
                        $.getJSON('rest/products/' + id, {}, function (data, textStatus, jqXHR) {
                            var productVersions = $("#selVersion").get(0).options;
                            productVersions.length = 0;
                            $(data).each(function (index, product) {
                                productVersions.add(new Option(product.version, product.id));
                            });
                            pageStatus.selectedProduct = {id:productVersions[0].value, version:productVersions[0].text};

                            jQuery("#applicationGridList").jqGrid('setGridParam', {url:"rest/applications/" + pageStatus.selectedProduct.id, datatype:"json"}).trigger("reloadGrid");

                            var newCaption = 'Applications for Product ' + pageStatus.selectedProductBase.name + ' version ' + pageStatus.selectedProduct.version
                            jQuery("#applicationGridList").jqGrid('setCaption', newCaption);
                        });
                    } else { //it is a Application
                        pageStatus.selectedPanel = showCenterPanel('DMS_applicationPanel');
                        //TODO: Initialize Application panel elements
                    }
                });
    });

    // jqGrid
    $('#applicationGridList').jqGrid({
        url:'json/appgrid.json',
        datatype:'json',
        width:600,
        height:350,
        colNames:['ID', 'Application', 'Version', 'Dict. Num.'],
        colModel:[
            {name:'id', index:'id', width:55, align:'center', hidden:true},
            {name:'name', index:'name', width:100, align:'center'},
            {name:'version', index:'version', width:90, align:'center'},
            {name:'dictNum', index:'dictNum', width:80, align:'center'}
        ],
        pager:'#pager',
        rowNum:10,
        rowList:[10, 20, 30],
        sortname:'name',
        sortorder:'asc',
        viewrecords:true,
        gridview:true,
        caption:'Applications for Product'
//                ,loadonce: true
    });
    jQuery("#applicationGridList").jqGrid('navGrid', '#pager', {edit:true, add:true, del:true});

//      create all the dialogs
    $('#newProductDialog').dialog({
        autoOpen:false,
        height:200,
        width:400,
        modal:true,
        buttons:{
            'OK':function () {
                url = 'app/create-product';
                var productName = {'name':$('#productName').val()};
                $.post(url, productName, function (data, textStatus, jqXHR) {
                    if (data.status != 0) {
                        alert(data.message);
                        return;
                    }
                    //create success.
                    // $("#appTree").jstree("create_node", -1, "last", {data: productName.name,attr:{id:data.id}});
                    $.jstree._reference("#appTree").create_node(-1, "last", {data:productName.name, attr:{id:data.id}});
                });

                $(this).dialog("close");
            },
            'Cancel':function () {
                $(this).dialog("close");
            }
        },
        close:function () {
            //alert('Are you sure?');
        }
    });

    $('#newProductReleaseDialog').dialog({
        autoOpen:false,
        height:200,
        width:400,
        modal:true,
        buttons:{
            'OK':function () {
//                    url = 'app/create-product';
//                    var productName = {'name':$('#productName').val()};
//                    $.post(url, productName, function (data, textStatus, jqXHR) {
//                        if (data.status != 0) {
//                            alert(data.message);
//                            return;
//                        }
//                        //create success.
//                        // $("#appTree").jstree("create_node", -1, "last", {data: productName.name,attr:{id:data.id}});
//                        $.jstree._reference("#appTree").create_node(-1, "last", {data:productName.name, attr:{id:data.id}});
//                    });

                $(this).dialog("close");
            },
            'Cancel':function () {
                $(this).dialog("close");
            }
        },
        close:function () {
            //alert('Are you sure?');
        }
    });

    //  create all the buttons;
    $("#newProduct").button().click(function () {
        $("#newProductDialog").dialog("open");
    });

    $("#newApp").button().click(function () {
        alert("to be implemented.");
    });
    $("#addApp").button().click(function () {
        alert("to be implemented.");
    });
    $("#newApp").button().click(function () {
        alert("to be implemented.");
    });
    $("#removeApp").button().click(function () {
        alert("to be implemented.");
    });
    $("#download").button().click(function () {
        alert("to be implemented.");
        jQuery("#applicationGridList").jqGrid('setGridParam', {url:"http://localhost:2000/" + pageStatus.selectedProduct.id, datatype:"json"}).trigger("reloadGrid");

    });

    $("#selVersion").change(function (event) {
//                $(this).find("option:selected").each(function () {});
        pageStatus.selectedProduct = {version:$(this).find("option:selected").text(), id:$(this).val()};
        log(pageStatus);
        //TODO: update Application Grid according to pageStatus
//            jQuery("#applicationGridList").jqGrid('setGridParam', {url:"json/appgrid1.json", datatype:"json"}).trigger("reloadGrid");
        jQuery("#applicationGridList").jqGrid('setGridParam', {url:"rest/applications/" + pageStatus.selectedProduct.id, datatype:"json"}).trigger("reloadGrid");

        var newCaption = 'Applications for Product ' + pageStatus.selectedProductBase.name + ' version ' + pageStatus.selectedProduct.version
        jQuery("#applicationGridList").jqGrid('setCaption', newCaption);
    });

    $("#newVersion").button({
        text:false,
        icons:{
            primary:"ui-icon-plus"
        }
    }).click(function () {
                //todo: implement new product release.
                $("#newProductReleaseDialog").dialog("open");
            });

});

</script>
</head>
<body>

<!--[if IE 5]>
<div id="ie5" class="ie"><![endif]-->
<!--[if IE 6]>
<div id="ie6" class="ie"><![endif]-->
<!--[if IE 7]>
<div id="ie7" class="ie"><![endif]-->

<%-- All the dialogs here --%>
<div id="newProductDialog" title="New product">
    <span> Product name<input id="productName" value="" type="text"></span>
</div>
<div id="newProductReleaseDialog" title="New product release">
    <span> Version name<input id="versionName" value="" type="text"></span>
    Duplicate all applications from a previous version<select id="dupVersion"></select>
</div>


<div id="optional-container">
    <div class="ui-layout-north" style="text-align: left">
        <span style="font-family:fantasy; font-size:14pt; font-style:normal; ">${pageTitle}</span>
    </div>

    <div id="ui_center" class="ui-layout-center">
        <div id="DMS_welcomePanel">
            <table align="center">
                <tr>
                    <td align="center">
                        <span style="font-size:30pt; font-style:normal;color: #6a5acd; ">WELCOME TO DMS</span>
                    </td>
                </tr>
                <tr>
                    <td><img src="images/Books.png"/></td>
                </tr>
            </table>
        </div>
        <div id="DMS_productPanel">
            Product: <span id="dispProductName"></span> <br/> <br/>
            Version: <select id="selVersion"></select>
            <button id="newVersion"></button>
            <br/><br/>

            <table border="0">
                <tr>
                    <td valign="top" rowspan="8">
                        <div id="applicationGrid">
                            <table id="applicationGridList">
                                <tr>
                                    <td/>
                                </tr>
                            </table>
                            <div id="pager"></div>
                        </div>
                    </td>
                    <td>
                        <button id="newApp">New App</button>
                    </td>
                </tr>
                <tr>
                    <td>&nbsp;</td>
                </tr>
                <tr>
                    <td>
                        <button id="addApp">Add App</button>
                    </td>
                </tr>
                <tr>
                    <td>&nbsp;</td>
                </tr>
                <tr>
                    <td>
                        <button id="removeApp">Remove App</button>
                    </td>
                </tr>
                <tr>
                    <td>&nbsp;</td>
                </tr>
                <tr>
                    <td>&nbsp;</td>
                </tr>
                <tr>
                    <td>
                        <button id="download">Download</button>
                    </td>
                </tr>
            </table>

        </div>

        <div id="DMS_applicationPanel">
            Application
        </div>

    </div>

    <div class="ui-layout-west">
        <p>&nbsp;</p>

        <div id="appTree" style="background-color: transparent;"></div>
        <p>&nbsp;</p>
        <button id="newProduct">New Product...</button>

    </div>

    <%--<div class="ui-layout-south"> South</div>--%>

</div>
<!--[if lte IE 7]></div><![endif]-->

</body>
</html>