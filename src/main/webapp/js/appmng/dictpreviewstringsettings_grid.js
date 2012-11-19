// Generated by CoffeeScript 1.3.3
(function() {

  define(['jqgrid', 'require'], function($, require) {
    var dicGrid, lastEditedCell;
    lastEditedCell = null;
    dicGrid = $('#dictPreviewStringSettingsGrid').jqGrid({
      url: '',
      mtype: 'post',
      datatype: 'json',
      width: 700,
      height: 300,
      pager: '#dictPreviewStringSettingsPager',
      editurl: "",
      cellurl: 'app/deliver-update-label',
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
          align: 'left'
        }, {
          name: 'maxLength',
          index: 'maxLength',
          width: 40,
          editable: true,
          classes: 'editable-column',
          align: 'right',
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
          classes: 'editable-column',
          editable: true,
          align: 'left'
        }, {
          name: 'description',
          index: 'description',
          width: 40,
          classes: 'editable-column',
          editable: true,
          align: 'left'
        }
      ],
      afterEditCell: function(rowid, cellname, val, iRow, iCol) {
        return lastEditedCell = {
          iRow: iRow,
          iCol: iCol,
          name: name,
          val: val
        };
      },
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
    dicGrid.jqGrid('navGrid', '#dictPreviewStringSettingsPager', {
      edit: false,
      add: false,
      del: false,
      search: false,
      view: false
    });
    return {
      saveLastEditedCell: function() {
        if (lastEditedCell) {
          return dicGrid.saveCell(lastEditedCell.iRow, lastEditedCell.iCol);
        }
      }
    };
  });

}).call(this);
