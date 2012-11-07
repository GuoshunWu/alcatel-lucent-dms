<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page contentType="text/html;charset=utf-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<html  xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>Dictionary Management System</title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <meta http-equiv="Pragma" content="no-cache"/>

    <link rel="stylesheet" type="text/css" href="css/login.css"/>
    <%@include file="common/env.jsp"%>
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
    <h1>Hello</h1>
    <table>
        <tr>
            <td>Center</td>
        </tr>
    </table>
</body>
</html>