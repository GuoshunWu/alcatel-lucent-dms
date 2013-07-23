<div id="newProductReleaseDialog" title="<s:text name="appmng.newproduct"/>">
    <table>
        <tr>
            <td><s:text name="product.version"/></td>
            <td><input id="versionName" value="" type="text"/></td>
        </tr>
        <tr>
            <td><s:text name="appmng.newproduct.duplicate"/></td>
            <td><select id="dupVersion"></select></td>
        </tr>
    </table>
    <div style="display: none" id="productErrInfo">
        <br/>
        <hr/>
        <span style="color: red"><s:text name="appmng.newproduct.required"/></span>
    </div>
</div>

<div id="newApplicationVersionDialog" title="<s:text name="appmng.newapp"/>">
    <table>
        <tr>
            <td><s:text name="application.version"/></td>
            <td><input id="appVersionName" value="" type="text"/></td>
        </tr>
        <tr>
            <td><s:text name="appmng.newapp.duplicate"/></td>
            <td><select id="dupDictsVersion"></select></td>
        </tr>
    </table>
    <div style="display: none" id="appErrInfo">
        <br/>
        <hr/>
        <span style="color: red"><s:text name="appmng.newapp.required"/></span>
    </div>
</div>

<div id="addNewApplicationVersionToProductVersionDialog" title="<s:text name="appmng.addapp"/>">
    <table border="0" width="100%">
        <tr>
            <td colspan="2">
                <s:text name="appmng.addappto"/>
                <br/>
            </td>
        </tr>
        <tr>
            <td><s:text name="product"/><span id="productBaseName"></span></td>
            <td><s:text name="version"/><select id="productVersions"></select></td>
        </tr>
    </table>
    <div style="display: none" id="addAppVerErr">
        <br/>
        <hr/>
        <span style="color: red"><s:text name="appmng.newapp.required"/></span>
    </div>
</div>

<div id="addApplicationDialog" title="<s:text name="appmng.addapp"/>">
    <table>
        <tr>
            <td><label><s:text name="application.name"/></label></td>
            <td>
                <div class="ui-widget">
                    <select id="applicationName" class="ui-widget"></select>
                </div>
        </tr>
        <tr>
            <td><label><s:text name="application.version"/></label></td>
            <td>
                <select id="version" class="ui-widget"></select>
            </td>
            </td>
        </tr>
    </table>
</div>
<div id="languageSettingsDialog">
    <table border="0" width="100%">
        <tr>
            <td>
                <table>
                    <tr>
                        <td><label for="refCode"><s:text name="appmng.dialogs.languagesettings.refcode"/></label></td>
                        <td><input id="refCode" readonly="readonly"/></td>
                    </tr>
                </table>
            </td>
        </tr>
        <tr>
            <td>
                <table id="languageSettingGrid">
                    <tr>
                        <td></td>
                    </tr>
                </table>
                <div id="langSettingPager"></div>
            </td>
        </tr>
    </table>
</div>

<div id="stringSettingsDialog">
    <table border="0" width="100%">
        <tr>
            <td>
                <table border="0" width="100%">
                    <tr>
                        <td style="width:40px;"><label for="dictName"><s:text name="dictionary"/></label></td>
                        <td><input id="dictName" size="50" readonly="readonly"/></td>
                        <td style="width: 40px"><label for="dictVersion"><s:text name="version"/></label></td>
                        <td><input id="dictVersion" readonly="readonly"/></td>
                    <tr>
                        <td style="width:35px;"><label for="dictFormat"><s:text name="dictionary.format"/></label></td>
                        <td><input id="dictFormat" size="20" readonly="readonly"/></td>
                        <td style="width:35px;"><label for="dictEncoding"><s:text name="dictionary.encoding"/></label>
                        </td>
                        <td><input id="dictEncoding" size="10" readonly="readonly"/></td>
                    </tr>

                    <tr>
                        <td colspan="2"><label for="searchText"><s:text name="searchtext"/></label>
                            <input id="searchText"/>
                            <button id="searchAction"/>
                        </td>
                        <td colspan="2">
                            <div>
                                <%--<button id="unlockLabels"/>--%>
                            </div>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
        <tr>
            <td>
                <table id="stringSettingsGrid">
                    <tr>
                        <td></td>
                    </tr>
                </table>
                <div id="stringSettingsPager"></div>
            </td>
        </tr>
        <tr>
            <td>
                <ul id="setContextMenu">
                    <li><a name="[DEFAULT]" href="#"><s:text name="context.default"/></a></li>
                    <li><a name="[EXCLUSION]" href="#"><s:text name="context.excl"/></a></li>
                    <li><a name="[DICT]" href="#"><s:text name="context.dict"/></a></li>
                    <li><a name="[APP]" href="#"><s:text name="context.app"/></a></li>
                    <li><a name="[PROD]" href="#"><s:text name="context.prod"/></a></li>
                    <li><a name="Custom" href="#"><s:text name="context.custom"/></a></li>
                </ul>
                <button id="setContexts"><s:text name="appmng.dialogs.languagesettings.setcontext"/></button>
                &nbsp; &nbsp; &nbsp; &nbsp;
                <ul id="stringSettingsTranslationStatus">
                    <li><a name="2" href="#"><s:text name="status.translated"/></a></li>
                    <li><a name="0" href="#"><s:text name="status.nottranslated"/></a></li>
                </ul>
                <button id='makeStringSettingsLabelTranslateStatus'><s:text
                        name="transmng.summarypanel.makelabelas"/></button>
                &nbsp; &nbsp; &nbsp; &nbsp;
                <button id='stringCapitalize'><s:text
                        name="appmng.capitalize"/></button>
                <ul id="stringCapitalizeMenu">
                    <li><a name="all" href="#" ><s:text name="appmng.capitalize.all"/></a></li>
                    <li><a name="first" href="#"><s:text name="appmng.capitalize.first"/></a></li>
                    <li><a name="lower" href="#"><s:text name="appmng.capitalize.lower"/></a></li>
                    <li><a name="upper" href="#"><s:text name="appmng.capitalize.upper"/></a></li>
                </ul>

            </td>
        </tr>
    </table>
</div>

<div id="stringSettingsTranslationDialog" title="<s:text name="appmng.dialogs.labeltrans.title" />">
    <table border="0" width="100%">
        <tr>
            <td>
                &nbsp;
            </td>
        </tr>
        <tr>
            <td>
                <table id="stringSettingsTranslationGrid">
                    <tr>
                        <td></td>
                    </tr>
                </table>
                <div id="stringSettingsTranslationPager"></div>
            </td>
        </tr>
    </table>
</div>

<div id="customContext" title="<s:text name="context.custom.title"/>">
    <table border="0" style="width: 100%;height: 100%">
        <tr>
            <td align="right"><label for="contextName"><s:text name="context.custom.name"/></label></td>
            <td align="left"><input id="contextName"/></td>
        </tr>
        <tr>
            <td align="center" colspan="2">
                <span id="customCtxErrorMsg" style="color: red"></span>
            </td>
        </tr>
    </table>
</div>

<div id="dictListPreviewDialog">
    <table border="0" width="100%">
        <tr>
            <td>
                <table id="dictListPreviewGrid">
                    <tr>
                        <td></td>
                    </tr>
                </table>
                <div id="dictListPreviewPager"></div>
            </td>
        </tr>
        <tr>
            <td>
               &nbsp;&nbsp; <input type="checkbox" id="isAutoCreateLanguage"/><s:text name="appmng.dialogs.dictpreview.autocreatelanguage"/>
            </td>
        </tr>
    </table>
</div>

<div id="dictPreviewStringSettingsDialog">
    <table border="0" width="100%">
        <tr>
            <td>
                <table border="0" width="100%">
                    <tr>
                        <td style="width:40px;"><label for="previewDictName"><s:text name="dictionary"/></label></td>
                        <td style="width:350px;"><input id="previewDictName" size="48" readonly="readonly"/></td>
                        <td><label for="previewDictVersion" style="width: 200px"><s:text name="version"/></label></td>
                        <td><input id="previewDictVersion" readonly="readonly"/></td>
                    <tr>
                        <td style="width:35px;"><label for="previewDictFormat"><s:text
                                name="dictionary.format"/></label></td>
                        <td><input id="previewDictFormat" size="48" readonly="readonly"/></td>
                        <td><label for="previewDictEncoding"><s:text name="dictionary.encoding"/></label></td>
                        <td><input id="previewDictEncoding" readonly="readonly"/></td>
                    </tr>
                </table>
            </td>
        </tr>
        <tr>
            <td>
                <table id="dictPreviewStringSettingsGrid">
                    <tr>
                        <td></td>
                    </tr>
                </table>
                <div id="dictPreviewStringSettingsPager"></div>
            </td>
        </tr>
    </table>
</div>

<div id="dictPreviewLanguageSettingsDialog">
    <table border="0" width="100%">
        <tr>
            <td>
                <table>
                    <tr>
                        <td><label for="previewRefCode"><s:text name="appmng.dialogs.languagesettings.refcode"/></label>
                        </td>
                        <td><input id="previewRefCode" readonly="readonly"/></td>
                    </tr>
                </table>
            </td>
        </tr>
        <tr>
            <td>
                <table id="previewLanguageSettingGrid">
                    <tr>
                        <td></td>
                    </tr>
                </table>
                <div id="previewLangSettingPager"></div>
            </td>
        </tr>
    </table>
</div>

<div id="addLanguageDialog" title="<s:text name="appmng.addlanguage.title"/> ">
    <table>
        <tr>
            <td><label for="languageName"><s:text name="appmng.addlanguage.name"/></label></td>
            <td><select id="languageName" name="languageName"/></td>
        </tr>
        <tr>
            <td><label for="charset"><s:text name="appmng.addlanguage.charset"/></label></td>
            <td><select id="charset" name="charset"/></td>
        </tr>
        <tr>
            <td><label for="addLangCode"><s:text name="code"/></label></td>
            <td><input id="addLangCode" name='code'></td>
        </tr>
        <tr>
            <td style="width: 25px"/>
            <td align="left">
                <span id="errorMsg" style="color:red"></span>
            </td>
        </tr>
    </table>
</div>

<div id="removeLanguageDialog" title="<s:text name="appmng.removelanguage.title"/> ">
    <table>
        <tr>
            <td><label for="removeLanguageDialog_addLangCode"><s:text name="code"/></label></td>
            <td><input id="removeLanguageDialog_addLangCode" name='code'></td>
        </tr>
        <tr>
            <td style="width: 25px"/>
            <td align="left">
                <span id="removeLanguageDialog_errorMsg" style="color:red"></span>
            </td>
        </tr>
    </table>
</div>


<div id="historyDialog" title="<s:text name="appmng.dialogs.history.title"/> ">
    <table border="0" width="100%">
        <tr>
            <td>
                <table id="historyGrid">
                    <tr>
                        <td></td>
                    </tr>
                </table>
                <div id="historyGridPager"></div>
            </td>
        </tr>
    </table>
</div>

<div id="addLabelDialog" title="<s:text name="appmng.dialogs.addlabel.title"/>">
    <table border="0" width="100%">
        <tr>
            <td><label class="form-label" for="key"><s:text name="appmng.dialogs.addlabel.key"/></label></td>
            <td>
                <input size="50" id="key"/><span style="color: red">*</span>
            </td>
        </tr>
        <tr>
            <td><label class="form-label" for="reference"><s:text name="appmng.dialogs.addlabel.reference"/></label>
            </td>
            <td><textarea cols="50" id="reference"></textarea><span style="color: red">*</span></td>
        </tr>
        <tr>
            <td><label class="form-label" for="maxLength"><s:text name="appmng.dialogs.addlabel.maxlength"/></label>
            </td>
            <td><input size="50" id="maxLength"/></td>
        </tr>
        <tr>
            <td><label class="form-label" for="context"><s:text name="appmng.dialogs.addlabel.context"/></label></td>
            <td><input size="50" id="context" value="[DEFAULT]"/><span style="color: red">*</span></td>
        </tr>
        <tr>
            <td><label class="form-label" for="description"><s:text name="appmng.dialogs.addlabel.description"/></label>
            </td>
            <td><textarea cols="50" id="description"></textarea></td>
        </tr>

        <tr>
            <td colspan="2">
                <div id="errMsg" class="err-message"></div>
            </td>

        </tr>
    </table>
</div>


<div id="searchTextDialog" title="<s:text name="searchtext.title"/>">
    <table>
        <tr>
            <td>
                <table id="searchTextGrid">
                    <tr>
                        <td></td>
                    </tr>
                </table>
                <div id="searchTextGridPager"></div>
            </td>
        </tr>
    </table>
</div>

<div id="importReportDialog" title="<s:text name="appmng.dialogs.importreport.title"/>">
    <table id="importReportStatistics" width="100%" border="0" style="border: 2px solid;" cellspacing="0">
        <caption style="padding-bottom: 10px">
            <span id="title" class="import-report-title"></span>
        </caption>

        <%-- Header --%>
        <tr>
            <th class="import-report-group import-report-group-right import-report-header">
                <label>
                    <s:text name="appmng.dialogs.importreport.category"/>
                </label>
            </th>
            <th class="import-report-number import-report-group import-report-group-right">
                <s:text name="appmng.dialogs.importreport.instrings"/>
            </th>
            <th class="import-report-group import-report-group-right"><s:text name="appmng.dialogs.importreport.inwords"/></th>
            <th class="import-report-group"><s:text name="appmng.dialogs.importreport.chart"/></th>
        </tr>

        <%-- Duplication Translations --%>
        <tr>
            <td class="import-report-label import-report-group-right"><label><s:text name="appmng.dialogs.importreport.toltrans"/></label></td>
            <td class="import-report-number import-report-group-right">
                <span id="totalTrans"></span>
            </td>
            <td class="import-report-number import-report-group-right"><span></span></td>
            <td rowspan="4" class="import-report-group">
                <div id="dupContainer" class="import-report-chart-container"></div>
            </td>
        </tr>
        <tr>
            <td class="import-report-label import-report-group-right"><label><s:text name="appmng.dialogs.importreport.duptrans"/></label></td>
            <td class="import-report-number import-report-group-right">
                <span id="dupTrans"></span>
            </td>
            <td class="import-report-number import-report-group-right"><span></span></td>
        </tr>
        <tr>
            <td class="import-report-label import-report-group-right"><label><s:text name="appmng.dialogs.importreport.disttrans"/></label></td>
            <td class="import-report-number import-report-group-right">
                <span id="distinctTrans1"></span>
            </td>
            <td class="import-report-number import-report-group-right"><span></span></td>
        </tr>
        <tr>
            <td class="import-report-label import-report-group import-report-group-right">
                <label><s:text name="appmng.dialogs.importreport.dupratio"/></label>
            </td>
            <td class="import-report-number import-report-group import-report-group-right">
                <span id="dupRatio"></span>
            </td>
            <td class="import-report-number import-report-group import-report-group-right"><span></span></td>

        </tr>

        <%-- Translated--%>
        <tr>
            <td class="import-report-label import-report-group-right"><label><s:text name="appmng.dialogs.importreport.disttrans"/></label></td>
            <td class="import-report-number import-report-group-right">
                <span id="distinctTrans2"></span>
            </td>
            <td class="import-report-number import-report-group-right"><span></span></td>
            <td rowspan="4" class="import-report-group">
                <div id="transContainer" class="import-report-chart-container"></div>
            </td>
        </tr>
        <tr>
            <td class="import-report-label import-report-group-right"><label><s:text name="appmng.dialogs.importreport.translated"/></label></td>
            <td class="import-report-number import-report-group-right">
                <span id="translated"></span>
            </td>
            <td class="import-report-number import-report-group-right"><span></span></td>
       </tr>
        <tr>
            <td class="import-report-label import-report-group-right"><label><s:text name="appmng.dialogs.importreport.untranslated"/></label></td>
            <td class="import-report-number import-report-group-right">
                <span id="untranslated"></span>
            </td>
            <td class="import-report-number import-report-group-right"><span></span></td>
        </tr>
        <tr>
            <td class="import-report-label import-report-group import-report-group-right"><label><s:text name="appmng.dialogs.importreport.transratio"/></label>
            </td>
            <td class="import-report-number import-report-group import-report-group-right">
                <span id="transRatio"></span>
            </td>
            <td class="import-report-number import-report-group import-report-group-right"><span></span></td>
        </tr>

        <%-- Auto Trans--%>
        <tr>
            <td class="import-report-label import-report-group-right"><label><s:text name="appmng.dialogs.importreport.untranslated"/></label></td>
            <td class="import-report-number import-report-group-right">
                <span id="untranslated1"></span>
            </td>
            <td class="import-report-number import-report-group-right"><span></span></td>
            <td rowspan="4">
                <div id="autoTransContainer" class="import-report-chart-container"></div>
            </td>
        </tr>
        <tr>
            <td class="import-report-label import-report-group-right"><label><s:text name="appmng.dialogs.importreport.autotrans"/></label></td>
            <td class="import-report-number import-report-group-right">
                <span id="autoTrans"></span>
            </td>
            <td class="import-report-number import-report-group-right"><span></span></td>
        </tr>
        <tr>
            <td class="import-report-label import-report-group-right"><label><s:text name="appmng.dialogs.importreport.nomatch"/></label></td>
            <td class="import-report-number import-report-group-right">
                <span id="noMatch"></span>
            </td>
            <td class="import-report-number import-report-group-right"><span></span></td>

        </tr>
        <tr>
            <td class="import-report-label import-report-group-right"><label><s:text name="appmng.dialogs.importreport.autotransratio"/></label></td>
            <td class="import-report-number import-report-group-right">
                <span id="autoRatio"></span>
            </td>
            <td class="import-report-number import-report-group-right"><span></span></td>
        </tr>
    </table>
</div>