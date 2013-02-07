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

        </tr>

        <tr>
            <td valign="top" align="center" id="applicationGrid_parent">
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
