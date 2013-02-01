<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page contentType="text/html;charset=utf-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

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
            <td><s:text name="product"/><span id="productBaseName"/></td>
            <td><s:text name="version"/><select id="productVersions"/></td>
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

<div id="progressbar" class="ui-widget-content">
    <div id="barvalue" style="z-index: 1000;position: absolute;left:45%;"></div>
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
                        <td style="width:350px;"><input id="dictName" size="48" readonly="readonly"/></td>
                        <td><label for="dictVersion" style="width: 200px"><s:text name="version"/></label></td>
                        <td><input id="dictVersion" readonly="readonly"/></td>
                    <tr>
                        <td style="width:35px;"><label for="dictFormat"><s:text name="dictionary.format"/></label></td>
                        <td><input id="dictFormat" size="48" readonly="readonly"/></td>
                        <td><label for="dictEncoding"><s:text name="dictionary.encoding"/></label></td>
                        <td><input id="dictEncoding" readonly="readonly"/></td>
                    </tr>
                </table>
            </td>
        </tr>
        <tr>
            <td>
                <table id="stringSettingsGrid">
                    <tr>
                        <td/>
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
                        <td/>
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
                        <td/>
                    </tr>
                </table>
                <div id="dictListPreviewPager"></div>
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
                        <td/>
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
            <td><label for="addLangCode"><s:text name="appmng.addlanguage.code"/></label></td>
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