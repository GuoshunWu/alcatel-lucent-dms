<!DOCTYPE html>
<html>
<head>
    <title>jQuery UI Dialog</title>

    <link rel="stylesheet" type="text/css" href="../css/main.css"/>
    <script type="text/javascript" src="../js/lib/require.js"></script>

</head>
<body>

<%@include file="../common/maskdiv.jsp" %>
<%@include file="../common/commonDialogs.jsp" %>

<div id="global-container">
    <div class="ui-layout-north">
        <%@include file="../common/navigator.jsp" %>
    </div>
    <div id="ui_center" class="ui-layout-center">
        <div class="ui-layout-content" id="globalUILayoutContent">
            <%@include file="../appmanagement/appmng.jsp" %>
            <%@include file="../transmanagement/transmng.jsp" %>
            <%@include file="../taskmanagement/taskmng.jsp" %>
            <%@include file="../contextmanagement/contextmng.jsp" %>
            <%@include file="../admin/admin.jsp" %>
        </div>
    </div>
    <div class="ui-layout-west">
        <div class="header">Navigation Tree</div>
        <div class="ui-layout-content">
            <div id="appTree" style="background-color: transparent;"></div>
        </div>
        <%--<div class="footer">A test footer</div>--%>
    </div>
</div>

<script type="text/javascript">

    require(['../js/config.js', '../js/lib/domReady.js'], function(config, domReady, r) {
        return domReady(function() {
            require(['../js/main'], function(){
                alert("done...");
            });
        });
    });
</script>


</body>
</html>