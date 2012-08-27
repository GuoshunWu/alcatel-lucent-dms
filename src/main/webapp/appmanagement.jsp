<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%--
  Created by IntelliJ IDEA.
  User: guoshunw
  Date: 12-8-7
  Time: 上午11:00
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <c:set scope="page" var="pageTitle">Application Management</c:set>

    <title>DMS->${pageTitle}</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta http-equiv="Pragma" content="no-cache">

    <link rel="stylesheet" type="text/css" media="screen" href="css/themes/ui-lightness/jquery-ui-1.8.22.custom.css"/>
    <link rel="stylesheet" type="text/css" media="screen" href="css/jqgrid/ui.jqgrid.css"/>

    <style type="text/css" media="all">
        html, body {
            height: 100%;
            width: 100%;
            margin: 0;
            padding: 0;
            overflow: hidden;
            font-size: 80%;
        }

        #header {
            height: 5%;
        }

        #splitterContainer {
            min-height: 300px;
            height: 100%;
            max-height: 600px;

            min-width: 300px;
            width: 100%;

            border-top: solid 1px #aaa;
            border-bottom: solid 1px #aaa;
        }

        .vsplitbar {
            width: 5px;
            background: #aaa;
        }

    </style>

    <script type="text/javascript" src="js/jquery-1.7.2.min.js"></script>
    <script type="text/javascript" src="js/jquery-ui-1.8.22.custom.min.js"></script>

    <script type="text/javascript" src="js/i18n/grid.locale-en.js"></script>
    <script type="text/javascript" src="js/jquery.jqGrid.min.js"></script>

    <script type="text/javascript" src="js/jquery.jstree.js"></script>
    <script type="text/javascript" src="js/jquery.cookie.js"></script>
    <script type="text/javascript" src="js/jquery.hotkeys.js"></script>

    <script type="text/javascript" src="js/splitter-152.js"></script>

    <script type="text/javascript">
        $(function () {
            $("#splitterContainer").splitter({
                type:'v',
                outline:true,
                sizeLeft:200,
                minLeft:100,
                minRight:100,
                dock:"left",
                dockSpeed:200,
                dockKey:'Z', // Alt-Shift-Z in FF/IE
                accessKey:'I', // Alt-Shift-I in FF/IE
                resizeToWidth:true
            });

            //load init treeData from this url
            var url= 'rest/products';
//            var url= 'json/tree.json';

            $.getJSON(url, {}, function (data, textStatus, jqXHR) {
                //data is the tree initialize data from server
                //appTree
                var productsTree=$('#appTree').jstree({
                    json_data:{
                        data:data
//                    ajax:{
//                        url:"get about to open node data url",
////                        data:function(node){
////                            return { id:node.attr ? node.attr("id") : 0 };
////                        },
//                        error:function(XMLHttpRequest, textStatus, errorThrown){
//                            alert("error: "+errorThrown);
//                        },
//                        success:function(data, textStatus){
//                            alert("success" + data.toString());
//                        }
//                    }
                    },
                    plugins:[ "themes", "json_data", "ui"]
                });
                productsTree.bind("select_node.jstree", function (event, data) {
                            alert(data.rslt.obj.attr("id"));
                        });
            });

            $('#newProductDialog').dialog({
                autoOpen:false,
                height:150,
                width:400,
                buttons:{
                    'OK':function () {
                        url='app/create-product';
                        var productName={'name':$('#productName').val()}
                        alert(productName.name);
                        $.post(url,productName,function(data, textStatus, jqXHR){
                            alert(data.message);
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

            $("#createProduct").button()
                    .click(function () {
                        $("#newProductDialog").dialog("open");
                    });

        });
    </script>
</head>
<body>
<div id="header">
    <h2>${pageTitle}</h2>
</div>

<div id="splitterContainer">
    <div id="leftPane" style="padding-top: 15px">
        <table align="center" border="0">
            <tr>
                <td>
                    <div id="appTree" style="background-color: transparent;"></div>
                </td>
            </tr>
            <tr>
                <td>&nbsp;</td>
            </tr>
            <tr>
                <td>
                    <div id="newProductDialog" title="New product">
                        <form>
                            <table align='center' border="0" width="100%">
                                <fieldset>
                                    <tr>
                                        <td width='30%'>Product name</td>
                                        <td><input id="productName" value="" type="text" SIZE="40"></td>
                                    </tr>
                                </fieldset>
                            </table>
                        </form>
                    </div>

                    <button id="createProduct">New Product...</button>
                </td>
            </tr>
        </table>
    </div>
    <!-- #leftPane -->
    <div id="rightPane" style="padding-top: 15px">
        <table align="center">
            <tr>
                <td align="center">
                    <h1>Welcome to DMS</h1>
                </td>
            </tr>
            <tr>
                <td><img src="images/Books.png"/></td>
            </tr>
        </table>
    </div>
</div>
</body>
</html>