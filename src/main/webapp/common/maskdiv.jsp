<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page contentType="text/html;charset=utf-8" %>

<style type="text/css">

    #loading-container {
        width: 100%;
        height: 100%;
        text-align: center;
    }

    .over-lay {
        width: 300px;
        height: 200px;
        margin-left: auto;
        margin-right: auto;
        /*background-color: white;*/
        /*opacity: 0.5;*/
        /*filter:alpha(opacity=50);*/
        cursor: wait
    }

</style>

<table id="loading-container">
    <tbody>
    <tr>
        <td style="vertical-align: middle;">
            <table class="over-lay">
                <tbody>
                <tr>
                    <td style="vertical-align: middle;">
                        <img src="images/wait/7.gif" alt="gif" style="vertical-align: middle;"/>
                        <s:text name="loading"/>
                    </td>
                </tr>
                </tbody>
            </table>
        </td>
    </tr>
    </tbody>
</table>