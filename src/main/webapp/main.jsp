<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page contentType="text/html;charset=utf-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<html xmlns="http://www.w3.org/1999/xhtml">

<head>
    <s:set var="base" scope="page"><s:url value="/"/></s:set>
    <title><s:text name="title"/></title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <meta http-equiv="Pragma" content="no-cache"/>

    <%@include file="common/env.jsp" %>
    <link rel="stylesheet" type="text/css" href="${base}css/main.css?v=<s:property value="buildNumber"/>"/>
    <script type="text/javascript" src="js/lib/require.js"></script>
    <script type="text/javascript">
        require(['./js/config.js?bust=' + new Date().getTime(), 'js/lib/domReady'], function (config, domReady) {
            domReady(function(){
                require(['main']);
            });
        });
    </script>

</head>
<body>

<%@include file="common/maskdiv.jsp" %>
<div id="global-container">
    <div class="ui-layout-north">
        <%@include file="common/navigator.jsp" %>
    </div>
    <div id="ui_center" class="ui-layout-center">
        <div class="ui-layout-content">
            <%@include file="appmanagement/appmng.jsp" %>
            <%@include file="transmanagement/transmng.jsp" %>
            <%@include file="taskmanagement/taskmng.jsp" %>
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