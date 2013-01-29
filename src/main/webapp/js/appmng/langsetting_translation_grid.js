// Generated by CoffeeScript 1.4.0
(function() {

  define(function(require) {
    var $, c18n, i18n, langSettingGrid, lastEditedCell;
    $ = require('jqgrid');
    i18n = require('i18n!nls/appmng');
    c18n = require('i18n!nls/common');
    lastEditedCell = null;
    langSettingGrid = $('#stringSettingsTranslationGrid').jqGrid({
      mtype: 'post',
      datatype: 'local',
      width: 800,
      height: $(window).innerHeight() - 200,
      pager: '#stringSettingsTranslationPager',
      rowNum: 10,
      rowList: [10, 20, 30],
      sortname: 'language.name',
      sortorder: 'asc',
      viewrecords: true,
      gridview: true,
      colNames: ['Code', 'Language', 'Translation'],
      colModel: [
        {
          name: 'code',
          index: 'languageCode',
          width: 40,
          editable: false,
          align: 'left'
        }, {
          name: 'language',
          index: 'language',
          width: 50,
          align: 'left'
        }, {
          name: 'translation',
          index: 'translation',
          width: 40,
          align: 'left'
        }
      ]
    }).jqGrid('navGrid', '#stringSettingsTranslationPager', {
      edit: false,
      add: false,
      del: false,
      search: false
    }).setGridParam({
      datatype: 'json'
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