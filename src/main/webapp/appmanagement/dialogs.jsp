<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<div id="newProductDialog" title="New product">
    <span> Product name<input id="productName" value="" type="text"></span>
</div>

<div id="newProductReleaseDialog" title="New product release">
    <table>
        <tr>
            <td> Version name</td>
            <td><input id="versionName" value="" type="text"></td>
        </tr>
        <tr>
            <td>Duplicate all applications from a previous version</td>
            <td><select id="dupVersion"></select></td>
        </tr>
    </table>
</div>

<div id="newOrAddApplicationDialog" title="New or add application">
    <table>
        <tr>
            <td><label>Application name</label></td>
            <td>
                <div class="ui-widget">
                    <select id="applicationName" class="ui-widget"></select>
                </div>
        </tr>
        <tr>
            <td><label>Version</label></td>
            <td>
                <select id="version" class="ui-widget"></select>
            </td>
            </td>
        </tr>
    </table>
</div>