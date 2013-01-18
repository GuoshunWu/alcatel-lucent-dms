// Generated by CoffeeScript 1.4.0
(function() {

  define(function(require) {
    var $, c18n, i18n, langSettingGrid, lastEditedCell;
    $ = require('jqgrid');
    i18n = require('i18n!nls/appmng');
    c18n = require('i18n!nls/common');
    lastEditedCell = null;
    langSettingGrid = $('#languageSettingGrid').jqGrid({
      url: 'json/dummy.json',
      mtype: 'post',
      datatype: 'local',
      width: 500,
      height: 230,
      pager: '#langSettingPager',
      editurl: "app/add-dict-language",
      cellactionurl: "app/remove-dict-language",
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
          classes: 'editable-column',
          edittype: 'select',
          align: 'left'
        }, {
          name: 'charsetId',
          index: 'charset.name',
          width: 40,
          editable: true,
          classes: 'editable-column',
          edittype: 'select',
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
      gridComplete: function() {}
    }).jqGrid('navGrid', '#langSettingPager', {
      edit: false,
      add: false,
      del: false,
      search: false
    }, {}, {
      zIndex: 2000,
      modal: true,
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
    }).setGridParam({
      datatype: 'json'
    });
    langSettingGrid.navButtonAdd('#langSettingPager', {
      id: "custom_del_" + (langSettingGrid.attr('id')),
      caption: "",
      buttonicon: "ui-icon-trash",
      position: "first",
      onClickButton: function() {
        var rowIds;
        if ((rowIds = $(this).getGridParam('selarrrow')).length === 0) {
          $.msgBox(c18n.selrow.format(c18n.language), null, {
            title: c18n.warning
          });
          return;
        }
        return $(this).jqGrid('delGridRow', rowIds, {
          zIndex: 2000,
          top: 250,
          left: 550,
          msg: i18n.dialog["delete"].delmsg.format(c18n.language),
          url: 'app/remove-dict-language'
        });
      }
    });
    langSettingGrid.navButtonAdd('#langSettingPager', {
      id: "custom_add_" + (langSettingGrid.attr('id')),
      caption: "",
      buttonicon: "ui-icon-plus",
      position: "first",
      onClickButton: function() {
        $('#addLanguageDialog').data('param', {
          dicts: [$('#languageSettingGrid').getGridParam('postData').dict]
        });
        return $('#addLanguageDialog').dialog("open");
      }
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
    $.getJSON('rest/charsets', {
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
    return {
      saveLastEditedCell: function() {
        if (lastEditedCell) {
          return langSettingGrid.saveCell(lastEditedCell.iRow, lastEditedCell.iCol);
        }
      }
    };
  });

}).call(this);
