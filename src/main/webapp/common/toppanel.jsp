<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page contentType="text/html;charset=utf-8" %>

<style type="text/css">
    .north-table {
        width: 100%;
        height: 100%;
        text-align: center;
        border-spacing: 0 !important;
        margin: 0;
        background: #D6D6D6 url(css/jqueryLayout/images/d6d6d6_40x100_textures_02_glass_80.png) 0 50% repeat-x;
    }
</style>

<table class="north-table">
    <tr class="top-bar">
        <td>
            <span class="page-title">
                <s:property value="naviPages[naviTo]"/>
            </span>
        </td>
        <td>
            <div class="navigator-bar">
                <s:iterator value="naviPages">
                    <span class='navigator-button' id="navi<s:property value="key.split('\\\.')[0]"/>Tab"
                          value="${key}">
                        <img class="navigator-tab-image"
                             src="images/navigator/icon_<s:property value="key.split('\\\.')[0].toLowerCase()"/>.png"
                             alt="Loading...">
                        <s:if test="naviTo==key">
                            <s:set var="tmpClass" scope="page" value="'navigator-tab-title-currentpage'"/>
                        </s:if>
                        <s:else>
                            <s:set var="tmpClass" scope="page" value="'navigator-tab-title'"/>
                        </s:else>
                        <span value="${key}" class="${tmpClass}">
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

            <%--<s:form id="langForm" action="" method="post" theme="simple">--%>
            <%--<select name="request_locale" onchange="return $('#langForm').submit();">--%>
            <%--<option value="en_US">English</option>--%>
            <%--<option value="zh_CN">Chinese</option>--%>
            <%--</select>--%>
            <%--</s:form>--%>
            <%--WW_TRANS_I18N_LOCALE: <s:property value="session['WW_TRANS_I18N_LOCALE']"/>--%>
            <%--request_locale: <s:property value="#parameters.request_locale"/>--%>

            <div style="margin-top:5px">
                <s:text name="header.welcome"/>&nbsp;
                <span style="color:#800080;font-weight:bold"><s:property
                        value="#session['user_context'].user.name"/></span>&nbsp;&nbsp;
                <a href='<s:url action="logout" namespace="/login"/>'><s:text name="header.logout"/></a>
            </div>
        </td>
    </tr>
    <tr/>
    <%-- a trial toolbar--%>
    <%--<tr>--%>
    <%--<td colspan="4" style="padding: 0;">--%>
    <%--<ul class="toolbar">--%>
    <%--<li id="tbarToggleNorth" class="first"><span></span>Toggle NORTH</li>--%>
    <%--<li id="tbarOpenSouth"><span></span>Open SOUTH</li>--%>
    <%--<li id="tbarCloseSouth"><span></span>Close SOUTH</li>--%>
    <%--<li id="tbarPinWest"><span></span>Pin/Unpin WEST</li>--%>
    <%--<li id="tbarPinEast" class="last"><span></span>Pin/Unpin EAST</li>--%>
    <%--</ul>--%>
    <%--</td>--%>
    <%--</tr>--%>
</table>

