<style type="text/css">
    #navigation-table {
        width: 100%;
        height: 100%;
        text-align: center;
        border-spacing: 0 !important;
        margin: 0;
        background: #D6D6D6 url(css/jqueryLayout/images/d6d6d6_40x100_textures_02_glass_80.png) 0 50% repeat-x;
    }
</style>


<div id="sessionTimeoutDialog" style="display: none" title="<s:text name="sessiontimout.title" />">
    <table style="width: 100%;height: 100%">
        <tr>
            <td>&nbsp;</td>
        </tr>
        <tr>
            <td align="center"><span id="timeOutMsg" style="color: red"><s:text name="sessiontimout.msg"/></span></td>
        </tr>
    </table>
</div>

<table id="navigation-table" border="0">
    <tr class="top-bar">
        <td style="width: 300px">
            <span class="page-title"><s:text name="appmng.title"/></span>
        </td>
        <td>
            <div class="navigator-bar">
                <s:iterator value="naviPages">
                    <s:set var="name" value="key.split('\\\.')[0].toLowerCase()"/>
                    <span class='navigator-button' id="navi${name}Tab" value="${name}">
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
            </s:form>

            <div style="margin-top:5px;margin-right: 15px">
                <s:text name="header.welcome"/>&nbsp;
                <span style="color:#800080;font-weight:bold"><s:property
                        value="#session['user_context'].user.name"/></span>&nbsp;&nbsp;
                <a href='<s:url action="logout" namespace="/login"/>'><s:text name="header.logout"/></a>
            </div>


        </td>
    </tr>
    <tr/>
</table>

