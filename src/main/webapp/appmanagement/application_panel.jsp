<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page contentType="text/html;charset=utf-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<div id="DMS_applicationPanel" class="dms_appmng_panel">

    <table border="0">
        <tr>
            <td>
                <table border="0" width="100%">
                    <%--<tr>--%>
                    <%--<td style="width:70px;" align="left" class="show-label"><s:text name="product"/></td>--%>
                    <%--<td class="show-label" align="left"><span id="appDispProductName"></span></td>--%>
                    <%--</tr>--%>
                    <tr>
                        <td align="left">
                            <span class="show-label"><s:text name="application"/></span>
                            <span class="show-label" id="appDispAppName"></span>
                            &nbsp;&nbsp;&nbsp;&nbsp;
                            <span class="show-label"><s:text name="version"/></span>
                            <select id="selAppVersion"></select>
                            <button id="newAppVersion" title="<s:text name="appmng.newapp"/> "></button>
                            &nbsp;&nbsp;
                            <button id="removeAppVersion" title="<s:text name="appmng.removeapp"/>"></button>

                        </td>
                    </tr>
                </table>
            </td>
        </tr>
        <tr>
            <td>
                <div id="dictionaryGrid">
                    <table id="dictionaryGridList">
                        <tr>
                            <td/>
                        </tr>
                    </table>
                    <div id="dictPager"></div>
                </div>
            </td>
        </tr>
        <tr>
            <td>
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
