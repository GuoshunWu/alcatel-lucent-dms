<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<script charset="utf8" type="text/coffeescript" src="js/taskmng/task_grid.coffee"></script>

<table width="100%" border="0">
    <tr>
        <td colspan="11" style="font-weight:bold;font-size: medium;">Summary</td>
    </tr>
    <%--<tr>--%>
        <%--<td colspan="11">&nbsp;</td>--%>
    <%--</tr>--%>
    <tr>
        <td>&nbsp;&nbsp;</td>
        <td>Language Filter<select/></td>
        <td>&nbsp;</td>
        <td>View Option<select/></td>
        <td>&nbsp;</td>
        <td>Search text<input/></td>
        <td>&nbsp;</td>
        <td><a href="">Excel</a></td>
        <td>&nbsp;</td>
        <td><a href="">PDF</a></td>
        <td>&nbsp;&nbsp;</td>
    </tr>
    <%--<tr>--%>
        <%--<td colspan="11">&nbsp;</td>--%>
    <%--</tr>--%>
    <tr>
        <td>&nbsp;</td>
        <td align="center" colspan="9" style="width: 100%">

            <table id="taskGridList">
                <tr>
                    <td/>
                </tr>
            </table>
            <div id="taskPager"/>
        </td>
        <td>&nbsp;</td>
    </tr>
    <tr>
        <td>&nbsp;</td>
        <td colspan="9">
            <button>Create translation task...</button>
            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
            Make all label as<select/>
        </td>
        <td>&nbsp;</td>
    </tr>

</table>