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

    <LINK rel="stylesheet" type="text/css" href="css/themes/base/jquery.ui.all.css">
    <LINK rel="stylesheet" type="text/css" href="css/layout-default-latest.css">
    <link rel="stylesheet" type="text/css" media="screen" href="css/jqgrid/ui.jqgrid.css"/>

    <STYLE type="text/css">
            /* Using an 'optional-container' instead of 'body', so need body to have a 'height' */
        html, body {
            width: 100%;
            height: 100%;
            padding: 0;
            margin: 0;
            overflow: hidden !important;
        }

        #optional-container {
            /*width: 96%;*/
            /*height: 94%;*/
            /*margin-top: 2%;*/
            /*margin-left: 2%;*/
            width: 100%;
            height: 100%;
        }

        .ui-layout-center {
            overflow: hidden;
        }
    </STYLE>

    <script type="text/javascript" src="js/jquery-1.7.2.min.js"></script>
    <script type="text/javascript" src="js/jquery-ui-1.8.22.custom.min.js"></script>

    <script type="text/javascript" src="js/i18n/grid.locale-en.js"></script>
    <script type="text/javascript" src="js/jquery.jqGrid.min.js"></script>

    <script type="text/javascript" src="js/jquery.jstree.js"></script>
    <script type="text/javascript" src="js/jquery.cookie.js"></script>
    <script type="text/javascript" src="js/jquery.hotkeys.js"></script>

    <script type="text/javascript" src="js/jquery.layout-latest.js"></script>

    <script type="text/javascript" src="js/dms-util.js"></script>

    <script type="text/javascript">
        var myLayout;


        $(function () {
            var pageStatus={};

            var dmsPanels = $('#ui_center').find("div[id^='DMS']");
            dmsPanels.addClass("ui-layout-content ui-corner-bottom"); //ui-widget-content
            dmsPanels.css({paddingBottom:'1em', borderTop:0});
            pageStatus.selectedPanel=showCenterPanel('DMS_welcomePanel');

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
            myLayout = $('#optional-container').layout();
            //            addThemeSwitcher('.ui-layout-north',{ top: '13px', right: '20px' });

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
                })
                        .bind("loaded.jstree", function (event, data) {
//                            alert("tree loaded.");
                        })
                    // select node fires
                        .bind("select_node.jstree", function (event, data) {
                            var appTree = $.jstree._reference("#appTree");
                            var text = appTree.get_text(data.rslt.obj);
                            var parent = appTree._get_parent(data.rslt.obj);

                            var id = data.rslt.obj.attr("id");
                            pageStatus.selectedProductBase={id:id, name:text};
                            if (-1 == parent) { //it is a Product
                                pageStatus.selectedPanel = showCenterPanel('DMS_productPanel');
                                $('#dispProductName', pageStatus.selectedPanel).text(text);
                                // query product version base on Product and its Id
                                $.getJSON('rest/products/'+id,{},function(data, textStatus, jqXHR){
                                    var productVersions=$("#selVersion" ).get(0).options;
                                    productVersions.length=0;
                                    $(data).each(function(index, product){
                                        productVersions.add(new Option(product.version, product.id));
                                    });
                                    pageStatus.selectedProduct={id:productVersions[0].value, version:productVersions[0].text};
                                    //TODO: Initialize Application grid accord to pageStatus
                                });
                            } else { //it is a Application
                                pageStatus.selectedPanel = showCenterPanel('DMS_applicationPanel');
                                //TODO: Initialize Application panel elements
                            }


                        });
            });

            // jqGrid
            $('#applicationGridList').jqGrid({
                url:'xml/testJQGrid.xml',
                datatype:'xml',
                mtype:'Get',
                colNames:['Inv No', 'Date', 'Amount', 'Tax', 'Total', 'Notes'],
                colModel:[
                    {name:'invid', index:'invid', width:55},
                    {name:'invdate', index:'invdate', width:90},
                    {name:'amount', index:'amount', width:80, align:'right'},
                    {name:'tax', index:'tax', width:80, align:'right'},
                    {name:'total', index:'total', width:80, align:'right'},
                    {name:'note', index:'note', width:150, sortable:false}
                ],
                pager:'#pager',
                rowNum:10,
                rowList:[10, 20, 30],
                sortname:'invid',
                sortorder:'desc',
                viewrecords:true,
                gridview:true,
                caption:'My first grid'
            });

            $('#newProductDialog').dialog({
                autoOpen:false,
                height:200,
                width:400,
                modal: true,
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
            });

            $("#selVersion").change(function(event){
//                $(this).find("option:selected").each(function () {});
                pageStatus.selectedProduct={version:$(this).find("option:selected").text(),id:$(this).val()};
                log(pageStatus);
                //TODO: update Application Grid according to pageStatus
            });

            $("#newVersion").button( {
                text: false,
                icons: {
                    primary: "ui-icon-squared"
                }
            }).click(function() {
                alert( "to be implement.." );
            });
        });
    </script>
</head>
<body>
<div id="optional-container">
    <div class="ui-layout-north" style="text-align: left">
        <span style="font-family:fantasy; font-size:14pt; font-style:normal; ">${pageTitle}</span>
        <%--<BUTTON onClick="removeUITheme()">Remove Theme</BUTTON>--%>
        <%--&nbsp; &nbsp;--%>
        <%--<BUTTON onClick="myLayout.resizeAll(); myLayout.sizeContent('center');">Resize Content</BUTTON>--%>
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
            Version: <select id="selVersion" ></select><button id="newVersion"></button>
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
        <div id="newProductDialog" title="New product">
            <span> Product name<input id="productName" value="" type="text"></span>
        </div>
    </div>
    <%--<div class="ui-layout-south"> South</div>--%>

</div>

</body>
</html>