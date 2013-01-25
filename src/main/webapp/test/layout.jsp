<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page contentType="text/html;charset=utf-8" %>
<html xmlns="http://www.w3.org/1999/xhtml">
<%@ taglib prefix="s" uri="/struts-tags" %>
<s:set var="base" value="'..'" scope="page"/>

<head>
    <meta http-equiv="Content-Type" content="text/html;charset=utf-8"/>
    <style type="text/css">
        html, body {
            font-family: 'Verdana', '宋体';
            font-size: 10px;
            height: 99%;
            margin: 0px;
        }

        .header {
            background: #80ade5 url(${base}/css/jqueryLayout/images/80ade5_40x100_textures_04_highlight_hard_100.png) 0 50% repeat-x;
            border-bottom: 1px solid #777;
            font-weight: bold;
            text-align: center;
            padding: 2px 0 4px;
            position: relative;
            overflow: hidden;
        }

        .subhead, .footer {
            background: #d6d6d6 url(${base}/css/jqueryLayout/images/d6d6d6_40x100_textures_02_glass_80.png) 0 50% repeat-x;
            padding: 3px 10px;
            font-size: 0.85em;
            position: relative;
            overflow: hidden;
            white-space: nowrap;
        }

        .subhead {
            border-bottom: 1px solid #777;
        }

        .footer {
            border-top: 1px solid #777;
        }

        #layout-container {
            width: 100%;
            height: 100%;
        }

        .ui-layout-center {
            /*overflow: hidden;*/
        }

        div.panel {
            border: 1px solid red;
            height: 99%;
            width: 100%;
        }
    </style>


    <link href="${base}/css/jqueryUI/themes/base/jquery.ui.all.css" rel="stylesheet" type="text/css"/>
    <script type="text/javascript" src="${base}/js/lib/jquery-1.7.2.min.js"></script>
    <script type="text/javascript" src="${base}/js/lib/jquery-ui-1.8.22.custom.min.js"></script>
    <script type="text/javascript" src="${base}/js/lib/jquery.layout-latest.min.js"></script>
    <script type="text/javascript" src="layouttest.js"></script>

</head>
<body>

<div class='dialog' id="testDialog">This is a panel.
</div>


<div id="layout-container">
    <div class="ui-layout-center">
        <div class='header'>
            I am a header
        </div>
        <div class="ui-layout-content">
            <div class='panel' id='p1' style="height:200%;">
                <p>
                    I am the panel1.
                </p>
            </div>
            <div class='panel' id='p2'>
                <div class="testTabs">
                    <ul>
                        <li><a href="#langAdmin"><s:text name="admin.language.title"/></a></li>
                        <li><a href="#charsetAdmin"><s:text name="admin.charset.title"/></a></li>
                        <li><a href="#userAdmin"><s:text name="admin.user.title"/></a></li>
                    </ul>

                    <div id="langAdmin" style="border: 1px solid pink;">
                        <table style="width:100%; height: 100%; border: 1px solid green;">
                            <tr>
                                <td>
                                    I am in table.<br/>
                                    I am in table.<br/>
                                    I am in table.<br/>
                                    I am in table.<br/>
                                    I am in table.<br/>
                                </td>
                            </tr>
                        </table>
                    </div>
                    <div id="charsetAdmin">
                        charset
                    </div>
                    <div id="userAdmin">
                        useradmin
                    </div>
                </div>
            </div>
        </div>
        <div class='footer'>
            I am a footer.
        </div>
    </div>
    <div class="ui-layout-north">North
        <button id="switchPanel">SwitchPanel</button>
    </div>
    <div class="ui-layout-east">East</div>
    <div class="ui-layout-west">West</div>
    <div class="ui-layout-south">South</div>
</div>

</body>
</html>