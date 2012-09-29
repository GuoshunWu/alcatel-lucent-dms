<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page contentType="text/html;charset=utf-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>


<div id="createTranslationTaskDialog" title="<s:text name="transmng.dialogs.transtask.title"/>">
    <table width="100%" border="0">
        <%--<tr>--%>
        <%--<td>&nbsp;</td>--%>
        <%--</tr>--%>
        <tr>
            <td>
                <table border="0" width="100%">
                    <tr>
                        <td><s:text name="transmng.dialogs.transtask.taskname"/></td>
                        <td><input size="45" id="taskName"></td>
                    </tr>
                </table>
            </td>
        </tr>
        <tr>
            <td>&nbsp;</td>
        </tr>
        <tr>
            <td>
                <table border="0" width="100%">
                    <tr>
                        <td><s:text name="transmng.dialogs.transtask.dictionaryselected"/><span
                                id="dictSelected"></span></td>
                        <td><s:text name="transmng.dialogs.transtask.totallabels"/><span id="totalLabels"></span></td>
                    </tr>
                </table>
            </td>
        </tr>
        <tr>
            <td>&nbsp;</td>
        </tr>
        <tr>
            <td>
                <table id="targetLanguages" border="0" width="100%">
                    <tr>
                        <td><s:text name="transmng.dialogs.transtask.targetlanguages"/></td>
                    </tr>
                </table>
            </td>
        </tr>
        <tr>
            <td>&nbsp;</td>
        </tr>
    </table>
</div>

<div id="translationDetailDialog" title="<s:text name="transmng.dialogs.transdetail.title"/>">
    <table border="0" width="100%">
        <%--<tr>--%>
        <%--<td>&nbsp;</td>--%>
        <%--</tr>--%>
        <tr>
            <td><s:text name="dictionary"/>&nbsp;&nbsp;<span id='dictionaryName'></span></td>
        </tr>
        <tr>
            <td>
                <table border="0" width="100%" style="border-color: red">
                    <tr>
                        <td style="width: 15px"/>
                        <td>
                            <select id="detailLanguageSwitcher">
                                <option value="">
                                    <s:text name="transmng.summarypanel.languagefilter"/>
                                </option>
                            </select>
                        </td>
                        <td>&nbsp;</td>
                        <td><s:text name="transmng.summarypanel.searchtext"/><input/></td>
                        <td>&nbsp;</td>

                        <td style="width: 15px"/>
                    </tr>
                </table>
            </td>
        </tr>
        <tr>
            <td>
                <table width="100%" border="0" style="border-color: yellow">
                    <tr>
                        <td align="center" colspan="9" style="width: 100%">

                            <table width="100%" id="transDetailGridList">
                                <tr>
                                    <td/>
                                </tr>
                            </table>
                            <div id="transDetailsPager"/>

                        </td>
                    </tr>
                </table>
            </td>
        </tr>
        <tr>
            <td>
                <s:text name="transmng.summarypanel.makelabelas"/>&nbsp;&nbsp;
                <button id='detailTransTranslated' value="2">T</button>
                <button id='detailTransNotTranslated' value="0">N</button>
            </td>
        </tr>
    </table>
</div>