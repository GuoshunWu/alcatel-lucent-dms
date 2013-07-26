define [
  'jqgrid'
  'blockui'
  'jqmsgbox'
  'jqueryui'

  'i18n!nls/common'
  'dms-util'
  'dms-urls'

], ($, blockui, msgbox, ui, c18n, util, urls)->
  gridId = 'glossaryGrid'
  hGridId = "##{gridId}"

  deleteOptions = {
  msg: 'Delete selected text?'
  afterShowForm: (formid)->
    $(formid).parent().parent().position(my:'center', at: 'center', of: window)
  }

  addOptions = {}

  grid = $(hGridId).jqGrid(
#    url: 'json/glossaries.json'
    url: urls.glossaries

    datatype: 'json'
    mtype: 'post'
    postData: {format:'grid', prop: 'text, createTime', 'idprop': 'text'}
    prmNames: id: 'text'
    cellurl: urls.glossary.update, cellEdit: true

    afterSubmitCell: (serverresponse, rowid, cellname, value, iRow, iCol)->
      jsonFromServer = $.parseJSON serverresponse.responseText
      [jsonFromServer.status == 0, jsonFromServer.message]
    beforeSubmitCell: (rowid, cellname, value, iRow, iCol)->
      console?.log "rowid: #{rowid}, value:#{value}"
      newText:value

    editurl: urls.glossary.update

    pager: "#{hGridId}Pager"
    rowNum: 30, rowList: [15, 30, 60]
    sortname: 'text',  sortorder: 'asc'
    viewrecords: true, gridview: true, multiselect: true

    caption: c18n.glossary.caption
    autowidth: true
    height: '100%'
    colNames: ['Glossary text', 'Create Time']
    colModel: [
      {name: 'text', index: 'text', editable: true, classes: 'editable-column', align: 'left',editrules: {required: true}}
      {name: 'createTime', index: 'createTime', align: 'left'}
    ]

    gridComplete: ->
      grid = $(@)
      #handlers = grid.getGridParam 'cellactionhandlers'
  )
#  .setGridParam(datatype: 'json')
  .navGrid "#{hGridId}Pager", {search: false, edit: false}, addOptions,{},deleteOptions

  grid: grid



