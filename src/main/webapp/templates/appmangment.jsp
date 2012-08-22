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
        html, body{
            height:100%;
            width:100%;
            margin:0;padding:0;overflow: hidden;
            font-size: 80%;
        }
        #header{height:5%;}
        #splitterContainer{
            min-height: 300px;
            height: 100%;
            max-height: 600px;

            min-width: 300px;
            width: 100%;

            border-top: solid 1px #aaa;
            border-bottom: solid 1px #aaa;
        }
        #splitterContainer div{
            /*overflow: auto;*/
        }
        .vsplitbar{
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
                outline: true,
                sizeLeft: 200,
                minLeft: 100,
                minRight: 100,
                dock: "left",
                dockSpeed: 200,
                dockKey: 'Z',	// Alt-Shift-Z in FF/IE
                accessKey: 'I',	// Alt-Shift-I in FF/IE
                resizeToWidth: true
            });

            //appTree
            $('#appTree').jstree({
                "json_data":{
                    "data":[
                        {
                            "data":"ISC",
                            "children":[
                                {
                                    "data":"R6.5",
                                    "children":["app1", "app2"]
                                },
                                "R6.6"
                            ]
                        },
                        {
                            "data":"New product..."
                        }
                    ]
                },
                plugins:[ "themes", "json_data", "ui"]
            });

            $('#dictGrid').jqGrid({
                url:'xml/testJQGrid.xml',
                datatype:'xml',
                mtype:'Get',
                colNames:['   ', 'Dictionary', 'Format', 'Encoding', 'Labels', 'Action'],
                colModel:[
                    {name:'invid', index:'invid', width:55},
                    {name:'invdate', index:'invdate', width:90},
                    {name:'amount', index:'amount', width:80, align:'right'},
                    {name:'tax', index:'tax', width:80, align:'right'},
                    {name:'total', index:'total', width:80, align:'right'},
                    {name:'note', index:'note', width:150, sortable:false}
                ],
                pager:'#dictGridPager',
                rowNum:10,
                rowList:[10, 20, 30],
                sortname:'invid',
                sortorder:'desc',
                viewrecords:true,
                gridview:true,
                caption:'Dictionary: XXXXXX'
            });
        });
    </script>
</head>
<body>
<div id="header">
    <h2>${pageTitle}</h2>
</div>

<div id="splitterContainer">
    <div id="leftPane">
        <div id="appTree" style="background-color: transparent;"></div>
    </div>
    <!-- #leftPane -->
    <div id="rightPane">
        <div style="height:5%;background:#bac8dc">Toolbar</div>
        <div style="padding:30px;">
            <form action="">
                <table width="100%">
                    <tr>
                        <td>Product:</td>
                        <td><input name="product" value="ISC"></td>
                        <td>Release:</td>
                        <td><input name="relase" value="6.6"></td>
                    </tr>
                    <tr>
                        <td>Application:</td>
                        <td><input name="product"></td>
                    </tr>
                    <tr>
                        <td>Deliver app dictionary</td>
                        <td><input name="dictName" size="50">
                            <input type="submit" value="Upload..."/>
                        </td>
                    </tr>
                </table>
            </form>

            <table id="dictGrid">
                <tr>
                    <td/>
                </tr>
            </table>
            <div id="dictGridPager"></div>

        </div>
    </div>
</div>
</body>
</html>