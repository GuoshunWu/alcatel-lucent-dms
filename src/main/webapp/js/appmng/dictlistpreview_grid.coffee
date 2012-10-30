define (require, util, i18n)->
  $ = require 'jqgrid'
  util = require 'util'
  i18n = require 'i18n!nls/appmng'
  require('jqmsgbox')
  c18n = require 'i18n!nls/common'

  handlers =
    'String':
      title: i18n.dialog.stringsettings.title, handler: (rowData, dialogs)->
        dialogs.dictPreviewStringSettings.data "param", rowData
        dialogs.dictPreviewStringSettings.dialog 'open'
    'Language':
      title: i18n.dialog.languagesettings.title, handler: (rowData, dialogs)->
        dialogs.dictPreviewLangSettings.data "param", rowData
        dialogs.dictPreviewLangSettings.dialog 'open'

  dicGrid = $('#dictListPreviewGrid').jqGrid {
  url: '', datatype: 'json', editurl: "",
  width: 1000, minHeight: 200, height: 240
  pager: '#dictListPreviewPager', rowNum: 100
  sortname: 'base.name', sortorder: 'asc'
  viewrecords: true, cellEdit: true, cellurl: '/app/deliver-update-dict'
  gridview: true, multiselect: true
  caption: i18n.grid.dictlistpreview.caption
  colNames: ['LangRefCode', 'Dictionary', 'Version', 'Format', 'Encoding', 'Labels','Error','Warning', 'Action']
  colModel: [
    {name: 'langrefcode', index: 'langrefcode', width: 55, align: 'center', hidden: true}
    {name: 'name', index: 'base.name', width: 200, editable: true, align: 'left'}
    {name: 'version', index: 'version', width: 25, editable: true, align: 'center'}
    {name: 'format', index: 'base.format', width: 60, editable: true, edittype: 'select',
    editoptions: {value: "DCT:DCT;Dictionary conf:Dictionary conf;Text properties:Text properties;XML labels:XML labels"},
    align: 'center'}
    {name: 'encoding', index: 'base.encoding', width: 40, editable: true, edittype: 'select',
    editoptions: {value: 'ISO-8859-1:ISO-8859-1;UTF-8:UTF-8;UTF-16LE:UTF-16LE;UTF-16BE:UTF-16BE'}, align: 'center'}
    {name: 'labelNum', index: 'labelNum', width: 20, align: 'center'}
    {name: 'error', index: 'errorCount', width: 20, align: 'center'}
    {name: 'warning', index: 'warningCount', width: 20, align: 'center'}
    {name: 'action', index: 'action', width: 70, editable: false, align: 'center'}
  ]
  beforeProcessing: (data, status, xhr)->

    actIndex = $(@).getGridParam('colNames').indexOf('Action')
    --actIndex if $(@).getGridParam('multiselect')

    actions = []
    actions.push k for k,v of handlers

    grid=@
    $(data.rows).each (index)->
      rowData = @
      @cell[actIndex] = $(actions).map(
        ()->
          "<A id='action_#{@}_#{rowData.id}_#{actIndex}'style='color:blue' title='#{handlers[@].title}' href=# >#{@}</A>"
      ).get().join('&nbsp;&nbsp;&nbsp;&nbsp;')

  beforeSubmitCell: (rowid, cellname, value, iRow, iCol)->handler: ($(@).getGridParam 'postData').handler
  afterSubmitCell: (serverresponse, rowid, cellname, value, iRow, iCol)->
    jsonFromServer = eval "(#{serverresponse.responseText})"
    [0 == jsonFromServer.status, jsonFromServer.message]

  gridComplete: ->
    grid = $(@)

    #      high light error rows
    $("tr[class!='jqgfirstrow']",grid).each (index,row)->
      rowData = grid.getRowData(row.id)

      $(row).css 'background','#FFFFAA' if parseInt(rowData.warning)>0
      $(row).css 'background','#FFD2D2' if parseInt(rowData.error)>0

    $('a[id^=action_]', @).click ()->
      [a, action, rowid, col]=@id.split('_')
      rowData = grid.getRowData(rowid)
      delete rowData.action
      rowData.id = rowid
      rowData.handler= grid.getGridParam('postData').handler
      handlers[action].handler rowData, require 'appmng/dialogs'
  }
  dicGrid.jqGrid 'navGrid', '#dictListPreviewPager', {add: false, edit: false, search: false, del: false}, {}, {}, {}
