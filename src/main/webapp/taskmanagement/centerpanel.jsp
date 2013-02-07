<table border="0" style="border-color: black; height:100%; width: 100%;">
    <tr>
        <td style="height:20px;">
            <table border="0" width="100%">
                <tr>
                    <td align="right" style="width:80px"><s:text name="product"/></td>
                    <td style="width:160px">
                        <s:select theme="simple" id="productBase" list="productBases" listKey="id" listValue="name"
                                  cssStyle="width:99%;" headerKey="-1" headerValue="%{getText('product.select.head')}"
                                  value="curProductBaseId"/>
                    </td>
                    <td align="right" style="width: 100px"><s:text name="version"/></td>
                    <td style="width:200px">
                        <s:select theme="simple" id="productRelease" list="products" listKey="id" listValue="version"
                                  cssStyle="width:99%;" headerKey="-1"
                                  headerValue="%{getText('product.version.select.head')}"
                                  value="curProductId"/>
                    </td>
                    <td>&nbsp;</td>
                </tr>
            </table>
        </td>
    </tr>
    <tr>
        <td valign="top" align="center" class="taskGrid_parent">
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

