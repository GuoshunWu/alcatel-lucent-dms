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
            <li><a href="#userAdmin"><s:text name="admin.user.title"/></a></li>
            <li><a href="#sysConfig">System configuration</a></li>
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
        <div id="sysConfig">
            <button id="buildLuceneIndex">(Re)build lucene index</button>
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
                                <label for="loginName">Login name: </label>
                            </td>
                            <td>
                                <input id="loginName" name="loginName" /><span style="color: red">*</span>
                            </td>
                        </tr>
                        <tr>
                            <td class="form-label">
                                <label for="name">Name: </label>
                            </td>
                            <td>
                                <input size="60" readonly="true" name="name" id="name"/>
                            </td>
                        </tr>
                        <tr>
                            <td class="form-label">
                                <label for="email">Email: </label>
                            </td>
                            <td>
                                <input size="60" readonly="true" name="email" id="email"/>
                            </td>
                        </tr>
                        <tr>
                            <td class="form-label">
                                <label for="enabled">Enabled: </label>
                            </td>
                            <td>
                                <input type="checkbox" name="userStatus" value="1" checked="true" id="enabled"/>
                            </td>
                        </tr>
                        <tr>
                            <td class="form-label">
                                <label for="role">Role: </label>
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