<link rel="shortcut icon" href="favicon.ico" type="image/x-icon"/>
<script type="text/javascript">
    var gridI18NMap = { 'zh-cn':'cn', 'en-us':'en'};
    function paramWarn(paramName, defaultVal) {
        if (console && console.log) console.log("Couldn't get " + paramName + " parameter form server, use default: " + defaultVal);
    }
    var param = {
        naviTo:'<s:property value="naviTo"/>',
        locale:'<s:property value="clientParams.locale"/>'.replace('_', '-').toLocaleLowerCase(),
        version: '<s:property value="version"/>',
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
