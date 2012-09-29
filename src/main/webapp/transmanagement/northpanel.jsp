<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page contentType="text/html;charset=utf-8" %>

<table width="99%" border="0">
    <tr>
        <td colspan="2">
<span style="font-family:fantasy,verdana, '黑体'; font-size:14pt; font-style:normal; ">
<s:text name="transmng.title"/>
</span>
        </td>
        <td align="right" colspan="2">
            <div id="switcher"></div>
        </td>
        <td>
            <%@include file="/common/pagenavigator.jsp" %>
        </td>
    </tr>
    <tr>
        <td align="right"><s:text name="product"/></td>
        <td style="width:160px"><select id="productBase" style="width:99%;"/></td>
        <td align="right" style="width: 100px"><s:text name="version"/></td>
        <td style="width:200px"><select id="productRelease" style="width:99%;"/></td>
        <td style="width:50px"></td>
    </tr>
</table>



