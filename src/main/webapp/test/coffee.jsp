<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page contentType="text/html;charset=utf-8" %>
<html xmlns="http://www.w3.org/1999/xhtml">
<%@ taglib prefix="s" uri="/struts-tags" %>

<head>
    <meta http-equiv="Content-Type" content="text/html;charset=utf-8"/>
    <style type="text/css">
        .html, body {
            font-family: 'Verdana', '宋体';
            font-size: 10px;
        }
    </style>
    <script type="text/javascript">
        function langSelecter_onChanged() {
            document.getElementById("langForm").submit();
        }
    </script>

</head>
<body>
<%-- 设置SESSION_LOCALE为用户session中的WW_TRANS_I18N_LOCALE属性值 --%>
<s:set name="SESSION_LOCALE" value="#session['WW_TRANS_I18N_LOCALE']"/>

locale: <s:property value="#parameters['request_locale']"/><br/>
session locale: <s:property value="#session['WW_TRANS_I18N_LOCALE']"/>

</body>
</html>