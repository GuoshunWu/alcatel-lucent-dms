define [
  'jqgrid'
  'blockui'
  'jqmsgbox'
  'jqueryui'

  'i18n!nls/common'
  'dms-util'
  'dms-urls'

], ($, blockui, msgbox, ui, c18n, util, urls)->
  gridId = 'preferredTranslationGrid'
  hGridId = "##{gridId}"

  deleteOptions = {
  msg: 'Delete selected text?'
  afterShowForm: (formid)->
    $(formid).parent().parent().position(my:'center', at: 'center', of: window)
  }

  grid = $(hGridId).jqGrid(
    url: urls.glossaries

#    datatype: 'json'
    datatype: 'local'
    mtype: 'post'
    postData: {format:'grid', prop: 'text, createTime,creator.name', 'idprop': 'text'}
    prmNames: id: 'text'
    cellurl: urls.glossary.update, cellEdit: true

    afterSubmitCell: (serverresponse, rowid, cellname, value, iRow, iCol)->
      jsonFromServer = $.parseJSON serverresponse.responseText
      setTimeout (->grid.trigger 'reloadGrid'), 10
      [jsonFromServer.status == 0, jsonFromServer.message]
    beforeSubmitCell: (rowid, cellname, value, iRow, iCol)->
      newText:value

    editurl: urls.glossary.update

    pager: "#{hGridId}Pager"
    rowNum: 30, rowList: [15, 30, 60]
    sortname: 'text',  sortorder: 'asc'
    viewrecords: true, gridview: true, multiselect: true

    caption: c18n.preferredTrans.caption
    autowidth: true
    height: '100%'
    colNames: ['', 'Create Time', 'Creator']
    colModel: [
      {name: 'text', index: 'text', editable: true, classes: 'editable-column', align: 'left',editrules: {required: true}}
      {name: 'createTime', index: 'createTime', align: 'left', formatter: 'date', formatoptions: {srcformat: 'ISO8601Long', newformat: 'ISO8601Long'}}
      {name: 'creator.name', index: 'creator.name', align: 'left'}
    ]

    gridComplete: ->
      grid = $(@)
  )
  .navGrid "#{hGridId}Pager", {search: false, edit: false}, {},{},deleteOptions

  grid: grid



