define ['jqgrid'], ($)->
#  console?.log "module appmng/langsetting_translation_grid loading."
  lastEditedCell = null

  langSettingGrid = $('#stringSettingsTranslationGrid').jqGrid(
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
    colNames: [ 'Code', 'Language', 'Translation', 'History']
    colModel: [
      {name: 'code', index: 'languageCode', width: 20, editable: false, align: 'left'}
      {name: 'language', index: 'language', width: 40, align: 'left'}
      {name: 'translation', index: 'translation', width: 100, align: 'left'}
      {name: 'history', index: 'history', width: 10, editable: false, align: 'center', sortable: false, search: false, formatter: (cellvalue, options)->
        "<img class='historyAct' id='hisact_#{options.rowId}'  src='images/history.png'>"
      }
    ]

    gridComplete:->
      grid = $(@)

      $('img.historyAct', @).click(()->
        grid.saveCell(lastEditedCell.iRow, lastEditedCell.iCol) if lastEditedCell
        [_, rowid]=@id.split('_')
        rowData = grid.getRowData(rowid)
        rowData.id = rowid
        $('#stringSettingsTranslationHistoryDialog').data('param', rowData).dialog 'open'

      ).on('mouseover',()->
        $(@).addClass('ui-state-hover')
      ).on('mouseout', ()->
        $(@).removeClass('ui-state-hover')
      )

  ).jqGrid('navGrid', '#stringSettingsTranslationPager',
    {edit: false, add: false, del: false, search: false}).setGridParam(datatype: 'json')
  saveLastEditedCell: ()->
    langSettingGrid.saveCell(lastEditedCell.iRow, lastEditedCell.iCol) if lastEditedCell







