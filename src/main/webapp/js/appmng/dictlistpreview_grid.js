// Generated by CoffeeScript 1.3.3
(function() {

  define(function(require, util, i18n) {
    var $, c18n, colModel, dicGrid, handlers, infoDialog, lastEditedCell;
    $ = require('jqgrid');
    util = require('util');
    i18n = require('i18n!nls/appmng');
    require('jqmsgbox');
    c18n = require('i18n!nls/common');
    infoDialog = $('<div>').dialog({
      autoOpen: false,
      height: 400,
      width: 800,
      buttons: {
        OK: function() {
          return $(this).dialog('close');
        }
      }
    });
    handlers = {
      'String': {
        title: i18n.dialog.stringsettings.title,
        handler: function(rowData, dialogs) {
          dialogs.dictPreviewStringSettings.data("param", rowData);
          return dialogs.dictPreviewStringSettings.dialog('open');
        }
      },
      'Language': {
        title: i18n.dialog.languagesettings.title,
        handler: function(rowData, dialogs) {
          dialogs.dictPreviewLangSettings.data("param", rowData);
          return dialogs.dictPreviewLangSettings.dialog('open');
        }
      }
    };
    lastEditedCell = null;
    colModel = [
      {
        name: 'langrefcode',
        index: 'langrefcode',
        width: 55,
        align: 'center',
        hidden: true
      }, {
        name: 'name',
        index: 'base.name',
        width: 200,
        editable: true,
        align: 'left'
      }, {
        name: 'version',
        index: 'version',
        width: 25,
        editable: true,
        align: 'left',
        editrules: {
          required: true
        }
      }, {
        name: 'format',
        index: 'base.format',
        width: 60,
        editable: true,
        edittype: 'select',
        editoptions: {
          value: "DCT:DCT;Dictionary conf:Dictionary conf;Text properties:Text properties;XML labels:XML labels"
        },
        align: 'left'
      }, {
        name: 'encoding',
        index: 'base.encoding',
        width: 40,
        editable: true,
        edittype: 'select',
        editoptions: {
          value: 'ISO-8859-1:ISO-8859-1;UTF-8:UTF-8;UTF-16LE:UTF-16LE;UTF-16BE:UTF-16BE'
        },
        align: 'left'
      }, {
        name: 'labelNum',
        index: 'labelNum',
        width: 20,
        align: 'right'
      }, {
        name: 'errors',
        index: 'errorCount',
        width: 20,
        align: 'right'
      }, {
        name: 'warnings',
        index: 'warningCount',
        width: 20,
        align: 'right'
      }, {
        name: 'actions',
        index: 'action',
        width: 70,
        editable: false,
        align: 'center'
      }
    ];
    $(colModel).each(function(index, colModel) {
      if (colModel.editable) {
        return colModel.classes = 'editable-column';
      }
    });
    dicGrid = $('#dictListPreviewGrid').jqGrid({
      url: 'json/dummy.json',
      datatype: 'json',
      editurl: "",
      mtype: 'POST',
      width: 1000,
      minHeight: 200,
      height: 240,
      pager: '#dictListPreviewPager',
      rowNum: 100,
      sortname: 'base.name',
      sortorder: 'asc',
      viewrecords: true,
      cellEdit: true,
      cellurl: 'app/deliver-update-dict',
      ajaxCellOptions: {
        async: false
      },
      gridview: true,
      multiselect: false,
      caption: i18n.grid.dictlistpreview.caption,
      colNames: ['LangRefCode', 'Dictionary', 'Version', 'Format', 'Encoding', 'Labels', 'Error', 'Warning', 'Action'],
      colModel: colModel,
      afterEditCell: function(rowid, cellname, val, iRow, iCol) {
        return lastEditedCell = {
          iRow: iRow,
          iCol: iCol,
          name: name,
          val: val
        };
      },
      ondblClickRow: function(rowid, iRow, iCol, e) {},
      beforeProcessing: function(data, status, xhr) {
        var actIdx, actions, errorIdx, grid, k, v, warningIdx, _ref;
        grid = $(this);
        _ref = [$.inArray('Action', grid.getGridParam('colNames')), $.inArray('Warning', grid.getGridParam('colNames')), $.inArray('Error', grid.getGridParam('colNames'))], actIdx = _ref[0], warningIdx = _ref[1], errorIdx = _ref[2];
        if (grid.getGridParam('multiselect')) {
          --actIdx;
          --warningIdx;
          --errorIdx;
        }
        actions = [];
        for (k in handlers) {
          v = handlers[k];
          actions.push(k);
        }
        return $(data.rows).each(function(index, rowData) {
          this.cell[warningIdx] = "<a id='warnAndErr_warnings_" + rowData.id + "' title='details' href=#>" + this.cell[warningIdx] + "</a>";
          this.cell[errorIdx] = "<a id='warnAndErr_errors_" + rowData.id + "' title='details' href=#>" + this.cell[errorIdx] + "</a>";
          return this.cell[actIdx] = $(actions).map(function() {
            return "<a id='action_" + this + "_" + rowData.id + "_" + actIdx + "' title='" + handlers[this].title + "' href=# >" + this + "</A>";
          }).get().join('&nbsp;&nbsp;&nbsp;&nbsp;');
        });
      },
      beforeSubmitCell: function(rowid, cellname, value, iRow, iCol) {
        return {
          handler: ($(this).getGridParam('postData')).handler
        };
      },
      afterSubmitCell: function(serverresponse, rowid, cellname, value, iRow, iCol) {
        var jsonFromServer, success;
        jsonFromServer = eval("(" + serverresponse.responseText + ")");
        success = 0 === jsonFromServer.status;
        if (success) {
          $(this).trigger('reloadGrid');
        }
        return [success, jsonFromServer.message];
      },
      gridComplete: function() {
        var grid;
        grid = $(this);
        $('a[id^=warnAndErr_]', this).click(function() {
          var handler, name, rowid, value, _, _ref;
          _ref = this.id.split('_'), _ = _ref[0], name = _ref[1], rowid = _ref[2];
          value = $(this).text();
          if (parseInt(value) === 0) {
            return;
          }
          handler = grid.getGridParam('postData').handler;
          return $.getJSON("rest/delivery/dict/" + rowid, {
            handler: handler,
            prop: name
          }, function(json) {
            infoDialog.dialog('option', {
              title: name
            });
            infoDialog.html($('<table border=1>').append('<tr><td>' + json[name].join('<tr><td>')));
            return infoDialog.dialog('open');
          });
        });
        $('a[id^=action_]', this).click(function() {
          var a, action, col, rowData, rowid, _ref;
          _ref = this.id.split('_'), a = _ref[0], action = _ref[1], rowid = _ref[2], col = _ref[3];
          if (lastEditedCell) {
            grid.saveCell(lastEditedCell.iRow, lastEditedCell.iCol);
          }
          rowData = grid.getRowData(rowid);
          delete rowData.action;
          rowData.id = rowid;
          rowData.handler = grid.getGridParam('postData').handler;
          return handlers[action].handler(rowData, require('appmng/dialogs'));
        });
        $('a', this).css('color', 'blue');
        return $("tr[class!='jqgfirstrow']", this).each(function(index, row) {
          var rowData;
          rowData = grid.getRowData(row.id);
          if (parseInt($(rowData.warnings).text()) > 0) {
            $(row).css('background', '#FFFFAA');
          }
          if (parseInt($(rowData.errors).text()) > 0) {
            return $(row).css('background', '#FFD2D2');
          }
        });
      }
    });
    dicGrid.jqGrid('navGrid', '#dictListPreviewPager', {
      add: false,
      edit: false,
      search: false,
      del: false
    }, {}, {}, {});
    return {
      gridHasErrors: function() {
        var hasError;
        hasError = false;
        $($('#dictListPreviewGrid').getRowData()).each(function(index, row) {
          hasError = parseInt(row.errors) > 0;
          if (hasError) {
            return false;
          }
        });
        return hasError;
      }
    };
  });

}).call(this);
