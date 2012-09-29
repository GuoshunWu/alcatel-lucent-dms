<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page contentType="text/html;charset=utf-8" %>

<select id="pageNavigator" onchange="window.location.href=this.value;">
    <option value="/appmng.jsp"><s:text name="appmng.title"/></option>
    <option value="/transmng.jsp"><s:text name="transmng.title"/></option>
    <option value="/taskmng.jsp"><s:text name="taskmng.title"/></option>
</select>