<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page contentType="text/html;charset=utf-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>



<div id="createTranslationTaskDialog" title="<s:text name="transmng.dialogs.title"/>">
    <table width="100%" border="0">
        <%--<tr>--%>
            <%--<td>&nbsp;</td>--%>
        <%--</tr>--%>
        <tr>
            <td>
                <table border="0" width="100%">
                    <tr>
                        <td><s:text name="transmng.dialogs.taskname"/></td>
                        <td><input size="45" value="2nd translation task for ISC R6.6" id="taskName" > </td>
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
                        <td><s:text name="transmng.dialogs.dictionaryselected"/><span id="dictSelected"></span></td>
                        <td><s:text name="transmng.dialogs.totallabels"/><span id="totalLabels"></span> </td>
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
                        <td><s:text name="transmng.dialogs.targetlanguages"/></td>
                   </tr>
                </table>
            </td>
        </tr>
        <tr>
            <td>&nbsp;</td>
        </tr>
        <tr>
            <td><button id="createTransTask"><s:text name="button.create"/></button><button id="cancelTransTask"><s:text name="button.cancel"/></button></td>
        </tr>
    </table>
</div>

