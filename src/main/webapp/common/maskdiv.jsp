<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page contentType="text/html;charset=utf-8" %>

<style type="text/css">
    .iphone {
        width: 300px;
        height: 180px;

        border: none;
        padding: 15px;
        /*background-color: #000;*/
        -webkit-border-radius: 10px;
        -moz-border-radius: 10px;
        opacity: 0.4;
        /*filter: alpha(opacity = 40);*/
        color: #fff;
    }

    .defaultCenterMask {
        padding: 0;
        margin: 0;

        width: 300px;
        height: 180px;

        text-align: center;
        color: #000;
        /*border: 3px solid #aaa;*/
        /*background-color: #f0f0f0;*/
        font-family:Trebuchet MS,Verdana,Arial, Helvetica;
        font-size:17px;
        cursor: wait
    }

    #overLay {
        width: 100%;
        height: 100%;
        /*background-color: #000;*/
        /*opacity: 0.5;*/
        cursor: wait
    }

</style>

<div id="loading-container">
    <table id="overLay" border="0">
        <tr>
            <td align="center" valign="center">
                <div id="centerMask" class="defaultCenterMask">
                    <table style="width: 100%;height: 100%">
                        <tr>
                            <td align="center" valign="middle">
                                <img src="images/wait/7.gif" alt="gif" style="vertical-align: middle;">
                                <s:text name="loading"/>
                            </td>
                        </tr>
                    </table>
                </div>
            </td>
        </tr>
    </table>
</div>