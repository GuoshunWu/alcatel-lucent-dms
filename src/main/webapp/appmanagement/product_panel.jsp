<div id="DMS_productPanel" class="dms_appmng_panel">

    <table border="0" style="width: 100%; height: 100%">
        <tr style="height:20px;">
            <td>
                <span class="show-label"><s:text name="product"/></span>
                <span class="show-label" id="dispProductName"></span>
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                <label class="show-label" for="selVersion"><s:text name="version"/></label>
                <select id="selVersion"></select>
                <button id="newVersion" title="<s:text name="appmng.newproduct"/> "></button>
                &nbsp;&nbsp;
                <button id="removeVersion" title="<s:text name="appmng.removeproduct"/>"></button>
            </td>
            <td  align ="right">
                <label for="prodSearchText"><s:text name="searchtext"/></label>
                <input id="prodSearchText" />
                <button id="prodSearchAction"></button>
                &nbsp;&nbsp;
                <s:text name="fuzzy"/> <input type="checkbox" id="prodSearchText_fuzzy"/>
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
            </td>

        </tr>

        <tr>
            <td colspan="2" id="applicationGrid_parent">
                <div id="applicationGrid">
                    <table id="applicationGridList">
                        <tr>
                            <td></td>
                        </tr>
                    </table>
                    <div id="pager"></div>
                </div>
            </td>
        </tr>
    </table>

</div>
