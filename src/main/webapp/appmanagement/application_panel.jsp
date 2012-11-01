<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page contentType="text/html;charset=utf-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<div id="DMS_applicationPanel">

    <table border="0">
        <tr>
            <td>
                <table border="0" width="100%">
                    <tr>
                        <td style="width:70px;" align="left" class="show-label"><s:text name="product"/></td>
                        <td class="show-label" align="left"><span id="appDispProductName"></span></td>
                    </tr>
                    <tr>
                        <td align="left" class="show-label"><s:text name="application"/></td>
                        <td align="left" class="show-label"><span id="appDispAppName"></span></td>
                    </tr>
                    <tr>
                        <td><span class="show-label"><s:text name="version"/></span></td>
                        <td>
                            <select id="selAppVersion"></select>
                            <button id="newAppVersion" title="<s:text name="appmng.newapp"/> "></button>
                            &nbsp;&nbsp;
                            <button id="removeAppVersion" title="<s:text name="appmng.removeapp"/>"></button>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2">
                            <%--@declare id="dctfileupload"--%>
                            <label class="show-label" for="dctFileUpload"><s:text name="appmng.deliverapp"/></label>
                            &nbsp;&nbsp;
                            <div id="uploadBrower"></div>
                            &nbsp;&nbsp;
                            <span id="uploadStatus"/>
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
                <table>
                    <tr>
                        <td>
                            <form id="downloadDict" action="/app/download-app-dict" method="post">
                                <input type="hidden" id="fileLoc" name="fileLoc"/>
                            </form>
                            <button id="generateDict">Generate dictionary</button>

                            <button id="batchAddLanguage">Add language</button>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
    </table>
</div>
