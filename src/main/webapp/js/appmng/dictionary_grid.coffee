define (require, util, dialogs, i18n)->
  $ = require 'jqgrid'
  util = require 'dms-util'
  dialogs = require 'appmng/dialogs'
  i18n = require 'i18n!nls/appmng'
  require('jqmsgbox')
  c18n = require 'i18n!nls/common'
  blockui = require 'blockui'

  #  for form edit delete option
  deleteOptions = {
  msg: i18n.dialog.delete.delmsg.format c18n.dict
  top: 250, left: 550
  reloadAfterSubmit: false
  url: 'app/remove-dict'
  beforeShowForm: (form)->
    permanent = $('#permanentDeleteSignId', form)
    #    make permanent sign default checked and hide
    $("<tr><td>#{i18n.grid.permanenttext}<td><input align='left'checked type='checkbox' id='permanentDeleteSignId'>")
    .hide().appendTo $("tbody", form) if permanent.length == 0
  #    permanent?.removeAttr 'checked'
  onclickSubmit: (params, posdata)->
    {appId: $("#selAppVersion").val(), permanent: Boolean($('#permanentDeleteSignId').attr("checked"))}
  afterSubmit: (response, postdata)->
    jsonFromServer = eval "(#{response.responseText})"
    #dictBase is deleted
    #remove dictionary base.
    [0 == jsonFromServer.status, jsonFromServer.message]
  }

  handlers =
    'String':
      url: ''
      title: i18n.dialog.stringsettings.title, handler: (rowData)->
      #        grid.saveCell(lastEditedCell.iRow, lastEditedCell.iCol) if lastEditedCell
        dialogs.stringSettings.data "param", rowData
        dialogs.stringSettings.dialog 'open'
    'Language':
      url: ''
      title: i18n.dialog.languagesettings.title, handler: (rowData)->
      #        grid.saveCell(lastEditedCell.iRow, lastEditedCell.iCol) if lastEditedCell
      #        dialogs.langSettings.on 'dialogopen', {param: rowData}, $('#languageSettingsDialog').dialog('option', 'openEvent')
        dialogs.langSettings.data "param", rowData
        dialogs.langSettings.dialog 'open'
  #    'X': title: i18n.dialog.delete.title, handler: (rowData)->$('#dictionaryGridList').jqGrid 'delGridRow', rowData.id, deleteOptions

  lastEditedCell = null

  colModel = [
    {name: 'langrefcode', index: 'langrefcode', width: 55, align: 'left', hidden: true}
    {name: 'name', index: 'base.name', width: 200, editable: false, align: 'left'}
    {name: 'version', index: 'version', width: 25, editable: true, classes: 'editable-column', edittype: 'select', editoptions: {value: {}}, align: 'left'}
    {name: 'format', index: 'base.format', width: 60, editable: true, edittype: 'select',
    editoptions: {value: c18n.dictformats},
    align: 'left'}
    {name: 'encoding', index: 'base.encoding', width: 40, editable: true, edittype: 'select',
    editoptions: {value: c18n.dictencodings}, align: 'left'}
    {name: 'labelNum', index: 'labelNum', width: 20, align: 'right'}
    {name: 'action', index: 'action', width: 70, editable: false, align: 'center',
    formatter: (cellvalue, options, rowObject)->
      $.map(handlers,
        (value, index)->"<A id='action_#{index}_#{options.rowId}' style='color:blue' title='#{value.title}'href=# >#{index}</A>"
      ).join('&nbsp;&nbsp;&nbsp;&nbsp;')
    }
    {name: 'history', index: 'history', width: 25, editable: false, align: 'center', formatter: (cellvalue, options, rowObject)->
      "<img class='historyAct' id='hisact_#{options.rowId}'  src='images/history.png'>"
    }
    {name: 'cellaction', index: 'cellaction', width: 20, editable: false, align: 'center', formatter: 'actions'
    formatoptions: {keys: true, delbutton: true, delOptions: deleteOptions, editbutton: false}
    }
  ]
  $(colModel).each (index, colModel)->colModel.classes = 'editable-column' if colModel.editable


  dicGrid = $('#dictionaryGridList').jqGrid({
  url: 'json/dummy.json'
  datatype: 'local'
  width: 1000
  height: 320
  cellactionhandlers: handlers
  pager: '#dictPager'
  editurl: "app/create-or-add-application"
  cellactionurl: 'app/remove-dict'
  rowNum: 999, loadonce: false
  sortname: 'base.name'
  sortorder: 'asc'
  viewrecords: true, cellEdit: true, cellurl: 'app/update-dict', ajaxCellOptions: {async: false}
  gridview: true, multiselect: true
  caption: 'Dictionary for Application'
  colNames: ['LangRefCode', 'Dictionary', 'Version', 'Format', 'Encoding', 'Labels', 'Action','History', 'Del']
  colModel: colModel
  beforeProcessing: (data, status, xhr)->
  afterEditCell: (id, name, val, iRow, iCol)->
    lastEditedCell = {iRow: iRow, iCol: iCol, name: name, val: val}
    grid = @
    if name == 'version'
      $.ajax {url: "rest/dict?slibing=#{id}&prop=id,version", async: false, dataType: 'json', success: (json)->
        $("##{iRow}_version", grid).append util.json2Options json, val
      }
  beforeSubmitCell: (rowid, cellname, value, iRow, iCol)->
    isVersion = cellname == 'version'
    $(@).setGridParam cellurl: if isVersion then 'app/change-dict-version' else 'app/update-dict'
    if isVersion then {appId: $("#selAppVersion").val(), newDictId: value} else {}

  afterSubmitCell: (serverresponse, rowid, cellname, value, iRow, iCol)->
    jsonFromServer = eval "(#{serverresponse.responseText})"
    [0 == jsonFromServer.status, jsonFromServer.message]

  gridComplete: ->
    grid = $(@)
    handlers = grid.getGridParam 'cellactionhandlers'
    $('a[id^=action_]', @).click ()->
      [a, action, rowid]=@id.split('_')
      #      save grid edit before get data
      grid.saveCell(lastEditedCell.iRow, lastEditedCell.iCol) if lastEditedCell
      rowData = grid.getRowData(rowid)
      rowData.id = rowid
      delete rowData.action

      handlers[action].handler rowData

    $('img.historyAct', @).click(()->
      grid.saveCell(lastEditedCell.iRow, lastEditedCell.iCol) if lastEditedCell
      [_, rowid]=@id.split('_')
      rowData = grid.getRowData(rowid)
      rowData.id = rowid
      delete rowData.action

      dialogs.historyDlg.data 'param', rowData
      dialogs.historyDlg.dialog 'open'
    ).on('mouseover',()->
      $(@).addClass('ui-state-hover')
    ).on('mouseout', ()->
      $(@).removeClass('ui-state-hover')
    )

  }).jqGrid('navGrid', '#dictPager', {add: false, edit: false, search: false, del: false}, {}, {}, deleteOptions)
  .setGridParam(datatype: 'json')
  #  .setGroupHeaders(useColSpanStyle: true, groupHeaders: [
  #      {startColumnName: "action", numberOfColumns: 2, titleText: 'Action'.bold()}
  #    ])


  #  custom button for del dictionary
  #  unless util.urlname2Action('app/remove-dict') in param.forbiddenPrivileges
  dicGrid.navButtonAdd('#dictPager', {id: "custom_del_#{dicGrid.attr 'id'}", caption: "", buttonicon: "ui-icon-trash", position: "first", onClickButton: ()->
    if(rowIds = $(@).getGridParam('selarrrow')).length == 0
      $.msgBox (c18n.selrow.format c18n.dict), null, {title: c18n.warning}
      return
    $(@).jqGrid 'delGridRow', rowIds, deleteOptions
  })


  $('#generateDict').button().width(170).attr('privilegeName', util.urlname2Action 'app/deliver-app-dict').click ->
  #    Test
    dicts = dicGrid.getGridParam('selarrrow')
    if !dicts || dicts.length == 0
      $.msgBox (c18n.selrow.format c18n.dict), null, {title: c18n.warning}
      return

    filename = "#{$('#appDispAppName').text()}_#{$('#selAppVersion option:selected').text()}_#{new Date().format 'yyyyMMdd_hhmmss'}.zip"
    #    $.blockUI()
    $(@).button 'disable'
    oldLabel = $(@).button 'option', 'label'
    $(@).button 'option', 'label', i18n.generating
    $.post 'app/generate-dict', {dicts: dicts.join(','), filename: filename}, (json)=>
    #      $.unblockUI()
      $(@).button 'option', 'label', oldLabel
      $(@).button 'enable'

      if(json.status != 0)
        $.msgBox json.message, null, {title: c18n.error}
        return

      window.location.href = "app/download-app-dict.action?fileLoc=#{json.fileLoc}"


  $('#batchAddLanguage').button().attr('privilegeName', util.urlname2Action 'app/add-dict-language').click ->
    dicts = dicGrid.getGridParam('selarrrow')
    if !dicts || dicts.length == 0
      $.msgBox (c18n.selrow.format c18n.dict), null, {title: c18n.warning}
      return
    #    pass parameters to dialog
    $('#addLanguageDialog').data 'param', dicts: dicts
    $('#addLanguageDialog').dialog 'open'

  appChanged: (param)->
    prop = "languageReferenceCode,base.name,version,base.format,base.encoding,labelNum"
    dicGrid.setGridParam(url: 'rest/dict', postData: {app: param.app.id, format: 'grid', prop: prop}).trigger "reloadGrid"
    dicGrid.setCaption "Dictionary for Application #{param.base.text} version #{param.app.version}"

