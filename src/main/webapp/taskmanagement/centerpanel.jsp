<%@ taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page contentType="text/html;charset=utf-8" %>

<%--dialogs--%>
<table width="100%" border="0" style="border-color: black">
    <tr>
        <td>
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
    <tr>
        <td>
            <table width="100%" border="0" style="border-color: yellow">
                <tr>
                    <td align="center" colspan="9" style="width: 100%">

                        <table id="taskGrid">
                            <tr>
                                <td/>
                            </tr>
                        </table>
                        <div id="taskPager"/>
                    </td>
                </tr>
                <tr>
                    <td>
                        <form id="downloadTaskFiles" action="app/download-app-dict" method="post">
                            <input type="hidden" id="fileLoc" name="fileLoc"/>
                        </form>
                    </td>
                </tr>
            </table>
        </td>
    </tr>
</table>

