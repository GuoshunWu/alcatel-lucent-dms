<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page contentType="text/html;charset=utf-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title><s:text name="title"/></title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <meta http-equiv="Pragma" content="no-cache"/>

    <link rel="stylesheet" type="text/css" href="css/login.css"/>
    <%@include file="common/env.jsp" %>
    <script type="text/javascript" src="js/lib/require.js"></script>
    <script type="text/javascript">
        //Load common code that includes config, then load the app
        //logic for this page. Do the require calls here instead of
        //a separate file so after a build there are only 2 HTTP
        //requests instead of three.

        require(['./js/config'], function (config) {
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
        <s:form id="loginForm" name="loginForm" validate="false" theme="simple" namespace="/login" action="login"
                method="post">
            <table align='center'>
                <tr>
                    <td><strong><s:label for="idLoginname"><s:text name="login.loginname"/></s:label></strong></td>
                    <td><s:textfield name="loginname" id="idLoginname" size="25" maxlength="40"/></td>
                </tr>
                <tr>
                    <td><strong><s:label for="idPassword"><s:text name="login.password"/></s:label></strong></td>
                    <td><s:password name="password" id="idPassword" size="25" maxlength="40"/></td>
                </tr>
                <tr>
                    <td colspan="2"/>
                </tr>
                <tr>
                    <td colspan="2">
                        <s:submit value="%{getText('login.login')}" cssClass="button"/>
                        <s:reset value="%{getText('login.reset')}" cssClass="button"/>
                    </td>
                </tr>
            </table>
        </s:form>
    </div>
    <div style="height:80px;overflow: hidden;">
        <img src="images/login/LoginBottom.gif" alt=""/>
    </div>
    <div style="height:18px">
        <div style="float: right; font-size: 13px; font-weight:bold; padding-right:5px"><s:property
                value="#version"/></div>
    </div>
</div>
</body>
</html>