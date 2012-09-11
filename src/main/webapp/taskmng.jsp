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
    <%--<script type="text/javascript" src="js/appmng.js"></script>--%>
    <script type="text/javascript" src="js/appmng/global.js"></script>
    <script type="text/javascript" src="js/coffee-script.js"></script>
    <script type="text/javascript" src="js/themeswitchertool.js"></script>

</head>
<body>

<!--[if IE 5]>
<div id="ie5" class="ie"><![endif]-->
<!--[if IE 6]>
<div id="ie6" class="ie"><![endif]-->
<!--[if IE 7]>
<div id="ie7" class="ie"><![endif]-->


<div id="optional-container">
    <div class="ui-layout-north" style="text-align: left; bottom:0px">
        <%@include file="taskmanagement/northpanel.jsp"%>
    </div>

    <div id="ui_center" class="ui-layout-center">
    Center
    </div>

    <%--<div class="ui-layout-west"></div>--%>
    <%--<div class="ui-layout-south"> South</div>--%>

</div>
<!--[if lte IE 7]></div><![endif]-->

</body>
</html>