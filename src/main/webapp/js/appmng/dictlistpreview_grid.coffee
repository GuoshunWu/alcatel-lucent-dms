define (require, util, i18n)->
  $ = require 'jqgrid'
  util = require 'util'
  i18n = require 'i18n!nls/appmng'
  require('jqmsgbox')
  c18n = require 'i18n!nls/common'

  infoDialog = $('<div>').dialog {
  autoOpen: false, height: 400, width: 800
  buttons: OK: ->$(@).dialog 'close'
  }
  handlers =
    'String':
      title: i18n.dialog.stringsettings.title, handler: (rowData, dialogs)->
        dialogs.dictPreviewStringSettings.data "param", rowData
        dialogs.dictPreviewStringSettings.dialog 'open'
    'Language':
      title: i18n.dialog.languagesettings.title, handler: (rowData, dialogs)->
        dialogs.dictPreviewLangSettings.data "param", rowData
        dialogs.dictPreviewLangSettings.dialog 'open'

  lastEditedCell = null

  colModel = [
    {name: 'langrefcode', index: 'langrefcode', width: 55, align: 'center', hidden: true}
    {name: 'name', index: 'base.name', width: 200, editable: true, align: 'left'}
    {name: 'version', index: 'version', width: 25, editable: true, align: 'left',
    editrules: {required: true}
    }
    {name: 'format', index: 'base.format', width: 60, editable: true, edittype: 'select',
    editoptions: {value: "DCT:DCT;Dictionary conf:Dictionary conf;Text properties:Text properties;XML labels:XML labels"},
    align: 'left'}
    {name: 'encoding', index: 'base.encoding', width: 40, editable: true, edittype: 'select',
    editoptions: {value: 'ISO-8859-1:ISO-8859-1;UTF-8:UTF-8;UTF-16LE:UTF-16LE;UTF-16BE:UTF-16BE'}, align: 'left'}
    {name: 'labelNum', index: 'labelNum', width: 20, align: 'right'}
    {name: 'errors', index: 'errorCount', width: 20, align: 'right'}
    {name: 'warnings', index: 'warningCount', width: 20, align: 'right'}
    {name: 'actions', index: 'action', width: 70, editable: false, align: 'center'}
  ]
  $(colModel).each (index, colModel)->colModel.classes = 'editable-column' if colModel.editable

  dicGrid = $('#dictListPreviewGrid').jqGrid {
  url: 'json/dummy.json', datatype: 'json', editurl: "", mtype: 'POST'
  width: 1000, minHeight: 200, height: 240
  pager: '#dictListPreviewPager', rowNum: 100
  sortname: 'base.name', sortorder: 'asc'
  viewrecords: true, cellEdit: true, cellurl: 'app/deliver-update-dict', ajaxCellOptions: {async: false}
  gridview: true, multiselect: false
  caption: i18n.grid.dictlistpreview.caption
  colNames: ['LangRefCode', 'Dictionary', 'Version', 'Format', 'Encoding', 'Labels', 'Error', 'Warning', 'Action']
  colModel: colModel
  afterEditCell: (rowid, cellname, val, iRow, iCol)->lastEditedCell = {iRow: iRow, iCol: iCol, name: name, val: val}
  ondblClickRow: (rowid, iRow, iCol, e)->

  beforeProcessing: (data, status, xhr)->
    grid = $(@)

    [actIdx, warningIdx, errorIdx]=[
      $.inArray 'Action', grid.getGridParam('colNames')
      $.inArray 'Warning', grid.getGridParam('colNames')
      $.inArray 'Error', grid.getGridParam('colNames')
    ]

    (--actIdx; --warningIdx; --errorIdx) if grid.getGridParam('multiselect')

    actions = []
    actions.push k for k,v of handlers


    $(data.rows).each (index, rowData)->
      @cell[warningIdx] = "<a id='warnAndErr_warnings_#{rowData.id}' title='details' href=#>#{@cell[warningIdx]}</a>"
      @cell[errorIdx] = "<a id='warnAndErr_errors_#{rowData.id}' title='details' href=#>#{@cell[errorIdx]}</a>"

      @cell[actIdx] = $(actions).map(
        ()->
          "<a id='action_#{@}_#{rowData.id}_#{actIdx}' title='#{handlers[@].title}' href=# >#{@}</A>"
      ).get().join('&nbsp;&nbsp;&nbsp;&nbsp;')



  beforeSubmitCell: (rowid, cellname, value, iRow, iCol)->handler: ($(@).getGridParam 'postData').handler
  afterSubmitCell: (serverresponse, rowid, cellname, value, iRow, iCol)->
    jsonFromServer = eval "(#{serverresponse.responseText})"

    success = 0 == jsonFromServer.status
    $(@).trigger 'reloadGrid' if success
    [success, jsonFromServer.message]

  gridComplete: ->
    grid = $(@)

    #   error and warning actions
    $('a[id^=warnAndErr_]', @).click ()->
      [_, name, rowid]=@id.split '_'
      value = $(@).text()
      return if parseInt(value) == 0
      handler = grid.getGridParam('postData').handler

      $.getJSON "rest/delivery/dict/#{rowid}", {handler: handler, prop: name}, (json)->
        infoDialog.dialog 'option', title: name
        infoDialog.html $('<table border=0>').append '<tr><td>' + json[name].join('<tr><td>')
        infoDialog.dialog 'open'


    $('a[id^=action_]', @).click ()->
      [a, action, rowid, col]=@id.split('_')
      #      save grid edit before get data
      grid.saveCell(lastEditedCell.iRow, lastEditedCell.iCol) if lastEditedCell

      rowData = grid.getRowData(rowid)
      delete rowData.action
      rowData.id = rowid
      rowData.handler = grid.getGridParam('postData').handler
      handlers[action].handler rowData, require 'appmng/dialogs'

    $('a', @).css 'color', 'blue'

    #      high light error rows
    $("tr[class!='jqgfirstrow']", @).each (index, row)->
      rowData = grid.getRowData(row.id)

      $(row).css 'background', '#FFFFAA' if parseInt($(rowData.warnings).text()) > 0
      $(row).css 'background', '#FFD2D2' if parseInt($(rowData.errors).text()) > 0

  }
  dicGrid.jqGrid 'navGrid', '#dictListPreviewPager', {add: false, edit: false, search: false, del: false}, {}, {}, {}
  gridHasErrors: ()->
    hasError = false
    $($('#dictListPreviewGrid').getRowData()).each (index, row) ->
      hasError = parseInt(row.errors) > 0
      return false if hasError
    hasError
