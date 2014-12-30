<!DOCTYPE html>
<html>
<head>
    <title>jQuery UI Dialog</title>

    <link rel="stylesheet" type="text/css" href="../css/main.css"/>
    <script type="text/javascript" src="../js/lib/require.js"></script>

</head>
<body class="myfont">

<table style="width: 100%;height: 100%" border="0">
    <tr>
        <td align="center" valign="top">
            <table id="charsetGrid"></table>
        </td>
    </tr>
</table>

<script type="text/javascript">

    function myCharsetGridTest(){
        require(['jqgrid'], function($) {
            var afterSubmit, grid, gridId, hGridId, hPagerId, pagerId;
            afterSubmit = function(response, postdata) {
                var jsonFromServer;
                jsonFromServer = $.parseJSON(response.responseText);
                return [jsonFromServer.status === 0, jsonFromServer.message];
            };
            gridId = 'charsetGrid';
            hGridId = '#' + gridId;
            pagerId = gridId + '_' + 'Pager';
            hPagerId = '#' + pagerId;
            return grid = $(hGridId).after("<div id='" + pagerId + "'>").jqGrid({
                url: '../rest/charsets',
                postData: {
                    prop: 'name',
                    format: 'grid'
                },
                datatype: 'json',
                pager: hPagerId,
                mtype: 'post',
                multiselect: true,
                rowNum: 15,
                rowList: [15, 30, 60],
                loadtext: 'Loading, please wait...',
                caption: 'Test charset caption',
                autowidth: true,
                height: '100%',
                cellurl: '',
                // here program on cell edit
                cellEdit: false,
                afterSubmitCell: function(serverresponse, rowid, cellname, value, iRow, iCol) {
                    var jsonFromServer;
                    jsonFromServer = $.parseJSON(serverresponse.responseText);
                    return [jsonFromServer.status === 0, jsonFromServer.message];
                },
                editurl: '',
                colNames: ['Name'],
                colModel: [
                    {
                        name: 'name',
                        index: 'name',
                        editable: true,
                        classes: 'editable-column',
                        align: 'left',
                        editrules: {
                            required: true
                        }
                    }
                ]
            }).jqGrid('navGrid', hPagerId, {
                search: false,
                edit: false
            }, {
                mtype: 'post',
                afterSubmit: afterSubmit,
                ajaxEditOptions: {
                    dataType: 'json'
                },
                closeAfterAdd: true,
                beforeShowForm: function(form) {}
            }, {
                mtype: 'post',
                afterSubmit: afterSubmit,
                ajaxDelOptions: {
                    dataType: 'json'
                },
                beforeShowForm: function(form) {}
            });
        });
    }

    require(['../js/config.js', '../js/lib/domReady.js'], function(config, domReady, r) {
        return domReady(function() {
            myCharsetGridTest();
//            require(['admin/charsetgrid'], function($){
//                console.log("load charset grid done.");
//            });
        });
    });
</script>


</body>
</html>