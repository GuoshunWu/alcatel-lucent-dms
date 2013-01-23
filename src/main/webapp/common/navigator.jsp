<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page contentType="text/html;charset=utf-8" %>

<style type="text/css">
    #navitation-table {
        width: 100%;
        height: 100%;
        text-align: center;
        border-spacing: 0 !important;
        margin: 0;
        background: #D6D6D6 url(css/jqueryLayout/images/d6d6d6_40x100_textures_02_glass_80.png) 0 50% repeat-x;
    }
</style>

<table id="navitation-table" border="0">
    <tr class="top-bar">
        <td style="width: 300px">
            <span class="page-title"><s:text name="appmng.title"/></span>
        </td>
        <td>
            <div class="navigator-bar">
                <s:iterator value="naviPages">
                    <s:set var="name" value="key.split('\\\.')[0].toLowerCase()"/>

                    <span class='navigator-button' id="navi${name}Tab" value="${key}">
                        <img class="navigator-tab-image" src="images/navigator/icon_${name}.png" alt="Loading...">
                        <span class="navigator-tab-title">
                            <s:property value="value.replace('Management', '').trim()"/>
                        </span>
                    </span>
                </s:iterator>
            </div>
        </td>
        <td align="right">
            <div id="switcher"></div>
        </td>
        <td align="right">

            <s:form id="naviForm" theme="simple" cssStyle="display: none" action="entry" namespace="/" method="post">
                <label for="pageNavigator"><s:text name="header.currentView"/></label>
                <s:select key="header.currentView" list="naviPages" id="pageNavigator" name="naviTo" value="naviTo"/>
                <s:hidden id="curProductBaseId" name="curProductBaseId"/>
                <s:hidden id="curProductId" name="curProductId"/>
            </s:form>

            <div style="margin-top:5px">
                <s:text name="header.welcome"/>&nbsp;
                <span style="color:#800080;font-weight:bold"><s:property
                        value="#session['user_context'].user.name"/></span>&nbsp;&nbsp;
                <a href='<s:url action="logout" namespace="/login"/>'><s:text name="header.logout"/></a>
            </div>
        </td>
    </tr>
    <tr/>
</table>

