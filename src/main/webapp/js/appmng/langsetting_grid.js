// Generated by CoffeeScript 1.3.3
(function() {

  define(function(require) {
    var $, langSettingGrid;
    $ = require('jqgrid');
    langSettingGrid = $('#languageSettingGrid').jqGrid({
      url: '',
      mtype: 'post',
      datatype: 'json',
      width: 500,
      height: 230,
      pager: '#langSettingPager',
      editurl: "",
      rowNum: 10,
      rowList: [10, 20, 30],
      sortname: 'language.name',
      sortorder: 'asc',
      viewrecords: true,
      gridview: true,
      multiselect: true,
      cellEdit: true,
      cellurl: 'app/update-dict-language',
      colNames: ['Code', 'Language', 'Charset'],
      colModel: [
        {
          name: 'code',
          index: 'languageCode',
          width: 40,
          editable: false,
          align: 'left'
        }, {
          name: 'languageId',
          index: 'language.name',
          width: 50,
          editable: true,
          edittype: 'select',
          align: 'left'
        }, {
          name: 'charsetId',
          index: 'charset.name',
          width: 40,
          editable: true,
          edittype: 'select',
          align: 'left'
        }
      ],
      gridComplete: function() {}
    });
    langSettingGrid.jqGrid('navGrid', '#langSettingPager', {
      edit: false,
      add: true,
      del: true,
      search: false
    }, {}, {
      url: 'app/add-dict-language',
      onclickSubmit: function(params, posdata) {
        return {
          dicts: $('#languageSettingGrid').getGridParam('postData').dict
        };
      },
      onClose: function() {
        return $('#languageSettingGrid').setColProp('code', {
          editable: false
        });
      },
      beforeInitData: function() {
        return $('#languageSettingGrid').setColProp('code', {
          editable: true
        });
      },
      afterSubmit: function(response, postdata) {
        var jsonfromServer;
        jsonfromServer = eval("(" + response.responseText + ")");
        return [jsonfromServer.status === 0, jsonfromServer.message, -1];
      }
    }, {
      url: 'app/remove-dict-language'
    });
    $.getJSON('rest/languages', {
      prop: 'id,name'
    }, function(languages) {
      return langSettingGrid.setColProp('languageId', {
        editoptions: {
          value: ($(languages).map(function() {
            return "" + this.id + ":" + this.name;
          })).get().join(';')
        }
      });
    });
    return $.getJSON('rest/charsets', {
      prop: 'id,name'
    }, function(charsets) {
      return langSettingGrid.setColProp('charsetId', {
        editoptions: {
          value: ($(charsets).map(function() {
            return "" + this.id + ":" + this.name;
          })).get().join(';')
        }
      });
    });
  });

}).call(this);
