<%@ page contentType="text/html;charset=utf-8"%>
<%
response.setStatus(401);
response.setHeader("WWW-Authenticate", "Basic realm=\"Dictionary Management System\"");
%>