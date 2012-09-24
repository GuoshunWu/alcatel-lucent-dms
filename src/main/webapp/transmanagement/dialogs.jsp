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
                        <td><input size="45" id="taskName" > </td>
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
                    <tr >
                        <td><s:text name="transmng.dialogs.transtask.dictionaryselected"/><span id="dictSelected"></span></td>
                        <td><s:text name="transmng.dialogs.transtask.totallabels"/><span id="totalLabels"></span> </td>
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

</div>