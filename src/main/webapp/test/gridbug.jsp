<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@page contentType="text/html; charset=utf-8" %>
<%--
 This page is used for demonstrate two jqgrid bugs.
 First: Both use grouping column and frozen
  scenario: if column number is an odd number and the no grouping column are frozen, the the grouping will be invalid.
 Second: Both use grouping and ondblClickRow
  scenario: frozen column can't find ondblCllickRow event handler

--%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html;charset=utf-8"/>

    <style type="text/css">
        .html, body {
            font-family: 'Verdana', '宋体';
            font-size: 10px;
        }
    </style>

    <link rel="stylesheet" type="text/css" href="../css/jqueryUI/themes/base/jquery.ui.all.css">
    <link rel="stylesheet" type="text/css" href="../css/jqgrid/ui.jqgrid.css"/>

    <script type="text/javascript" src="../js/lib/jquery-1.7.2.min.js"></script>
    <script type="text/javascript" src="../js/lib/i18n/grid.locale-en.js"></script>
    <script type="text/javascript" src="../js/lib/jquery.jqGrid.min.js"></script>
    <%--<script type="text/javascript" src="js/lib/ui.multiselect.js"></script>--%>

    <script type="text/javascript">

        $(function () {
            var transGrid = $("#transGridList").jqGrid({
                url:'/json/gridbug.json',
                mtype:'GET',
                postData:{},
                editurl:"",
                datatype:'json',
                width:$(window).width() * 0.95,
                height:300,
                shrinkToFit:false,
                rownumbers:true,
                loadonce:false,
                pager:'#taskPager',
                rowNum:10,
                rowList:[10, 20, 30],
                sortname:'name',
                sortorder:'asc',
                viewrecords:true,
                gridview:true,
                multiselect:true,
                multikey:"ctrlKey",
                caption:'Translation Task List',
                colNames:[
                    'A',
                    'B1', 'B2'
                ],
                colModel:[
                    {name:'a', index:'a', width:80, hidden:false, align:'center', frozen:true} ,
                    {name:'b1', index:'b1', width:30, align:'center'},
                    {name:'b2', index:'b2', width:30, editable:true, align:'center'}
                ],
                ondblClickRow:function (rowid, iRow, iCol, e) {
                    return alert("rowid:" + rowid + ", iCol:" + iCol + ", iRow: " + iRow + ", e," + e);
                }
            });
            transGrid.navGrid('#taskPager', {edit:true, add:true, del:false, search:false, view:false});
            transGrid.navButtonAdd("#taskPager", {
                caption:"Clear",
                title:"Clear Search",
                buttonicon:'ui-icon-refresh',
                position:'first',
                onClickButton:function () {
                    return transGrid[0].clearToolbar();
                }
            });
            transGrid.setGroupHeaders({
                useColSpanStyle:true,
                groupHeaders:[
                    {startColumnName:'b1', numberOfColumns:2, titleText:'<bold>B</bold>'}
                ]
            });

            transGrid.setFrozenColumns();
        });

    </script>
</head>
<body>
<h1 align="center"> example</h1>
<table id="transGridList">
    <tr>
        <td></td>
    </tr>
</table>
<div id="taskPager"/>
</body>
</html>