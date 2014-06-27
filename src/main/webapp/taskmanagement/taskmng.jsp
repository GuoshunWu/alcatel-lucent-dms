<div class="dms-panel" id="taskmng">
    <table border="0" style="border-color: black; height:100%; width: 100%;">
        <tr>
            <td style="height:20px;">
                <table border="0" width="100%">
                    <tr>
                        <td colspan="4">
                            <span class="show-label" id="typeLabel">
                                <s:text name="product"/>
                            </span>

                            <span class="show-label" id="versionTypeLabel">
                                <s:text name="context.prod"/>
                            </span>
                            &nbsp;&nbsp;&nbsp;&nbsp;
                            <span class="show-label">
                            <s:text name="version"/>
                            </span>

                            <s:select theme="simple" id="selVersion" list="products" listKey="id"
                                      listValue="version"
                                      cssStyle="width:200px" headerKey="-1"
                                      headerValue="%{getText('product.version.select.head')}"
                                      value="curProductId"/>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
        <tr >
            <td valign="top" align="center" class="taskGrid_parent" >
                <table id="taskGrid">
                    <tr>
                        <td></td>
                    </tr>
                </table>
                <div id="taskPager"/>
                <form id="downloadTaskFiles" action="app/download-app-dict" method="post">
                    <input type="hidden" id="fileLoc" name="fileLoc"/>
                </form>

            </td>
        </tr>
    </table>
    <%@include file="dialogs.jsp" %>
</div>