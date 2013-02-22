<div class="dms-panel" id="transmng">
    <table style="width: 100%; height:100%;padding: 10px;border: 2px solid red">
        <tr style="height: 20px">
            <td style="font-weight:bold;font-size: medium;"><s:text name="transmng.summarypanel.summary"/></td>
        </tr>
        <tr style="height:20px;">
            <td>
                <table border="0" width="100%">
                    <tr>
                        <td style="width:18px;">
                        </td>
                        <td style="width: 130px">
                            <button id="languageFilter"><s:text name="transmng.summarypanel.languagefilter"/></button>
                        </td>
                        <td style="width: 60px" align="right"><span id="versionTypeLabel"><s:text name="context.prod"/></span></td>
                        <%--<td style="width:160px">--%>
                            <%--<s:select theme="simple" id="productBase" list="productBases" listKey="id" listValue="name"--%>
                                      <%--cssStyle="width:99%;" headerKey="-1"--%>
                                      <%--headerValue="%{getText('product.select.head')}"--%>
                                      <%--value="curProductBaseId"/>--%>
                        <%--</td>--%>
                        <td align="right" style="width: 50px"><s:text name="version"/></td>
                        <td style="width:200px">
                            <s:select theme="simple" id="selVersion" list="products" listKey="id"
                                      listValue="version"
                                      cssStyle="width:99%;" headerKey="-1"
                                      headerValue="%{getText('product.version.select.head')}"
                                      value="curProductId"/>
                        </td>
                        <td style="width: 250px">
                            <div style="border: none;width: 280px;text-align: center">
                                <input type="radio" id="applicationView" name="viewOption" value="application"><label
                                    for="applicationView"><s:text
                                    name="transmng.summarypanel.viewoption.applicationlevel"/></label>&nbsp;&nbsp;&nbsp;&nbsp;
                                <input type="radio" id="dictionaryView" checked name="viewOption"
                                       value="dictionary"><label
                                    for="dictionaryView"><s:text
                                    name="transmng.summarypanel.viewoption.dictionarylevel"/></label>
                            </div>
                        </td>
                        <td style="display: none;width: 240px"><s:text name="transmng.summarypanel.searchtext"/><input/>
                        </td>
                        <td><a id="exportExcel" href="#"><img src="images/excel.gif"/><s:text name="export"/></a></td>
                        <td><a style="display: none" id="exportPDF" href="#">PDF
                            <form id="exportForm" method="post" action="trans/export-translation-report">
                                <input type="hidden" name="prod"/>
                                <input type="hidden" name="type"/>
                                <input type="hidden" name="language"/>
                                <input type="hidden" name="ftype"/>
                            </form>
                        </a></td>
                        <td style="width:18px;"></td>
                    </tr>
                </table>
            </td>
        </tr>
        <tr>
            <td valign="top" align="center" class="transGrid_parent">
                <table id="transGrid">
                    <tr>
                        <td></td>
                    </tr>
                </table>
                <div id="transPager"/>
            </td>
        </tr>
        <tr style="height:20px;">
            <td>
                <table width="100%" border="0" style="border-color: blue">
                    <tr>
                        <td style="width: 15px"/>
                        <td style="width: 410px">
                            <button id="create"><s:text name="button.translationtask"/></button>
                            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                            <button id="exportTranslation"><s:text name="button.exporttranslation"/></button>
                        </td>
                        <td align="left">
                            <ul id="translationStatus">
                                <li><a name="2" href="#"><s:text name="status.translated"/></a></li>
                                <li><a name="0" href="#"><s:text name="status.nottranslated"/></a></li>
                            </ul>
                            <button id='makeLabelTranslateStatus'><s:text
                                    name="transmng.summarypanel.makelabelas"/></button>
                        </td>
                        <td style="width: 15px"/>
                    </tr>
                </table>
            </td>
        </tr>
    </table>
</div>