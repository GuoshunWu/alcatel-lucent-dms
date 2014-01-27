<div class="dms-panel" id="ctxmng" style="height: 100%">
    <div id="layout-container">
        <div class="ui-layout-center">
            <table border="0" width="100%" height="100%">
                <tr style="height: 25px">
                    <td>
                        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                        <select id="contextSelector"></select>
                        <s:text name="searchtext"/><input id="contextSearchText"/>
                        <button id="contextSearchAction"></button>
                        <s:text name="fuzzy"/><input id="contextSearchTextFuzzy" type="checkbox">
                    </td>
                </tr>
                <tr>
                    <td align='center' valign='top'>
                        <table id="contextGrid">
                            <tr>
                                <td></td>
                            </tr>
                        </table>
                        <div id="contextGridPager"></div>
                    </td>
                </tr>
            </table>
        </div>

        <div class="ui-layout-south">
            <table border="0" width="100%" height="100%">
                <tr style="height: 25px">
                    <td align="left">
                        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                        <s:text name="ctxmng.comparewithctx"/>
                        <select id="compareWithContextSelector"></select>
                        <button id="contextShowDiff"><s:text name="ctxmng.showdiff"/></button>
                        <button id="contextMerge"><s:text name="ctxmng.merge"/></button>
                    </td>
                </tr>
                <tr>
                    <td align='center' valign='top'>
                        <table id="compareContextGrid">
                            <tr>
                                <td></td>
                            </tr>
                        </table>
                        <div id="compareContextGridPager"></div>
                    </td>
                </tr>
            </table>
        </div>
    </div>

    <%@include file="dialogs.jsp" %>
</div>

