<div class="dms-panel" id="admin.jsp">
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
</div>