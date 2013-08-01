<div class="dms-panel" id="admin">
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

            <s:if test="#session.user_context.user.role in {4}">
                <li><a href="#userAdmin"><s:text name="admin.user.title"/></a></li>
                <li><a href="#sysConfig"><s:text name="admin.sysconfig.title"/></a></li>
            </s:if>
            <li><a href="#glossary">Glossary</a></li>
        </ul>

        <div id="langAdmin">
            <%@include file="languageadmin.jsp" %>
        </div>
        <div id="charsetAdmin">
            <%@include file="charsetadmin.jsp" %>
        </div>

        <s:if test="#session.user_context.user.role in {4}">
            <div id="userAdmin">
                <%@include file="useradmin.jsp" %>
            </div>
            <div id="sysConfig">
                <button id="buildLuceneIndex"><s:text name="admin.sysconfig.rebuildindex"/></button>
            </div>
        </s:if>

        <div id="glossary">
            <%@include file="glossary.jsp" %>
        </div>

    </div>
</div>

<div id="addUserDialog" title="Add new user">
    <table width='100%' border="0">
        <tr>
            <td align='center'>
                <form id="addUserForm" method="post">
                    <table border="0">
                        <tr>
                            <td class="form-label">
                                <label for="loginName"><s:text name="admin.user.loginname"/></label>
                            </td>
                            <td>
                                <input id="loginName" name="loginName"/><span style="color: red">*</span>
                            </td>
                        </tr>
                        <tr>
                            <td class="form-label">
                                <label for="name"><s:text name="admin.user.name"/></label>
                            </td>
                            <td>
                                <input size="60" readonly="true" name="name" id="name"/>
                            </td>
                        </tr>
                        <tr>
                            <td class="form-label">
                                <label for="email"><s:text name="admin.user.email"/></label>
                            </td>
                            <td>
                                <input size="60" readonly="true" name="email" id="email"/>
                            </td>
                        </tr>
                        <tr>
                            <td class="form-label">
                                <label for="enabled"><s:text name="admin.user.enabled"/></label>
                            </td>
                            <td>
                                <input type="checkbox" name="userStatus" value="1" checked="true" id="enabled"/>
                            </td>
                        </tr>
                        <tr>
                            <td class="form-label">
                                <label for="role"><s:text name="admin.user.role"/></label>
                            </td>
                            <td>
                                <select id="role"></select>
                            </td>
                        </tr>

                        <tr>
                            <td colspan="2" align='center'>
                                <span style="color: red" id="errMsg"></span>
                            </td>
                        </tr>
                    </table>
                </form>
            </td>
        </tr>
    </table>

</div>