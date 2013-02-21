// Generated by CoffeeScript 1.4.0
(function() {

  define(function(require) {
    var $, afterSubmit, grid, i18n, util;
    $ = require('jqgrid');
    util = require('util');
    i18n = require('i18n!nls/admin');
    afterSubmit = function(response, postdata) {
      var jsonFromServer;
      jsonFromServer = $.parseJSON(response.responseText);
      return [jsonFromServer.status === 0, jsonFromServer.message];
    };
    return grid = $('#languageGrid').jqGrid({
      url: 'rest/languages',
      postData: {
        prop: 'name,defaultCharset',
        format: 'grid'
      },
      datatype: 'json',
      mtype: 'post',
      pager: '#languagePager',
      rowNum: 15,
      rowList: [15, 30, 60],
      multiselect: true,
      cellEdit: true,
      cellurl: 'admin/language',
      afterSubmitCell: function(serverresponse, rowid, cellname, value, iRow, iCol) {
        var jsonFromServer;
        jsonFromServer = $.parseJSON(serverresponse.responseText);
        return [jsonFromServer.status === 0, jsonFromServer.message];
      },
      editurl: 'admin/language',
      loadtext: 'Loading, please wait...',
      caption: i18n.langgrid.caption,
      autowidth: true,
      height: '100%',
      colNames: ['Name', 'Default Charset'],
      colModel: [
        {
          name: 'name',
          index: 'name',
          width: 100,
          classes: 'editable-column',
          editable: true,
          align: 'left',
          editrules: {
            required: true
          }
        }, {
          name: 'defaultCharset',
          index: 'defaultCharset',
          width: 100,
          align: 'left',
          editable: true,
          classes: 'editable-column',
          edittype: 'select',
          editoptions: {
            dataUrl: 'rest/charsets?prop=id,name',
            buildSelect: function(response) {
              return "<select>" + (($($.parseJSON(response)).map(function(idx, elem) {
                return "<option value=" + this.id + ">" + this.name + "</option>";
              })).get().join('\n')) + "</select>";
            }
          },
          editrules: {
            required: true
          }
        }
      ]
    }).jqGrid('navGrid', '#languagePager', {
      search: false,
      edit: false
    }, {}, {
      mtype: 'post',
      afterSubmit: afterSubmit,
      closeAfterAdd: true,
      afterShowForm: function(form) {
        return $("#editmod" + this.id).position({
          my: 'center',
          at: 'center',
          of: window
        });
      }
    }, {
      mtype: 'post',
      afterSubmit: afterSubmit
    });
  });

}).call(this);
