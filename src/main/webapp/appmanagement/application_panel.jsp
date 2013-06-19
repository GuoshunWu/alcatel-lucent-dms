<div id="DMS_applicationPanel" class="dms_appmng_panel">

    <table border="0" style="width: 100%; height:100%;">
        <tr style="height: 20px">
            <td>
                <span class="show-label"><s:text name="application"/></span>
                <span class="show-label" id="appDispAppName"></span>
                &nbsp;&nbsp;&nbsp;&nbsp;
                <span class="show-label"><s:text name="version"/></span>
                <select id="selAppVersion"></select>
                <button id="newAppVersion" title="<s:text name="appmng.newapp"/> "></button>
                &nbsp;&nbsp;
                <button id="removeAppVersion" title="<s:text name="appmng.removeapp"/>"></button>
            </td>

            <td align ="right">
                <label for="appSearchText"><s:text name="searchtext"/></label>
                <input id="appSearchText" />
                <button id="appSearchAction"></button>
                &nbsp;&nbsp;
                <s:text name="fuzzy"/> <input type="checkbox" id="appSearchText_fuzzy"/>
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
            </td>
        </tr>
        <tr>
            <td colspan="2" id="dictionaryGridList_parent">
                <div id="dictionaryGrid">
                    <table id="dictionaryGridList">
                        <tr>
                            <td></td>
                        </tr>
                    </table>
                    <div id="dictPager"></div>
                </div>
            </td>
        </tr>
        <tr style="height: 20px">
            <td colspan="2">
                <table border="0" width="100%">
                    <tr>
                        <td style="width: 165px">
                            <span id="uploadBrower" style="height: 28px"></span>
                        </td>
                        <td style="width: 170px">
                            <button style="width:160;" id="generateDict"><s:text name="appmng.generatedict"/></button>
                        </td>
                        <td>
                            <button id="batchAddLanguage">Add language</button>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
    </table>
</div>
