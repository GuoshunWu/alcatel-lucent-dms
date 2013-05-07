<div id="DMS_searchPanel" class="dms_appmng_panel" style="text-align: center; ">
    <table style="height:100%;width: 100%; " border="0">
        <tr style="height:30px;">
            <td>
                <button id="goBackToWelcome"><s:text name="button.back2welcome"/></button>
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                <s:text name="searchtext"/>
                <input size="80" id="globalSearchInResultPanel" name="globalSearch"/><button id="globalSearchInResultPanelAction"/>

            </td>
            <td>
                <button id="groupingToggle"><s:text name="button.grptoggle"/></button>
            </td>
        </tr>
        <tr>
            <td colspan="2" style="vertical-align:middle;" id="globalSearchResultGrid_parent">
                <div>
                    <table id="globalSearchResultGrid">
                        <tr>
                            <td></td>
                        </tr>
                    </table>
                    <div id="globalSearchResultGridPager"></div>
                </div>
            </td>
        </tr>
    </table>
</div>