<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<script type="text/coffeescript" src="js/transmng/grid.colmodel.coffee"></script>
<script type="text/coffeescript" src="js/transmng/trans_grid.coffee"></script>

<%--dialogs--%>
<table width="100%" border="0" style="border-color: black">
    <tr>
        <td style="font-weight:bold;font-size: medium;"><s:text name="transmng.summarypanel.summary"/></td>
    </tr>
    <tr>
        <table border="0" width="100%" style="border-color: red">
            <tr>
                <td style="width: 15px"/>
                <td>
                    <button id="languageFilter"><s:text name="transmng.summarypanel.languagefilter"/></button>
                </td>
                <td>&nbsp;</td>
                <td>View Option<select/></td>
                <td>&nbsp;</td>
                <td>Search text<input/></td>
                <td>&nbsp;</td>
                <td><a href="">Excel</a></td>
                <td>&nbsp;</td>
                <td><a href="">PDF</a></td>
                <td style="width: 15px"/>
            </tr>
        </table>
    </tr>
    <tr>
        <td>&nbsp;</td>
    </tr>
    <tr>
        <td>
            <table width="100%" border="0" style="border-color: yellow">
                <tr>
                    <td align="center" colspan="9" style="width: 98%">

                        <table id="taskGridList">
                            <tr>
                                <td/>
                            </tr>
                        </table>
                        <div id="taskPager"/>

                    </td>
                </tr>
            </table>
        </td>
    </tr>
    <tr>
        <td>
            <table width="100%" border="0" style="border-color: blue">
                <tr>
                    <td style="width: 15px"/>
                    <td>
                        <button id="create">Create translation task...</button>
                        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                        Make all label as<select/>
                    </td>
                    <td style="width: 15px"/>
                </tr>
            </table>
        </td>
    </tr>

</table>

