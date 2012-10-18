// Generated by CoffeeScript 1.3.3
(function() {

  define(function(require, util, i18n) {
    var $, c18n, dicGrid, languageSetting, stringSetting;
    $ = require('jqgrid');
    util = require('util');
    i18n = require('i18n!nls/appmng');
    require('jqmsgbox');
    c18n = require('i18n!nls/common');
    languageSetting = function(rowData) {
      var dialogs;
      dialogs = require('appmng/dialogs');
      dialogs.dictPreviewLangSettings.data("param", rowData);
      return dialogs.dictPreviewLangSettings.dialog('open');
    };
    stringSetting = function(rowData) {
      var dialogs;
      dialogs = require('appmng/dialogs');
      dialogs.dictPreviewStringSettings.data("param", rowData);
      return dialogs.dictPreviewStringSettings.dialog('open');
    };
    dicGrid = $('#dictListPreviewGrid').jqGrid({
      url: '',
      datatype: 'json',
      editurl: "",
      width: 1000,
      minHeight: 200,
      height: 240,
      pager: '#dictListPreviewPager',
      rowNum: 100,
      sortname: 'base.name',
      sortorder: 'asc',
      viewrecords: true,
      cellEdit: true,
      cellurl: '/app/deliver-update-dict',
      gridview: true,
      multiselect: true,
      caption: i18n.grid.dictlistpreview.caption,
      colNames: ['LangRefCode', 'Dictionary', 'Version', 'Format', 'Encoding', 'Labels', 'Action'],
      colModel: [
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
          align: 'center'
        }, {
          name: 'format',
          index: 'base.format',
          width: 60,
          editable: true,
          edittype: 'select',
          editoptions: {
            value: "DCT:DCT;Dictionary conf:Dictionary conf;Text properties:Text properties;XML labels:XML labels"
          },
          align: 'center'
        }, {
          name: 'encoding',
          index: 'base.encoding',
          width: 40,
          editable: true,
          edittype: 'select',
          editoptions: {
            value: 'ISO-8859-1:ISO-8859-1;UTF-8:UTF-8;UTF-16LE:UTF-16LE;UTF-16BE:UTF-16BE'
          },
          align: 'center'
        }, {
          name: 'labelNum',
          index: 'labelNum',
          width: 20,
          align: 'center'
        }, {
          name: 'action',
          index: 'action',
          width: 45,
          editable: false,
          align: 'center'
        }
      ],
      beforeProcessing: function(data, status, xhr) {
        var actIndex;
        actIndex = $(this).getGridParam('colNames').indexOf('Action');
        if ($(this).getGridParam('multiselect')) {
          --actIndex;
        }
        return $(data.rows).each(function(index) {
          var rowData;
          rowData = this;
          return this.cell[actIndex] = ($(['S', 'L']).map(function() {
            return "<A id='action_" + this + "_" + rowData.id + "_" + actIndex + "' href=# >" + this + "</A>";
          })).get().join('');
        });
      },
      beforeSubmitCell: function(rowid, cellname, value, iRow, iCol) {
        return {
          handler: ($(this).getGridParam('postData')).handler
        };
      },
      afterSubmitCell: function(serverresponse, rowid, cellname, value, iRow, iCol) {
        var jsonFromServer;
        jsonFromServer = eval("(" + serverresponse.responseText + ")");
        return [0 === jsonFromServer.status, jsonFromServer.message];
      },
      gridComplete: function() {
        var grid;
        grid = $(this);
        return $('a[id^=action_]', this).button({
          create: function(e, ui) {
            var a, action, col, rowid, titles, _ref;
            _ref = this.id.split('_'), a = _ref[0], action = _ref[1], rowid = _ref[2], col = _ref[3];
            titles = {
              S: i18n.dialog.stringsettings.title,
              L: i18n.dialog.languagesettings.title
            };
            this.title = titles[action];
            return this.onclick = function(e) {
              var rowData;
              rowData = grid.getRowData(rowid);
              delete rowData.action;
              rowData.id = rowid;
              rowData.handler = grid.getGridParam('postData').handler;
              switch (action) {
                case 'S':
                  return stringSetting(rowData);
                case 'L':
                  return languageSetting(rowData);
                default:
                  return console.log('Invalid action');
              }
            };
          }
        });
      }
    });
    return dicGrid.jqGrid('navGrid', '#dictListPreviewPager', {
      add: false,
      edit: false,
      search: false,
      del: false
    }, {}, {}, {});
  });

}).call(this);
