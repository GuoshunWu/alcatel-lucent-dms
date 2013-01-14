<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%--
  Created by IntelliJ IDEA.
  User: guoshunw
  Date: 12-8-7
  Time: 上午11:00
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=utf-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<html xmlns="http://www.w3.org/1999/xhtml">
<head>

    <title><s:text name="transmng.title"/></title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta http-equiv="Pragma" content="no-cache">

    <link rel="stylesheet" type="text/css" href="css/transmanagement.css">
    <%@include file="common/env.jsp" %>
    <script type="text/javascript" src="js/lib/require.js"></script>
    <script type="text/javascript">
        //Load common code that includes config, then load the app
        //logic for this page. Do the require calls here instead of
        //a separate file so after a build there are only 2 HTTP
        //requests instead of three.

        require(['./js/config.js?bust=' + new Date().getTime()], function (config) {
            require(['transmng/layout']);
        }, function (err) {
            console.log("load module err: " + err);
        });
    </script>


</head>
<body>

<%@include file="common/maskdiv.jsp" %>
<div id="optional-container">
    <div class="ui-layout-north">
        <%@include file="common/toppanel.jsp" %>
    </div>

    <div id="ui_center" class="ui-layout-center">
        <div class="content">
            <%@include file="transmanagement/summarypanel.jsp" %>
        </div>
    </div>

    <%@include file="transmanagement/dialogs.jsp" %>
</div>
</body>
</html>