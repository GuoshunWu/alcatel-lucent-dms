<%@ taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page contentType="text/html;charset=utf-8" %>

<%--dialogs--%>
<table width="100%" border="1" style="border-color: black">
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
                            <option value="actions"><button>Study</button></option>
                            <option value="actions"><button>Two</button></option>
                        </select>
                    </td>
                    <td style="width: 120px;">
                        <%--@declare id="taskfileupload"--%><label for="taskFileUpload"><s:text name="appmng.deliverapp"/></label>
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

