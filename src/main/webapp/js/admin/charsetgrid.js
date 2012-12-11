// Generated by CoffeeScript 1.3.3
(function() {

  define(function(require) {
    var $, afterSubmit, grid, util;
    $ = require('jqgrid');
    util = require('util');
    afterSubmit = function(response, postdata) {
      var jsonFromServer;
      jsonFromServer = $.parseJSON(response.responseText);
      return [jsonFromServer.status === 0, jsonFromServer.message];
    };
    return grid = $('#charsetGrid').jqGrid({
      url: 'rest/charsets',
      postData: {
        prop: 'name',
        format: 'grid'
      },
      datatype: 'json',
      pager: '#charsetPager',
      mtype: 'post',
      multiselect: true,
      rowNum: 15,
      rowList: [15, 30, 60],
      loadtext: 'Loading, please wait...',
      caption: 'Place holder',
      width: $(window).innerWidth() * 0.95,
      height: $(window).innerHeight() * 0.6,
      cellurl: 'admin/charset',
      cellEdit: true,
      afterSubmitCell: function(serverresponse, rowid, cellname, value, iRow, iCol) {
        var jsonFromServer;
        jsonFromServer = $.parseJSON(serverresponse.responseText);
        return [jsonFromServer.status === 0, jsonFromServer.message];
      },
      editurl: 'admin/charset',
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
    }).jqGrid('navGrid', '#charsetPager', {}, {
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
      beforeShowForm: function(formid) {}
    });
  });

}).call(this);
