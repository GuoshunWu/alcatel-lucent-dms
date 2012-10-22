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
            </table>
        </td>
    </tr>
    <tr>
        <td>
            <table width="100%" border="0" style="border-color: blue">
                <tr>
                    <td style="width:30px"></td>
                    <td>
                        <select id="actions">
                            <option value="actions">
                                <button>Study</button>
                            </option>
                            <option value="actions">
                                <button>Two</button>
                            </option>
                        </select>
                    </td>
                    <td>
                        <button id='transReport'>TransReoprt</button>
                        <style type="text/css">
                            <!--
                            .a1:link {
                                color: #FF0000;
                            }

                            .a1:visited {
                                color: #0000FF;
                            }

                            .a1:hover {
                                color: #990000;
                            }

                            .a1:active {
                                color: #993399;
                            }

                        </style>
                        <a href="#" class="a1">test</a>
                        <button id='viewDetail'>viewDetail</button>
                    </td>
                    <td style="width: 120px;">
                        <%--@declare id="taskfileupload"--%><label for="taskFileUpload"><s:text
                            name="appmng.deliverapp"/></label>
                    </td>
                    <td style="width: 100px;">
                        <div id="uploadTask"></div>
                    </td>
                    <td>
                        <span id="uploadTaskStatus">status</span>
                    </td>
                </tr>
            </table>
        </td>
    </tr>
</table>

