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

  grid = $(hGridId).jqGrid(
    url: urls.text.texts
    datatype: 'local', mtype: 'post'
    postData: {format:'grid', prop: 'reference, languageNum, t, n, i, refs'}
#    pager: "#{hGridId}Pager"
#    rowNum: 999, loadonce: false
#    sortname: 'reference',  sortorder: 'asc'
#    viewrecords: true, gridview: true
#    caption: 'Text in context statistics'
    colNames: ['Name']
    colModel: [
      {name: 'name', index: 'name', editable: true, classes: 'editable-column', align: 'left',editrules: {required: true}}
    ]

    gridComplete: ->
      grid = $(@)
      #handlers = grid.getGridParam 'cellactionhandlers'
  ).setGridParam(datatype: 'json')
  .navGrid "#{hGridId}Pager", {edit: false, add: false, del: false, search: false, view: false},{},{},deleteOptions

  grid: grid



