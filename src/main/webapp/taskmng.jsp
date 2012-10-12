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

    <link rel="stylesheet" type="text/css" href="css/taskmanagement.css">
    <%@include file="common/env.jsp"%>
    <script type="text/javascript" data-main="js/taskmng/taskmng" src="js/require.js"></script>

</head>
<body>

<!--[if IE 5]>
<div id="ie5" class="ie"><![endif]-->
<!--[if IE 6]>
<div id="ie6" class="ie"><![endif]-->
<!--[if IE 7]>
<div id="ie7" class="ie"><![endif]-->

<%@include file="common/maskdiv.jsp"%>

<div id="optional-container">
    <div class="ui-layout-north" style="text-align: left; bottom:0px">
        <table width="100%" border="0">
            <tr>
                <td>
            <span style="font-family:fantasy; font-size:14pt; font-style:normal; ">
                <s:text name="taskmng.title"/>
            </span>
                </td>
                <td>
                    <div id="switcher"></div>
                </td>
                <td  align="right">
                    <%@include file="/common/pagenavigator.jsp" %>
                </td>
            </tr>
        </table>
    </div>

    <div id="ui_center" class="ui-layout-center">
        <%@include file="taskmanagement/centerpanel.jsp"%>
    </div>
    <%--<div class="ui-layout-west"></div>--%>
    <%--<div class="ui-layout-south"> South</div>--%>

</div>
<!--[if lte IE 7]></div><![endif]-->
</body>
</html>