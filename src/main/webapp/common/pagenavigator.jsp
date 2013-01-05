<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page contentType="text/html;charset=utf-8" isELIgnored="false" %>
<span style="font-family:Trebuchet MS,Verdana,Arial, Helvetica;font-size:12px">

<s:form id="naviForm" theme="simple" action="entry" namespace="/" method="post">
    <label for="pageNavigator"><s:text name="header.currentView"/></label>
    <s:select key="header.currentView" list="naviPages" id="pageNavigator" name="naviTo" value="naviTo"/>
    <s:hidden id="curProductBaseId" name="curProductBaseId"/>
    <s:hidden id="curProductId" name="curProductId"/>
</s:form>

<div style="margin-top:5px">
    <s:text name="header.welcome"/>&nbsp;
    <span style="color:#800080;font-weight:bold"><s:property value="#session['user_context'].user.name"/></span>&nbsp;&nbsp;
    <a href='<s:url action="logout" namespace="/login"/>'><s:text name="header.logout"/></a>
</div>
</span>
<%--<s:form id="langForm" action="" method="post" theme="simple">--%>
<%--<select name="request_locale" onchange="return $('#langForm').submit();">--%>
<%--<option value="en_US">English</option>--%>
<%--<option value="zh_CN">Chinese</option>--%>
<%--</select>--%>
<%--</s:form>--%>
<%--WW_TRANS_I18N_LOCALE: <s:property value="session['WW_TRANS_I18N_LOCALE']"/>--%>
<%--request_locale: <s:property value="#parameters.request_locale"/>--%>
