<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<link rel="shortcut icon" href="favicon.ico" type="image/x-icon"/>

<script type="text/javascript">
    var gridI18NMap = { 'zh-cn':'cn', 'en-us':'en'};
    function paramWarn(paramName, defaultVal) {
        if (console && console.log) console.log("Couldn't get " + paramName + " parameter form server, use default: " + defaultVal);
    }
    var param = {
        naviTo:'<s:property value="naviTo"/>',
        locale:'<s:property value="clientParams.locale"/>'.replace('_', '-').toLocaleLowerCase(),
        forbiddenPrivileges:'<s:property value="clientParams.forbiddenPrivileges"/>'.split(','),
        buildNumber:<s:property value="buildNumber"/>,
        currentSelected:{
            productBaseId:'<s:property value="curProductBaseId"/>',
            productId:'<s:property value="curProductId"/>'
        }
    }

    var ROLE = {
        GUEST:0,
        APPLICATION_OWNER:1,
        TRANSLATION_MANAGER:2,
        ADMINISTRATOR:4
    }

    <s:if test="#session['user_context'].user">

    param.user = {
        name:'<s:property value="#session['user_context'].user.name"/>',
        role:<s:property value="#session['user_context'].user.role"/>
    }
    </s:if>


    if (!param.currentSelected.productBaseId) {
        param.currentSelected.productBaseId = -1;
        paramWarn('productBaseId', param.currentSelected.productBaseId);
    }

    if (!param.currentSelected.productId) {
        param.currentSelected.productId = -1;
        paramWarn('productId', param.currentSelected.productId);
    }

    if (!param.naviTo) {
        param.naviTo = 'appmng.jsp';
        paramWarn('naviTo', param.naviTo);
    }

    if (!param.naviTo) {
        param.naviTo = 'appmng.jsp';
        paramWarn('naviTo', param.naviTo);
    }
    if (!param.locale) {
        param.locale = 'en-us';
        paramWarn('locale', param.locale);
    }
    if (!param.buildNumber) {
        param.buildNumber = '-1';
        paramWarn('buildNumber', param.buildNumber);
    }
    param.i18ngridfile = 'i18n/grid.locale-' + (gridI18NMap[param.locale] ? gridI18NMap[param.locale] : param.locale);
</script>


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