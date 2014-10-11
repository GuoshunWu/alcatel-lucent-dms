define [
  'jqgrid'
  'jqmsgbox'
  'jqueryui'
  'i18n!nls/appmng'
  'i18n!nls/common'
  'dms-urls'
], (
  $
  msgbox
  ui
  i18n
  c18n
  urls
)->

#  console?.log "module appmng/dictlistpreview_grid loading."

  infoDialog = $('<div>').dialog {
  autoOpen: false, height: 400, width: 800
  buttons: OK: ->$(@).dialog 'close'
  }
  handlers =
    'String':
      title: i18n.dialog.stringsettings.title, handler: (rowData)->
        $('#dictPreviewStringSettingsDialog').data("param", rowData).dialog 'open'
    'Language':
      title: i18n.dialog.languagesettings.title, handler: (rowData)->
        $('#dictPreviewLanguageSettingsDialog').data("param", rowData).dialog 'open'

  lastEditedCell = null

  colModel = [
    {name: 'langrefcode', index: 'langrefcode', width: 55, align: 'center', hidden: true}
    {name: 'name', index: 'base.name', width: 200, editable: true, align: 'left'}
    {name: 'version', index: 'version', width: 25, editable: true, align: 'left',
    editrules: {required: true}
    }
    {name: 'format', index: 'base.format', width: 60, editable: true, edittype: 'select',
    editoptions: {value: c18n.dictformats},
    align: 'left'}
    {name: 'encoding', index: 'base.encoding', width: 40, editable: true, edittype: 'select',
    editoptions: {value: c18n.dictencodings}, align: 'left'}
    {name: 'labelNum', index: 'labelNum', width: 20, align: 'right'}
    {name: 'errors', index: 'errorCount', width: 20, align: 'right'
#      unformat:(cellvalue, options, cell)->$('a', cell).text()
    }
    {name: 'warnings', index: 'warningCount', width: 20, align: 'right'}
    {name: 'actions', index: 'action', width: 70, editable: false, align: 'center'}
  ]
  $(colModel).each (index, colModel)->colModel.classes = 'editable-column' if colModel.editable

  prop = 'languageReferenceCode,base.name,version,base.format,base.encoding,labelNum,errorCount,warningCount'
  dicGrid = $('#dictListPreviewGrid').jqGrid({
  url: urls.deliver_dict, datatype: 'local', editurl: ""
  width: 1000, minHeight: 200, height: 240
  postData: { format: 'grid', prop: prop}
  pager: '#dictListPreviewPager', rowNum: 100
  sortname: 'base.name', sortorder: 'asc'
  viewrecords: true, cellEdit: true, cellurl: urls.app.deliver_update_dict, ajaxCellOptions: {async: false}
  gridview: true, multiselect: false
  caption: i18n.grid.dictlistpreview.caption
  colNames: ['LangRefCode', 'Dictionary', 'Version', 'Format', 'Encoding', 'Labels', 'Error', 'Warning', 'Action']
  colModel: colModel
  afterEditCell: (rowid, name, val, iRow, iCol)->lastEditedCell = {iRow: iRow, iCol: iCol, name: name, val: val}
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
      @cell[warningIdx] = "<a id='warnAndErr_warnings_#{rowData.id}' title='details' href='javascript:void(0);'>#{@cell[warningIdx]}</a>"
      @cell[errorIdx] = "<a id='warnAndErr_errors_#{rowData.id}' title='details' href='javascript:void(0);'>#{@cell[errorIdx]}</a>"
      @cell[actIdx] = $(actions).map(
        ()->
          "<a id='action_#{@}_#{rowData.id}_#{actIdx}' title='#{handlers[@].title}' href='javascript:void(0);' >#{@}</A>"
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
      handlers[action].handler rowData

    $('a', @).css 'color', 'blue'

    #      high light error rows
    $("tr[class!='jqgfirstrow']", @).each (index, row)->
      rowData = grid.getRowData(row.id)

      $(row).css 'background', '#FFFFAA' if parseInt($(rowData.warnings).text()) > 0
      $(row).css 'background', '#FFD2D2' if parseInt($(rowData.errors).text()) > 0

    errors = $('#dictListPreviewGrid').getCol('errors')
    hasError = ($(error).text() for error in errors when $(error).text() > 0).length

    btnText = i18n.dialog.dictlistpreview['import']
    importBtn = $('#dictListPreviewDialog').next("div.ui-dialog-buttonpane").find("button:contains('#{btnText}')")
    importBtn.button('option','disabled', hasError)

  }).setGridParam(datatype: 'json')
  .jqGrid('navGrid', '#dictListPreviewPager', {add: false, edit: false, search: false, del: false}, {}, {}, {})
  gridHasErrors: (grid = $('#dictListPreviewGrid'))->
    return true for row in grid.getRowData() when parseInt(row.errors) > 0
    false
