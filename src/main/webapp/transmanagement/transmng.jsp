<div class="dms-panel" id="transmng">
    <table style="width: 100%; height:100%; border: 0px solid red">
        <tr style="height:20px;">
            <td>
                <table border="0" width="100%">
                    <tr>
                        <td width="480px">
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
                                      cssStyle="width:150px" headerKey="-1"
                                      value="curProductId"/>

                        </td>

                        <td  align="right">
                          <fieldset style="width: 590px">
                                <legend align="center"><s:text name="searchtext"/></legend>
                                <span class="translation-search-text">
                                <label for="transSearchText"><s:text name="searchtext"/></label>
                                <input id="transSearchText"/>
                                <s:text name="transmng.in"/>
                                <select id="transSearchTextLanguage" >
                                    <option value="1">
                                        <s:text name="transmng.ref"/>
                                    </option>
                                </select>
                                &nbsp;

                                <div style="display: inline">
                                    <s:text name="exact"/>: <input style="vertical-align: middle" type="checkbox"
                                                  id="transSearchText_exact"/>
                                </div>

                                <button id="transSearchAction"></button>

                                </span>

                            </fieldset>


                        </td>
                    </tr>
                </table>
                <table border="0" width="100%">
                    <tr>
                        <td>
                            <button id="languageFilter"><s:text name="transmng.summarypanel.languagefilter"/></button>

                            <fieldset style="border:none;width: 280px;text-align: center; display: inline">
                                <input type="radio" id="applicationView" name="viewOption" value="app"><label
                                    for="applicationView"><s:text
                                    name="transmng.summarypanel.viewoption.applicationlevel"/></label>&nbsp;&nbsp;&nbsp;&nbsp;
                                <input type="radio" id="dictionaryView" checked name="viewOption"
                                       value="dict"><label
                                    for="dictionaryView"><s:text
                                    name="transmng.summarypanel.viewoption.dictionarylevel"/></label>
                            </fieldset>

                            <a id="exportExcel" href="Javascript:void(0);">
                                <img src="images/excel.gif"/> <s:text name="report"/>
                            </a>

                            <a style="display: none" id="exportPDF" href="#">PDF
                                <form id="exportForm" method="post" action="trans/export-translation-report">
                                    <input type="hidden" name="prod"/>
                                    <input type="hidden" name="type"/>
                                    <input type="hidden" name="language"/>
                                    <input type="hidden" name="ftype"/>
                                </form>
                            </a>
                        </td>
                        <td align="right">
                            <button id="transHistories"
                                    title="<s:text name="transmng.translation.history"/>"></button>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
        <tr>
            <td valign="top" align="center" class="transGrid_parent">
                <table id="transGrid"></table>
            </td>
        </tr>
        <tr style="height:20px;">
            <td>
                <table border="0" style="border-color: blue">
                    <tr>
                        <td style="width: 15px"/>
                        <td>
                            <button id="create" style="width: 170px"><s:text name="button.translationtask"/></button>
                        </td>
                        <td>
                            <button id="exportTranslation" style="width:160px"><s:text name="button.exporttranslation"/></button>
                        </td>
                        <td>
                            <div id="importTranslation" style="width: 160px"></div>
                        </td>
                        <td align="left">
                            <ul id="translationStatus">
                                <li><a name="2" href="#"><s:text name="status.translated"/></a></li>
                                <li><a name="0" href="#"><s:text name="status.nottranslated"/></a></li>
                            </ul>
                            <button id='makeLabelTranslateStatus' style="width: 200px"><s:text
                                    name="transmng.summarypanel.makelabelas"/></button>
                        </td>
                        <td>
                            <button id="checkTranslations" style="width: 160px"><s:text name="transmng.chktrans"/>...</button>
                        </td>
                        <td style="width: 15px"/>
                    </tr>
                </table>
            </td>
        </tr>
    </table>
    <%@include file="dialogs.jsp" %>
</div>

