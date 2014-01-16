<style type="text/css">
    .tipLayout {
        width: 100%;
        height: 99%;
        overflow: hidden;
        margin: 0px;
        background:#F0F0F0;
        border: 1px solid gray;
    }

    #tipHeader {
        vertical-align: middle;
        height: 3em;
    }

    #tipContent {
        border: 1px solid #d3d3d3;
        background: #ffffff;
        /*display: block;*/
        overflow: auto;
        margin: 1em .5em 1em .5em;
        height: calc(100% - 5em);
    }


</style>
<div id="tipOfTheDayDialog" title="<s:text name="common.tipofday.title"/>">
    <div id="tipLayoutContainer" class="tipLayout">
        <div id="tipHeader">
            <img src="images/tips/ktip32.png" style="vertical-align: middle"><span><s:text name="common.tipofday.know" /></span>
        </div>
        <div id="tipContent"></div>
    </div>
</div>