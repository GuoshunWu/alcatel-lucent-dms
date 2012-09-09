<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%--
  Created by IntelliJ IDEA.
  User: guoshunw
  Date: 12-8-7
  Time: 上午11:00
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <c:set scope="page" var="pageTitle">Application Management</c:set>

    <title>DMS->${pageTitle}</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta http-equiv="Pragma" content="no-cache">

    <link rel="stylesheet" type="text/css" href="css/appmanagement.css">

    <script type="text/javascript" src="js/jquery-1.7.2.min.js"></script>
    <script type="text/javascript" src="js/jquery-ui-1.8.22.custom.min.js"></script>

    <script type="text/javascript" src="js/dms-util.js"></script>

    <script type="text/javascript" src="js/i18n/grid.locale-en.js"></script>
    <script type="text/javascript" src="js/jquery.jqGrid.min.js"></script>

    <script type="text/javascript" src="js/jquery.jstree.js"></script>
    <script type="text/javascript" src="js/jquery.cookie.js"></script>
    <script type="text/javascript" src="js/jquery.hotkeys.js"></script>

    <script type="text/javascript" src="js/jquery.layout-latest.js"></script>

    <script type="text/javascript" src="js/jquery.easy-confirm-dialog.js"></script>
    <script type="text/javascript" src="js/jquery.msgBox.v1.js"></script>

    <script type="text/javascript" src="js/combobox.js"></script>
    <script type="text/javascript" src="js/appmng.js"></script>

    <%--<script type="text/javascript" src="js/themeswitchertool.js"></script>--%>

    <%--<script type="text/javascript"--%>
            <%--src="http://jqueryui.com/themeroller/themeswitchertool/">--%>
    <%--</script>--%>
    <%--<script type="text/javascript" >--%>
        <%--$(document).ready(function(){--%>
            <%--$('#switcher').themeswitcher();--%>
        <%--});--%>
    <%--</script>--%>
</head>
<body>

<!--[if IE 5]>
<div id="ie5" class="ie"><![endif]-->
<!--[if IE 6]>
<div id="ie6" class="ie"><![endif]-->
<!--[if IE 7]>
<div id="ie7" class="ie"><![endif]-->

<%-- All the dialogs here --%>
<div id="newProductDialog" title="New product">
    <span> Product name<input id="productName" value="" type="text"></span>
</div>
<div id="newProductReleaseDialog" title="New product release">
    <table>
        <tr>
            <td> Version name</td>
            <td><input id="versionName" value="" type="text"></td>
        </tr>
        <tr>
            <td>Duplicate all applications from a previous version</td>
            <td><select id="dupVersion"></select></td>
        </tr>
    </table>
</div>

<div id="newOrAddApplicationDialog" title="New or add application">
    <table>
        <tr>
            <td><label>Application name</label></td>
            <td>
                <div class="ui-widget">
                    <select id="applicationName" class="ui-widget"></select>
                </div>
        </tr>
        <tr>
            <td><label>Version</label></td>
            <td>
                <select id="version" class="ui-widget"></select>
            </td>
            </td>
        </tr>
    </table>
</div>


<div id="optional-container">
    <div class="ui-layout-north" style="text-align: left; bottom:0px">
        <span style="font-family:fantasy; font-size:14pt; font-style:normal; ">${pageTitle}</span>
        <span><div id="switcher"></div></span>
    </div>

    <div id="ui_center" class="ui-layout-center">
        <div id="DMS_welcomePanel">
            <table align="center">
                <tr>
                    <td align="center">
                        <span style="font-size:30pt; font-style:normal;color: #6a5acd; ">WELCOME TO DMS</span>
                    </td>
                </tr>
                <tr>
                    <td><img src="images/Books.png"/></td>
                </tr>
            </table>
        </div>
        <div id="DMS_productPanel">

            <table border="0">
                <tr>
                    <td style="width:30px;">Product:</td>
                    <td><span id="dispProductName"></span></td>

                </tr>
                <tr>
                    <td>Version:</td>
                    <td><select id="selVersion"></select>
                        <button id="newVersion"></button>
                    </td>

                </tr>
                <tr>
                    <td>TestBtn:</td>
                    <td>
                        <button id="newApp">New App</button>
                        <button id="addApp">Add App</button>
                        <button id="removeApp">Remove App</button>
                        <button id="download">Download</button>
                    </td>
                </tr>
                <tr>
                    <td valign="top" colspan="2">
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

        <div id="DMS_applicationPanel">

            <table border="0">
                <tr>
                    <td style="width:30px;">Product:</td>
                    <td colspan="2"><span id="appDispProductName"></span></td>
                </tr>
                <tr>
                    <td>Application:</td>
                    <td><span id="appDispAppName"></span></td>
                    <td align="left">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                        Version:<select id="selAppVersion"></select>
                    </td>
                </tr>
                <tr>
                    <td colspan="2">Deliver app dictionary</td>
                    <td><input type="file" name="dctFileChooser" title="Chose file: "/>
                        <button id="dictUpload">Upload...</button>
                    </td>
                </tr>
                <tr>
                    <td colspan="3">
                        <div id="dictionaryGrid">
                            <table id="dictionaryGridList">
                                <tr>
                                    <td/>
                                </tr>
                            </table>
                            <div id="dictPager"></div>
                        </div>
                    </td>
                </tr>

            </table>


        </div>

    </div>

    <div class="ui-layout-west">
        <p>&nbsp;</p>

        <div id="appTree" style="background-color: transparent;"></div>
        <p>&nbsp;</p>
        <button id="newProduct">New Product...</button>

    </div>

    <%--<div class="ui-layout-south"> South</div>--%>

</div>
<!--[if lte IE 7]></div><![endif]-->

</body>
</html>