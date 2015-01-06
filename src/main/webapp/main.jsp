<!DOCTYPE html>
<%@ page contentType="text/html;charset=utf-8" %>
<%--<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>--%>
<%@ taglib prefix="s" uri="/struts-tags" %>

<html>

<head>
    <s:set var="base" scope="page"><s:url value="/"/></s:set>
    <title><s:text name="title"/></title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <!--[if IE]>
    <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
    <![endif]-->
    <%@include file="common/env.jsp" %>
    <link rel="stylesheet" type="text/css" href="${base}css/main.css?v=<s:property value="buildNumber"/>"/>
    <script type="text/javascript" data-main="js/entry" async="async" src="js/lib/require.js"></script>

</head>
<body>

<%@include file="common/maskdiv.jsp" %>
<%@include file="common/commonDialogs.jsp" %>

<div id="global-container">
    <div class="ui-layout-north">
        <%@include file="common/navigator.jsp" %>
    </div>
    <div id="ui_center" class="ui-layout-center">
        <div class="ui-layout-content" id="globalUILayoutContent">
            <%@include file="appmanagement/appmng.jsp" %>
            <%@include file="transmanagement/transmng.jsp" %>
            <%@include file="taskmanagement/taskmng.jsp" %>
            <%@include file="contextmanagement/contextmng.jsp" %>
            <%@include file="admin/admin.jsp" %>
        </div>
    </div>
    <div class="ui-layout-west">
        <div class="header">Navigation Tree</div>
        <div class="ui-layout-content">
            <div id="appTree" style="background-color: transparent;"></div>
        </div>
        <%--<div class="footer">A test footer</div>--%>
    </div>
</div>
</body>
</html>