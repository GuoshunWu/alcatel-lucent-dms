<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page contentType="text/html;charset=utf-8" %>

<table width="100%" border="0">
    <tr>
        <td>
            <span style="font-family:fantasy; font-size:14pt; font-style:normal; ">
                <s:text name="taskmng.title"/>
            </span>
        </td>
        <td>
            <div id="switcher"></div>
        </td>
        <td  align="right">
            <%@include file="/common/pagenavigator.jsp" %>
        </td>
    </tr>
    <tr>
        <td colspan="3">&nbsp;</td>
    </tr>
    <tr>
        <td colspan="3">
            <table border="0" width="100%">
                <tr>
                    <td align="right" style="width:80px"><s:text name="product"/></td>
                    <td style="width:160px"><select id="productBase" style="width:99%;"/></td>
                    <td align="right" style="width: 100px"><s:text name="version"/></td>
                    <td style="width:200px"><select id="productRelease" style="width:99%;"/></td>
                    <td>&nbsp;</td>
                </tr>
            </table>
        </td>
    </tr>
</table>