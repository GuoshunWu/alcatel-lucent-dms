<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page contentType="text/html;charset=utf-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title><s:text name="title"/></title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <meta http-equiv="Pragma" content="no-cache"/>

    <%--<link rel="stylesheet" href="../css/jqueryUI/themes/base/jquery.ui.all.css">--%>

    <link rel="stylesheet" type="text/css" href="css/login.css"/>
    <link rel="stylesheet" type="text/css" href="css/jqformvalidator/validator.css"/>

    <%@include file="common/env.jsp" %>

    <script type="text/javascript" src="js/lib/require.js"></script>
    <script type="text/javascript">
        //Load common code that includes config, then load the app
        //logic for this page. Do the require calls here instead of
        //a separate file so after a build there are only 2 HTTP
        //requests instead of three.

        require(['./js/config.js?bust=' + new Date().getTime()], function (config) {
            require(['login/main']);
        }, function (err) {
            console.log("load module err: " + err);
        });
    </script>
</head>
<body>
<div id="login_area">
    <div style="height: 80px;background: url(images/login/LoginTop.gif) no-repeat center right;"></div>
    <div style="font-size: 20px;font-weight: bold;color: white;background: url(images/login/bar_alcatel.gif) 100%">
        <s:text name="title"/>
    </div>
    <img src="images/login/LoginMiddle.jpg" alt="picture"/>

    <div>
        <s:form id="loginForm" name="loginForm" validate="false" theme="simple" method="post" action='login'
                namespace='/'>
            <%--<s:token/>--%>
            <table border="0" align='center'>
                <tr>
                    <td align="right">
                        <label for="idLoginname"><s:text name="login.loginname"/></label></td>
                    <td><s:textfield name="loginname" id="idLoginname" size="25" maxlength="40"/></td>
                    <td style="width:120px">
                        <div id="idLoginnameTip">
                            <s:fielderror name="loginname"/>
                        </div>
                    </td>
                </tr>
                <tr>
                    <td align="right">
                        <label for="idPassword"><s:text name="login.password"/></label>
                    </td>
                    <td><s:password name="password" id="idPassword" size="25" maxlength="40"/></td>
                    <td>
                        <div id="idPasswordTip">
                            <s:fielderror name="password"/>
                        </div>
                    </td>
                </tr>
                <tr>
                    <td colspan="3"></td>
                </tr>
                <tr>
                    <td colspan="3">
                        <s:submit id="idSubmit" value="%{getText('login.login')}" cssClass="button"/>
                        <s:reset value="%{getText('login.reset')}" cssClass="button"/>
                    </td>
                </tr>
                <tr>
                    <td colspan="3">
                        <div id="loginStatus" style="color:red">
                            <s:actionerror/>
                        </div>
                    </td>
                </tr>
            </table>
        </s:form>
    </div>
    <div style="height:80px;overflow: hidden;">
        <img src="images/login/LoginBottom.gif" alt=""/>
    </div>
    <div style="height:18px">
        <div style="float: left;font-size: 13px">
            &nbsp;<a href="<%=request.getContextPath()%>/manual/Dictionary Management System - User Guide.doc"
                     target="_blank" style="text-decoration:none"><img
                src="<%=request.getContextPath()%>/images/help.png" alt="" style="border: 0"/>&nbsp;<b>User
            Guide</b></a>
        </div>
        <div style="float: left;font-size: 13px">
            &nbsp;<a href="<%=request.getContextPath()%>/release_notes.txt"
                     target="_blank" style="text-decoration:none">
            <img src="<%=request.getContextPath()%>/images/release_notes.gif" alt="" style="border: 0"/>
            <b>Release Notes</b></a>
        </div>
        <div style="float: right; font-size: 13px; font-weight:bold; padding-right:5px">
            V<span id='version'></span>&nbsp;Build&nbsp;
            <span id="buildNumber"></span>
        </div>
    </div>

    <div id="testForDialog"></div>
</div>
</body>
</html>