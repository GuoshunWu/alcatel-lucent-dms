<div class="dms-panel" id='appmng.jsp'>
    <div id="appmng-container" style="width: 100%; height: 100%;">
        <div class="ui-layout-west" style="border-top: none">
            <div class="header">Navigation Tree</div>
            <div class="content">
                <div id="appTree" style="background-color: transparent;"></div>
            </div>
            <div class="footer">A test footer</div>
        </div>
        <div id="ui_center" class="ui-layout-center" style="border-top: none">
            <div class="content">
                <%@include file="welcome_panel.jsp" %>
                <%@include file="product_panel.jsp" %>
                <%@include file="application_panel.jsp" %>
            </div>
        </div>
    </div>

    <%@include file="dialogs.jsp" %>
</div>