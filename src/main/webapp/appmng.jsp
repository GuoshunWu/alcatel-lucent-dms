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

    <link rel="stylesheet" type="text/css" href="css/appmanagement.css">

    <script type="text/javascript" src="js/jquery-1.7.2.min.js"></script>
    <script type="text/javascript" src="js/jquery-ui-1.8.22.custom.min.js"></script>

    <script type="text/javascript" src="js/dms-util.js"></script>

    <script type="text/javascript" src="js/i18n/grid.locale-en.js"></script>
    <script type="text/javascript" src="js/jquery.jqGrid.min.js"></script>

    <script type="text/javascript" src="js/jquery.jstree.js"></script>
    <script type="text/javascript" src="js/jquery.cookie.js"></script>
    <script type="text/javascript" src="js/jquery.hotkeys.js"></script>

    <script type="text/javascript" src="js/jquery.layout-latest.js"></script>

    <script type="text/javascript" src="js/jquery.easy-confirm-dialog.js"></script>
    <script type="text/javascript" src="js/jquery.msgBox.v1.js"></script>

    <script type="text/javascript" src="js/combobox.js"></script>
    <script type="text/javascript" src="js/appmng.js"></script>

    <%--<script type="text/javascript" src="js/themeswitchertool.js"></script>--%>

    <script type="text/javascript"
            src="http://jqueryui.com/themeroller/themeswitchertool/">
    </script>
    <script type="text/javascript">
        $(document).ready(function () {
            $('#switcher').themeswitcher();
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
<%@include file="appmanagement/dialogs.html" %>


<div id="optional-container">
    <div class="ui-layout-north" style="text-align: left; bottom:0px">
        <span style="font-family:fantasy; font-size:14pt; font-style:normal; ">${pageTitle}</span>
        <span><div id="switcher"></div></span>
    </div>

    <div id="ui_center" class="ui-layout-center">
        <%@include file="appmanagement/welcome_panel.html" %>
        <%@include file="appmanagement/product_panel.html" %>
        <%@include file="appmanagement/application_panel.html" %>
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