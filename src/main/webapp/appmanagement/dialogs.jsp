<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page contentType="text/html;charset=utf-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div id="newProductDialog" title="<s:text name="appmng.newproductbase"/> ">
    <span><s:text name="product.name"/><input id="productName" value="" type="text"/></span>
</div>

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
</div>

<div id="newOrAddApplicationDialog" title="<s:text name="appmng.addapp"/>">
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

<div id="languageSettingsDialog" />
<table border="1" width="100%">
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
                    <td/>
                </tr>
            </table>
            <div id="langSettingPager"></div>
        </td>
    </tr>
</table>
</div>

<div id="stringSettingsDialog" />
<table border="1" width="100%">
    <tr>
        <td>
            <table>
                <tr>
                    <td><label for="dictName"><s:text name="dictionary"/></label></td>
                    <td><input id="dictName" readonly="readonly"/></td>
                    <td><label for="dictVersion"><s:text name="version"/></label></td>
                    <td><input id="dictVersion" readonly="readonly"/></td>
                </tr>
            </table>
        </td>
    </tr>
    <tr>
        <td>
            <table>
                <tr>
                    <td><label for="dictFormat"><s:text name="dictionary.format"/></label></td>
                    <td><input id="dictFormat" readonly="readonly"/></td>
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
</table>
</div>