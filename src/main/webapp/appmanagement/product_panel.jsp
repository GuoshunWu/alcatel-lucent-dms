<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page contentType="text/html;charset=utf-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div id="DMS_productPanel">

    <table border="0">
        <tr>
            <td style="width:30px;"><s:text name="product"/></td>
            <td><span id="dispProductName"></span></td>
        </tr>
        <tr>
            <td><s:text name="version"/></td>
            <td><select id="selVersion"></select>
                <button id="newVersion" title="<s:text name="appmng.newproduct"/> "></button>
                &nbsp;&nbsp;
                <button id="removeVersion" title="<s:text name="appmng.removeproduct"/>"></button>
            </td>
        </tr>
        <tr>
            <td valign="top" colspan="2">
                <div id="applicationGrid">
                    <table id="applicationGridList">
                        <tr>
                            <td/>
                        </tr>
                    </table>
                    <div id="pager"></div>
                </div>
            </td>
        </tr>
    </table>

</div>
