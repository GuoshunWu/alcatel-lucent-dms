<div id="ctxReferencesDialog" title="<s:text name='ctxmng.dialogs.references.title'/> ">
    <table>
        <tr>
            <td><s:text name="ctxmng.dialogs.reftext"/><span id="refText"></span></td>
        </tr>
        <tr>
            <td><s:text name="ctxmng.dialogs.references.linkwith"/></td>
        </tr>
        <tr>
            <td>
                <table id="refLinkGrid">
                    <tr>
                        <td></td>
                    </tr>
                </table>
                <div id="refLinkGridPager"></div>
            </td>
        </tr>
    </table>
</div>

<div id="ctxTranslationsDialog" title="<s:text name='ctxmng.dialogs.translations.title'/> ">
    <table>
        <tr>
            <td><s:text name="ctxmng.dialogs.reftext"/><span id="transRefText"></span></td>
        </tr>
        <tr>
            <td>
                <table id="transLinkGrid">
                    <tr>
                        <td></td>
                    </tr>
                </table>
                <div id="transLinkGridPager"></div>
            </td>
        </tr>
    </table>
</div>


<div id="ctxLanguagesDialog" title="<s:text name='ctxmng.dialogs.languages.title'/> ">
    <table>
        <tr>
            <td>
                <table id="languagesLinkGrid">
                    <tr>
                        <td></td>
                    </tr>
                </table>
                <div id="languagesLinkGridPager"></div>
            </td>
        </tr>
    </table>
</div>


<div id="ctxDifferencesDialog" title="<s:text name='ctxmng.dialogs.differences.title'/> ">
    <table>
        <tr>
            <td>
                <table id="diffLinkGrid">
                    <tr>
                        <td></td>
                    </tr>
                </table>
                <div id="diffLinkGridPager"></div>
            </td>
        </tr>
    </table>
</div>

<div id="ctxMergesDialog" title="<s:text name='ctxmng.dialogs.merge.title'/> ">
    <form>
          <fieldset id="contexts">
              <legend>Which context do you want to merge? </legend>
              <div style="float: left">
                  <label id="ctxALabel" for="contextA" style="vertical-align: middle"></label>
                  <input id="contextA" checked type="radio" name="contextGrp" style="vertical-align: middle"/>
              </div>
              <div style="float: right">
                  <label id="ctxBLabel" for="contextB" style="vertical-align: middle"></label>
                  <input id="contextB" type="radio" name="contextGrp" style="vertical-align: middle"/>
              </div>
          </fieldset>
    </form>
</div>


