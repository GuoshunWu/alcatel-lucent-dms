<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ taglib prefix="s" uri="/struts-tags" %>
<html  xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>Welcome to DMS</title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>

    <style type="text/css">
        html, body {
            margin: 0;
            padding: 0;
            font-size: 75%;
            font-family: "verdana","宋体";
        }
    </style>

</head>
<body>
<h1 align="center"><s:text name="index.welcome"/></h1>
    <s:a href="transmng.jsp">Translation Management</s:a><br>
    <s:a href="appmng.jsp">Application Management</s:a>
</body>
</html>