define ['jqgrid'], ($)->
#  console?.log "module appmng/langsetting_translation_grid loading."

  lastEditedCell = null

  langSettingGrid = $('#stringSettingsTranslationGrid').jqGrid({
  mtype: 'post', datatype: 'local'
  width: 800, height: 270
#  height: $(window).innerHeight() - 200
  pager: '#stringSettingsTranslationPager'
  rowNum: 100
  sortname: 'language.name'
  caption: 'Label Translation'
  sortorder: 'asc'
  viewrecords: true
  #  ajaxGridOptions:{async:false}
  gridview: true
  colNames: [ 'Code', 'Language', 'Translation']
  colModel: [
    {name: 'code', index: 'languageCode', width: 20, editable: false, align: 'left'}
    {name: 'language', index: 'language', width: 40, align: 'left'}
    {name: 'translation', index: 'translation', width: 100, align: 'left'}
  ]
  }).jqGrid('navGrid', '#stringSettingsTranslationPager', {edit: false, add: false, del: false, search: false}).setGridParam(datatype: 'json')
  saveLastEditedCell: ()->langSettingGrid.saveCell(lastEditedCell.iRow, lastEditedCell.iCol) if lastEditedCell







