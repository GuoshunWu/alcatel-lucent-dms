<%@ taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page contentType="text/html;charset=utf-8" %>
<style type="text/css">
    #adminTabs>div.ui-tabs-panel {
        /*border: 1px solid blue;*/
        padding: 0
    }
</style>

<div id="adminTabs">
    <ul>
        <li><a href="#langAdmin"><s:text name="admin.language.title"/></a></li>
        <li><a href="#charsetAdmin"><s:text name="admin.charset.title"/></a></li>
        <li><a href="#userAdmin"><s:text name="admin.user.title"/></a></li>
    </ul>

    <div id="langAdmin">
        <jsp:include page="admin/languageadmin.jsp"/>
    </div>
    <div id="charsetAdmin">
        <jsp:include page="admin/charsetadmin.jsp"/>
    </div>
    <div id="userAdmin">
        <jsp:include page="admin/useradmin.jsp"/>
    </div>
</div>

