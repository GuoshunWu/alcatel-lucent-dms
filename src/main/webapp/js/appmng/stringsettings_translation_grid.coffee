define ['jqgrid', 'i18n!nls/common'], ($, c18n)->
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
    colNames: [ 'Code', 'Language', 'Translation','CtId', 'History']
    colModel: [
      {name: 'code', index: 'languageCode', width: 20, editable: false, align: 'left'}
      {name: 'language', index: 'language', width: 40, align: 'left'}
      {name: 'ct.translation', index: 'ct.translation', width: 100, align: 'left', }
      {name: 'ct.id', index: 'ct.id', width: 100, align: 'left', hidden: true}
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

        ($.msgBox c18n.history.nohistory, null, {title: c18n.error} ;return) unless rowData['ct.id']? and parseInt(rowData['ct.id']) > 0

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







