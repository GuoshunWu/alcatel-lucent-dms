<%@ taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page contentType="text/html;charset=utf-8" %>

<%--dialogs--%>
<table width="100%" border="0" style="border-color: black">
    <tr>
        <td style="font-weight:bold;font-size: medium;"><s:text name="transmng.summarypanel.summary"/></td>
    </tr>
    <tr>
        <td>
            <table border="0" width="100%" style="border-color: red">
                <tr>
                    <td style="width:18px;">
                        <%--Base: <s:property value="productBases"/><br/>--%>
                        <%--Product:<s:property value="products"/>--%>
                    </td>
                    <td style="width: 130px">
                        <button id="languageFilter"><s:text name="transmng.summarypanel.languagefilter"/></button>
                    </td>
                    <td style="width: 60px" align="right"><s:text name="product"/></td>
                    <td style="width:160px">
                        <s:select theme="simple" id="productBase" list="productBases" listKey="id" listValue="name"
                                  cssStyle="width:99%;" headerKey="-1" headerValue="%{getText('product.select.head')}"
                                value="curProductBaseId" />
                    </td>
                    <td align="right" style="width: 50px"><s:text name="version"/></td>
                    <td style="width:200px">
                        <s:select theme="simple" id="productRelease" list="products" listKey="id" listValue="version"
                                  cssStyle="width:99%;" headerKey="-1" headerValue="%{getText('product.version.select.head')}"
                                  value="curProductId" />
                    </td>
                    <td style="width: 250px">
                        <div style="border: none;width: 280px;text-align: center">
                            <input type="radio" id="applicationView" name="viewOption" value="application"><label
                                for="applicationView"><s:text
                                name="transmng.summarypanel.viewoption.applicationlevel"/></label>&nbsp;&nbsp;&nbsp;&nbsp;
                            <input type="radio" id="dictionaryView" checked name="viewOption" value="dictionary"><label
                                for="dictionaryView"><s:text
                                name="transmng.summarypanel.viewoption.dictionarylevel"/></label>
                        </div>
                    </td>
                    <td style="width: 240px"><s:text name="transmng.summarypanel.searchtext"/><input/></td>
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
        <td>&nbsp;</td>
    </tr>
    <tr>
        <td>
            <table width="100%" border="0" style="border-color: yellow">
                <tr>
                    <td align="center" colspan="9" style="width: 100%">
                        <table id="transGrid">
                            <tr>
                                <td/>
                            </tr>
                        </table>
                        <div id="transPager"/>
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
                        <button id="create"><s:text name="button.translationtask"/></button>
                        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                        <s:text name="transmng.summarypanel.makelabelas"/>&nbsp;&nbsp;
                        <button id='makeLabelTranslated' value="2">T</button>
                        <button id="makeLabelNotTranslated" value="0">N</button>
                    </td>
                    <td style="width: 15px"/>
                </tr>
            </table>
        </td>
    </tr>
</table>

