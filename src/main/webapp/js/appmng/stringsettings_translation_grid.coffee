define ['jqgrid', 'i18n!nls/common'], ($, c18n)->
#  console?.log "module appmng/langsetting_translation_grid loading."

  gridId = 'stringSettingsTranslationGrid'
  hGridId = '#' + gridId
  pagerId = gridId + 'Pager'
  hPagerId = '#' + pagerId
  langSettingGrid = $(hGridId).after($("<div>").attr("id", pagerId)).jqGrid(
    mtype: 'post', url: 'rest/label/translation', datatype: 'local'
    width: 800, height: 270
    pager: pagerId,
    rowNum: 100
    postData: {format: 'grid',prop: 'languageCode,language.name,ct.translation,ct.id, language.id'}
    sortname: 'language.name'
    caption: 'Label Translation'
    sortorder: 'asc'
    viewrecords: true, cellEdit: true, cellsubmit: 'clientArray'
    gridview: true
    colNames: [ 'Code', 'Language', 'Translation','CtId', 'LanguageId', 'History']
    colModel: [
      {name: 'code', index: 'languageCode', width: 20, editable: false, align: 'left'}
      {name: 'language', index: 'language', width: 40, align: 'left'}
      {name: 'ct.translation', index: 'ct.translation', width: 100, align: 'left',edittype:'textarea', classes: 'editable-column', editable: true}
      {name: 'ct.id', index: 'ct.id', width: 100, align: 'left', hidden: true}
      {name: 'language.id', index: 'language.id', width: 40, align: 'left', hidden:true}
      {name: 'history', index: 'history', width: 10, editable: false, align: 'center', sortable: false, search: false, formatter: (cellvalue, options)->
        "<img class='historyAct' id='hisact_#{options.rowId}'  src='images/history.gif'>"
      }
    ]
    afterEditCell: (rowid, cellname, value, iRow, iCol)->
      $(@).data("editedCell", {iRow:iRow, iCol: iCol})
    gridComplete:->
      grid = $(@)

      $('img.historyAct', @).click(()->
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

  ).setGridParam(datatype: 'json').jqGrid(
    'navGrid', hPagerId,
    {edit: false, add: false, del: false, search: false}
  )







