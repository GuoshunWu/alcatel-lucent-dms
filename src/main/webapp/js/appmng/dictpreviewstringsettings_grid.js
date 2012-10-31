// Generated by CoffeeScript 1.3.3
(function() {

  define(['jqgrid', 'require'], function($, require) {
    var dicGrid;
    dicGrid = $('#dictPreviewStringSettingsGrid').jqGrid({
      url: '',
      mtype: 'post',
      datatype: 'json',
      width: 700,
      height: 300,
      pager: '#dictPreviewStringSettingsPager',
      editurl: "",
      cellurl: '/app/deliver-update-label',
      cellEdit: true,
      rowNum: 10,
      rowList: [10, 20, 30],
      sortname: 'name',
      sortorder: 'asc',
      viewrecords: true,
      gridview: true,
      colNames: ['Label', 'Reference Language', 'Max Length', 'Context', 'Description'],
      colModel: [
        {
          name: 'key',
          index: 'key',
          width: 50,
          editable: false,
          align: 'left'
        }, {
          name: 'reference',
          index: 'reference',
          width: 40,
          editable: false,
          align: 'center'
        }, {
          name: 'maxLength',
          index: 'maxLength',
          width: 40,
          editable: true,
          align: 'center',
          editrules: {
            custom: true,
            custom_func: function(value, colname) {
              if (!/^\d+(\s*,?\s*\d+\s*)*$/.test(value)) {
                return [false, 'Invalid max length format.'];
              }
              return [true, ''];
            }
          }
        }, {
          name: 'context',
          index: 'context.name',
          width: 50,
          editable: true,
          align: 'left'
        }, {
          name: 'description',
          index: 'description',
          width: 40,
          editable: true,
          align: 'center'
        }
      ],
      beforeSubmitCell: function(rowid, cellname, value, iRow, iCol) {
        var postData;
        postData = $(this).getGridParam('postData');
        return {
          handler: postData.handler,
          dict: postData.dict
        };
      },
      afterSubmitCell: function(serverresponse, rowid, cellname, value, iRow, iCol) {
        var jsonFromServer, success;
        jsonFromServer = eval("(" + serverresponse.responseText + ")");
        success = 0 === jsonFromServer.status;
        if (success) {
          $('#dictListPreviewGrid').trigger('reloadGrid');
        }
        return [success, jsonFromServer.message];
      }
    });
    return dicGrid.jqGrid('navGrid', '#dictPreviewStringSettingsPager', {
      edit: false,
      add: false,
      del: false,
      search: false,
      view: false
    });
  });

}).call(this);
