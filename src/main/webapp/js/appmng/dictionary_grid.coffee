define (require, util, dialogs, i18n)->
  $ = require 'jqgrid'
  util = require 'util'
  dialogs = require 'appmng/dialogs'
  i18n = require 'i18n!nls/appmng'
  require('jqmsgbox')
  c18n = require 'i18n!nls/common'
  blockui = require 'blockui'

  #  for form edit delete option
  deleteOptions = {
  msg: i18n.dialog.delete.delmsg.format c18n.dict
  top: 250, left: 550
  reloadAfterSubmit: false, url: 'app/remove-dict'
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
      title: i18n.dialog.stringsettings.title, handler: (rowData)->
        dialogs.stringSettings.data "param", rowData
        dialogs.stringSettings.dialog 'open'
    'Language':
      title: i18n.dialog.languagesettings.title, handler: (rowData)->
      #        dialogs.langSettings.data "param", rowData
        dialogs.langSettings.on 'dialogopen', {param: rowData}, $('#languageSettingsDialog').dialog('option', 'openEvent')
        dialogs.langSettings.dialog 'open'
    'X': title: i18n.dialog.delete.title, handler: (rowData)->$('#dictionaryGridList').jqGrid 'delGridRow', rowData.id, deleteOptions

  lastEditedCell = null

  colModel = [
    {name: 'langrefcode', index: 'langrefcode', width: 55, align: 'left', hidden: true}
    {name: 'name', index: 'base.name', width: 200, editable: false, align: 'left'}
    {name: 'version', index: 'version', width: 25, editable: true, classes: 'editable-column', edittype: 'select', editoptions: {value: {}}, align: 'left'}
    {name: 'format', index: 'base.format', width: 60, editable: true, edittype: 'select',
    editoptions: {value: "DCT:DCT;Dictionary conf:Dictionary conf;Text properties:Text properties;XML labels:XML labels"},
    align: 'left'}
    {name: 'encoding', index: 'base.encoding', width: 40, editable: true, edittype: 'select',
    editoptions: {value: 'ISO-8859-1:ISO-8859-1;UTF-8:UTF-8;UTF-16LE:UTF-16LE;UTF-16BE:UTF-16BE'}, align: 'left'}
    {name: 'labelNum', index: 'labelNum', width: 20, align: 'right'}
    {name: 'action', index: 'action', width: 80, editable: false, align: 'center'}
  ]
  $(colModel).each (index, colModel)->colModel.classes = 'editable-column' if colModel.editable


  dicGrid = $('#dictionaryGridList').jqGrid({
  url: 'json/dummy.json'
  datatype: 'local'
  width: 1000
  height: 320
  pager: '#dictPager'
  editurl: "app/create-or-add-application"
  rowNum: 999, loadonce: false
  sortname: 'base.name'
  sortorder: 'asc'
  viewrecords: true, cellEdit: true, cellurl: 'app/update-dict', ajaxCellOptions: {async: false}
  gridview: true, multiselect: true
  caption: 'Dictionary for Application'
  colNames: ['LangRefCode', 'Dictionary', 'Version', 'Format', 'Encoding', 'Labels', 'Action']
  colModel: colModel
  beforeProcessing: (data, status, xhr)->
    actIndex = $.inArray('Action', $(@).getGridParam('colNames'))
    --actIndex if $(@).getGridParam('multiselect')
    actions = []
    actions.push k for k,v of handlers

    $(data.rows).each (index)->
      rowData = @
      @cell[actIndex] = $(actions).map(
        ()->
          "<A id='action_#{@}_#{rowData.id}_#{actIndex}'style='color:blue' title='#{handlers[@].title}' href=# >#{@}</A>"
      ).get().join('&nbsp;&nbsp;&nbsp;&nbsp;')
  afterEditCell: (id, name, val, iRow, iCol)->
    lastEditedCell = {iRow: iRow, iCol: iCol, name: name, val: val}
    grid = @
    if name == 'version'
    #        console.log "name=#{name},id=#{id},val=#{val}"
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

    $('a[id^=action_]', @).click ()->
      [a, action, rowid, col]=@id.split('_')
      #      save grid edit before get data
      grid.saveCell(lastEditedCell.iRow, lastEditedCell.iCol) if lastEditedCell

      rowData = grid.getRowData(rowid)

      delete rowData.action
      rowData.id = rowid
      handlers[action].handler rowData
  }).jqGrid('navGrid', '#dictPager', {add: false, edit: false, search: false, del: false}, {}, {}, deleteOptions)
  #  custom button for del dictionary
  .navButtonAdd('#dictPager', {caption: "", buttonicon: "ui-icon-trash", position: "first"
      onClickButton: ()->
        if(rowIds = $(@).getGridParam('selarrrow')).length == 0
          $.msgBox (c18n.selrow.format c18n.dict), null, {title: c18n.warning}
          return
        $(@).jqGrid 'delGridRow', rowIds, deleteOptions
    }).setGridParam(datatype: 'json')


  ($('#generateDict').button {}).click ->
  #    Test
    dicts = dicGrid.getGridParam('selarrrow')
    if !dicts || dicts.length == 0
      $.msgBox (c18n.selrow.format c18n.dict), null, {title: c18n.warning}
      return

    filename = "#{$('#appDispAppName').text()}_#{$('#selAppVersion option:selected').text()}_#{new Date().format 'yyyyMMdd_hhmmss'}.zip"

    $.blockUI()
    $.ajax 'app/generate-dict', dataType: 'json', timeout: 1000 * 60 * 30, data: {dicts: dicts.join(','), filename: filename}, success: (json, textStatus, jqXHR)->
      $.unblockUI()
      if(json.status != 0)
        $.msgBox json.message, null, {title: c18n.error}
        return

      downloadForm = $('#downloadDict')
      $('#fileLoc', downloadForm).val json.fileLoc
      downloadForm.submit()


  ($('#batchAddLanguage').button {}).click ->
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
