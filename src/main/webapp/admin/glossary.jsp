<table style="width: 100%;height: 100%" border="0">
    <tr>
        <td align="center" valign="top">
            <table id="glossaryGrid">
                <tr>
                    <td></td>
                </tr>
            </table>
            <div id="glossaryGridPager"/>
        </td>
    </tr>
    <%--<tr>--%>
        <%--<td>--%>
            <%--<button id="consistentGlossaries"><s:text name="admin.sysconfig.glossaries"/></button>--%>
        <%--</td>--%>
    <%--</tr>--%>
</table>

<div id="createGlossaryDialog">
    <table border="0" width="100%">
        <tr>
            <td class="text-align-right"><label for="glossaryText"><s:text name="admin.glossary.text"/></label></td>
            <td><input id="glossaryText" class="fixed-width-340px" name="text"/>&nbsp;*</td>
        </tr>
        <tr>
            <td class="text-align-right"><label for="glossaryDescription"><s:text name="admin.glossary.comment"/></label></td>
            <td>
                <textarea id="glossaryDescription" cols="50" style="height: 65px" class="fixed-width-340px" name="description"></textarea>
            </td>
        </tr>

        <tr>
            <td class="text-align-right"><label><s:text name="admin.glossary.translate"/></label></td>
            <td>
                <input type="radio" value="true" name="translate"/>Yes
                <input type="radio" value="false" checked name="translate"/>No
            </td>
        </tr>
        <tr>
            <td>&nbsp;</td>
            <td><s:text name="admin.glossary.translateDescription"/></td>
        </tr>

        <tr>
            <td colspan="2" align="center">
                <div id="glossaryErrorMsgContainer" style="width: 95%;">
                    <hr />
                    <span id="errorMsg" style="color: red;" />
                </div>
            </td>
        </tr>
    </table>
</div>