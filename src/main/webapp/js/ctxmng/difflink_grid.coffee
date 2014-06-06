define [
  'jqgrid'
  'blockui'
  'jqmsgbox'
  'jqueryui'

  'i18n!nls/common'
  'dms-util'
  'dms-urls'

], ($, blockui, msgbox, ui, c18n, util, urls)->
  gridId = 'diffLinkGrid'
  hGridId = "##{gridId}"

  _takeTranslationResult = {}

  deleteOptions = {
  msg: 'Delete selected text?'
  afterShowForm: (formid)->
    $(formid).parent().parent().position(my:'center', at: 'center', of: window)
  }

  grid = $(hGridId).jqGrid(
    url: urls.text.diff_text_translations
    width: 1150
    height: 320

  #          postData: {format:'grid', prop: 'a.id,b.id,a.language.name,a.translation,a.status,b.translation,b.status'}
    postData: {format:'grid', prop: 'b.id,a.language.name,a.translation,a.status,b.translation,b.status'}
    datatype: 'local', mtype: 'post'
    colNames: ['TextBId','Language', 'Translation', 'Status', 'Translation', 'Status', 'Take']
    colModel: [
      # text a id is the row id
      {name: 'textb.id', index: 'textb.id', width: 140, editable:false, align: 'left', hidden: true}
      {name: 'language', index: 'language', width: 140, editable:false, align: 'left'}
      {name: 'translationA', index: 'translationA', width: 220, editable:false, align: 'left'}
      {name: 'statusA', index: 'statusA', editable:false, width: 60, align: 'center'
      formatter: 'select', stype: 'select', searchoptions: {value: c18n.translation.values}
      edittype: 'select', editoptions: {value: c18n.translation.values}
      }
      {name: 'translationB', index: 'translationB',width: 220, editable:false, align: 'left'}
      {name: 'statusB', index: 'statusB', editable:false, width: 60, align: 'center'
      formatter: 'select', stype: 'select', searchoptions: {value: c18n.translation.values}
      edittype: 'select', editoptions: {value: c18n.translation.values}
      }
      {name: 'take', index: 'take', editable:false, width: 40, align: 'center'
      unformat: (cellvalue, options, cell) ->cellvalue
      formatter: (cellvalue, options, rowObject)->
          "<a href='javascript:void(0);' title='A' id=\"act_#{options.rowId}_#{options.colModel.name}_#{options.pos}_A\" style='color:blue'>A</a>
          &nbsp;&nbsp;
          <a href='javascript:void(0);' title='B' id=\"act_#{options.rowId}_#{options.colModel.name}_#{options.pos}_B\" style='color:blue'>B</a>
          "
      }
    ]
    gridComplete: ->
      grid = $(@)
      $("a[id^='act']", @).click(->
        [_, id, name, pos, which] = @id.split '_'
        takeTranslation id, which
      )
  ).setGridParam(datatype: 'json')
  .navGrid("#{hGridId}Pager", {edit: false, add: false, del: false, search: false, view: false},{},{},{})


  takeTranslation =(textId, selectedText)->
    rowData = grid.getRowData(textId)
#    console.log("textAId=", textId, ", textBId=", rowData['textb.id'], ", selectedText=", selectedText)
    grid.setCell(textId, "translation#{selectedText}", '', {'text-decoration':'initial'})
    grid.setCell(textId, "status#{selectedText}", '', {'text-decoration':'initial'})
    otherColumn = if 'A' == selectedText then 'B' else 'A'
    grid.setCell(textId, "translation#{otherColumn}", '', {'text-decoration':'line-through'})
    grid.setCell(textId, "status#{otherColumn}", '', {'text-decoration':'line-through'})

    _takeTranslationResult[textId] = other: rowData['textb.id'], selected: selectedText
#    console.log "takeTranslation =", _takeTranslationResult

  _takeForAll= (selectedText)->
    return unless selectedText in ['A', 'B']
    $.each grid.getDataIDs(), (rowIndex, rowId)->takeTranslation(rowId, selectedText)

  setTakeTranslationResult: (content={})-> _takeTranslationResult = content
  getTakeTranslationResult: ()-> _takeTranslationResult

  takeForAll: _takeForAll

  grid: grid




