<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page contentType="text/html;charset=utf-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<div id="DMS_applicationPanel">

    <table border="0">
        <tr>
            <td style="max-height: 10px">
                <s:text name="product"/>&nbsp;
                <span id="appDispProductName"></span>
            </td>
        </tr>
        <tr>
            <td>
                <table border="0" width="100%">
                    <tr>
                        <td style="width: 70px"><s:text name="application"/></td>
                        <td style="width: 70px"><span id="appDispAppName"></span></td>
                        <td><s:text name="version"/><select id="selAppVersion"></select></td>
                    </tr>
                </table>
            </td>
        </tr>
        <tr>
            <td>
                <table border="0" width="100%">
                    <tr>
                        <td style="width: 120px;">
                            <label for="dctFileUpload"><s:text name="appmng.deliverapp"/></label>
                        </td>
                        <td style="width: 100px;">
                            <style>
                                #dctFileUpload{
                                    position: absolute;margin-left: -10px;margin-top: -6px; font-size:60px;z-index: 1000;opacity: 0;
                                }
                                #uploadContainer{
                                    overflow: hidden;position: absolute;margin-top: -15px;
                                }
                            </style>
                            <span id="uploadContainer">
                                <input title="Choose File..." id="dctFileUpload" type="file" name="upload" hidefocus/>
                                <button id="uploadBrower">Brower...</button>
                                 <%--<button onclick="dctFileUpload.click();">MyTest</button>--%>
                            </span>
                        </td>
                        <td>
                            <span id="uploadStatus">status</span>
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

    </table>
</div>
