<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page contentType="text/html;charset=utf-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>


<div id="progressbar" class="progressbar">
    <div class="progressbar-label"></div>
</div>

<div id="translationReportDialog" title="<s:text name="taskmng.dialogs.transreport.title"/>">
    <table border="0" width="100%">
        <tr>
            <td>&nbsp;</td>
        </tr>
        <tr>
            <td>
                <table id="reportGrid">
                    <tr>
                        <td></td>
                    </tr>
                </table>
                <div id="reportPager"></div>
            </td>
        </tr>
        <tr>
            <td>&nbsp;</td>
        </tr>
        <%--<tr><td align="right">2 Warnings...</td></tr>--%>
        <%--<tr>--%>
        <%--<td>--%>
        <%--<table style="display: none">--%>
        <%--<tr>--%>
        <%--<td><label><s:text name="taskmng.dialogs.transreport.chooselang"/></label></td>--%>
        <%--<td><button id="langChooser"><s:text name="button.chooselang"/></button></td>--%>
        <%--</tr>--%>
        <%--</table>--%>
        <%--</td>--%>
        <%--</tr>--%>
    </table>
</div>

<div id="translationDetailDialog" title="<s:text name="taskmng.dialogs.viewdetail.title"/> ">
    <table border="0" width="100%">
        <tr>
            <td>&nbsp;</td>
        </tr>
        <tr>
            <td>
                <table id="viewDetailGrid">
                    <tr>
                        <td></td>
                    </tr>
                </table>
                <div id="ViewDetailPager"></div>
            </td>
        </tr>
        <tr>
            <td>&nbsp;</td>
        </tr>
    </table>
</div>
