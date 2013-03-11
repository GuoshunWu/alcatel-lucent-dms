<div class="dms-panel" id="transmng">
    <%@include file="dialogs.jsp" %>

    <table style="width: 100%; height:100%;padding: 10px;border: 0px solid red">
        <%--<tr style="height: 20px">--%>
        <%--<td style="font-weight:bold;font-size: medium;"><s:text name="transmng.summarypanel.summary"/></td>--%>
        <%--</tr>--%>
        <tr style="height:20px;">
            <td>
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
                    <tr>
                        <td style="width: 130px">
                            <button id="languageFilter"><s:text name="transmng.summarypanel.languagefilter"/></button>
                        </td>
                        <td style="width: 250px">
                            <div style="border: none;width: 280px;text-align: center">
                                <input type="radio" id="applicationView" name="viewOption" value="app"><label
                                    for="applicationView"><s:text
                                    name="transmng.summarypanel.viewoption.applicationlevel"/></label>&nbsp;&nbsp;&nbsp;&nbsp;
                                <input type="radio" id="dictionaryView" checked name="viewOption"
                                       value="dict"><label
                                    for="dictionaryView"><s:text
                                    name="transmng.summarypanel.viewoption.dictionarylevel"/></label>
                            </div>
                        </td>
                        <td style="display: none;width: 240px"><s:text name="transmng.summarypanel.searchtext"/><input/>
                        </td>
                        <td><a id="exportExcel" href="#"><img src="images/excel.gif"/><s:text name="export"/></a></td>
                        <td>
                            <a style="display: none" id="exportPDF" href="#">PDF
                                <form id="exportForm" method="post" action="trans/export-translation-report">
                                    <input type="hidden" name="prod"/>
                                    <input type="hidden" name="type"/>
                                    <input type="hidden" name="language"/>
                                    <input type="hidden" name="ftype"/>
                                </form>
                            </a>
                        </td>
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

