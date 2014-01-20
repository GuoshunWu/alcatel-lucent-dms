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
    <tr>
        <td>
            <button id="consistentGlossaries"><s:text name="admin.sysconfig.glossaries"/></button>
        </td>
    </tr>
</table>

<div id="createGlossaryDialog">
    <table border="0" width="100%">
        <tr>
            <td class="text-align-right"><label for="glossaryText"><s:text name="admin.glossary.text"/></label></td>
            <td><input id="glossaryText" class="fixed-width-340px" name="text"/>&nbsp;*</td>
        </tr>
        <tr>
            <td class="text-align-right"><label for="glossaryTranslate"><s:text name="admin.glossary.translate"/></label></td>
            <td><input id="glossaryTranslate" type="checkbox" name="translate"/></td>
        </tr>
        <tr>
            <td class="text-align-right"><label for="glossaryDescription"><s:text name="admin.glossary.description"/></label></td>
            <td>
                <textarea id="glossaryDescription" rows="6" cols="50" class="fixed-width-340px" name="description"></textarea>
            </td>
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