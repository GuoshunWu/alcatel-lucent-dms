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
        #optional-container {
            /*margin-top: 2%;*/
            /*margin-left: 2%;*/
            width: 100%;
            height: 100%;
        }
        .ui-layout-center {
            overflow: hidden;
        }

    </style>
    <script type="text/javascript" src="/js/lib/jquery-1.7.2.min.js"></script>
    <script type="text/javascript" src="/js/lib/jquery.layout-latest.js" ></script>
    <script type="text/javascript">
//        $(function(){
//            $("#optional-container").layout({resizable: true, closable: true});
$('body').layout({ applyDemoStyles: true });
//        });

        $(document).ready(function () {
            $('body').layout({ applyDemoStyles: true });
        });

    </script>
</head>
<body>

<div class="ui-layout-center">Center
    <p><a href="http://layout.jquery-dev.net/demos.html">Go to the Demos page</a></p>
    <p>* Pane-resizing is disabled because ui.draggable.js is not linked</p>
    <p>* Pane-animation is disabled because ui.effects.js is not linked</p>
</div>
<div class="ui-layout-north">North</div>
<div class="ui-layout-south">South</div>
<div class="ui-layout-east">East</div>
<div class="ui-layout-west">West</div>


<%--<!--[if IE 5]>--%>
<%--<div id="ie5" class="ie"><![endif]-->--%>
<%--<!--[if IE 6]>--%>
<%--<div id="ie6" class="ie"><![endif]-->--%>
<%--<!--[if IE 7]>--%>
<%--<div id="ie7" class="ie"><![endif]-->--%>

<%--<div id="optional-container">--%>
    <%--<div class="ui-layout-north" style="text-align: left; bottom:0px">--%>
        <%--NorthCCC--%>
    <%--</div>--%>

    <%--<div id="ui_center" class="ui-layout-center">--%>
        <%--Center--%>
    <%--</div>--%>
    <%--&lt;%&ndash;<div class="ui-layout-west"></div>&ndash;%&gt;--%>
    <%--&lt;%&ndash;<div class="ui-layout-south"> South</div>&ndash;%&gt;--%>
<%--</div>--%>
<%--<!--[if lte IE 7]></div><![endif]-->--%>

</body>
</html>