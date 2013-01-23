<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page contentType="text/html;charset=utf-8" %>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html;charset=utf-8"/>
</head>
<body>
<%
    System.out.println("Begin forward aaa...");
%>

<jsp:forward page="entry.action">
    <jsp:param name="naviTo" value="appmng.jsp"/>
</jsp:forward>

</body>
</html>