// Generated by CoffeeScript 1.5.0
(function() {

  define(['jqgrid'], function($) {
    var langSettingGrid, lastEditedCell;
    lastEditedCell = null;
    langSettingGrid = $('#stringSettingsTranslationGrid').jqGrid({
      mtype: 'post',
      datatype: 'local',
      width: 800,
      height: 270,
      pager: '#stringSettingsTranslationPager',
      rowNum: 10,
      rowList: [10, 20, 30],
      sortname: 'language.name',
      caption: 'Label Translation',
      sortorder: 'asc',
      viewrecords: true,
      gridview: true,
      colNames: ['Code', 'Language', 'Translation'],
      colModel: [
        {
          name: 'code',
          index: 'languageCode',
          width: 20,
          editable: false,
          align: 'left'
        }, {
          name: 'language',
          index: 'language',
          width: 40,
          align: 'left'
        }, {
          name: 'translation',
          index: 'translation',
          width: 100,
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
