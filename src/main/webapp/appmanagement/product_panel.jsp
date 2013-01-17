<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page contentType="text/html;charset=utf-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div id="DMS_productPanel" class="dms_appmng_panel">

    <table border="0">
        <tr>
            <td>
                <span class="show-label"><s:text name="product"/></span>
                <span class="show-label" id="dispProductName"></span>
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                <label class="show-label" for="selVersion"><s:text name="version"/></label>
                <select id="selVersion"></select>
                <button id="newVersion" title="<s:text name="appmng.newproduct"/> "></button>
                &nbsp;&nbsp;
                <button id="removeVersion" title="<s:text name="appmng.removeproduct"/>"></button>
            </td>

        </tr>

        <tr>
            <td valign="top">
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
