define ['jqgrid','appmng/langsetting_translation_grid', 'require'], ($,ltgrid, require)->

  lastEditedCell = null
  util = require 'util'

  dicGrid = $('#stringSettingsGrid').jqGrid(
    url: 'json/dummy.json', mtype: 'post', datatype: 'local'
    width: $(window).innerWidth() * 0.8, height: 300
    pager: '#stringSettingsPager'
    editurl: ""
    rowNum: 10, rowList: [10, 20, 30]
    sortorder: 'asc'
    viewrecords: true
    gridview: true, multiselect: true, cellEdit: true, cellurl: 'app/update-label'
    colNames: ['Label', 'Reference Language', 'T', 'N', 'I', 'Max Length', 'Context', 'Description', ]
    colModel: [
      {name: 'key', index: 'key', width: 100, editable: false, align: 'left'}
      {name: 'reference', index: 'reference', width: 200, editable: false, align: 'left'}
      {name: 't', index: 't', formatter: 'showlink', formatoptions: {baseLinkUrl: '#'}, sortable: true, width: 15, align: 'right'}
      {name: 'n', index: 'n', formatter: 'showlink', formatoptions: {baseLinkUrl: '#'}, sortable: true, width: 15, align: 'right'}
      {name: 'i', index: 'i', formatter: 'showlink', formatoptions: {baseLinkUrl: '#'}, sortable: true, width: 15, align: 'right'}
      {name: 'maxLength', index: 'maxLength', width: 40, editable: true, classes: 'editable-column', align: 'right'}
      {name: 'context', index: 'context.name', editrules: {required: true}, width: 40, editable: true, classes: 'editable-column', align: 'left'}
      {name: 'description', index: 'description', width: 60, editable: true, classes: 'editable-column', align: 'left'}
    ]
    gridComplete: ->
      grid = $(@)
      $('a', @).each (index, a)->$(a).before(' ').remove() if '0' == $(a).text()
      $('a', @).css('color', 'blue').click ()->
        $('#stringSettingsTranslationDialog').data param: util.getUrlParams(@href)
        $('#stringSettingsTranslationDialog').dialog 'open'

    afterEditCell: (rowid, cellname, val, iRow, iCol)->lastEditedCell = {iRow: iRow, iCol: iCol, name: name, val: val}
  ).setGridParam(datatype: 'json')
  .jqGrid('navGrid', '#stringSettingsPager', {edit: false, add: false, del: false, search: false, view: false},
    {}, {}, {})
  .setGroupHeaders(useColSpanStyle: true, groupHeaders: [
      {startColumnName: "t", numberOfColumns: 3, titleText: 'Status'}
    ])


  saveLastEditedCell: ()->dicGrid.saveCell(lastEditedCell.iRow, lastEditedCell.iCol) if lastEditedCell


