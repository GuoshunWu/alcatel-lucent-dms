define (require, util, dialogs, i18n)->
  $ = require 'jqgrid'
  util = require 'util'
  dialogs = require 'appmng/dialogs'
  i18n = require 'i18n!nls/appmng'
  require('jqmsgbox')
  c18n = require 'i18n!nls/common'
  blockui = require 'blockui'


  localIds = {
  dic_grid: '#dictionaryGridList'
  }
  deleteOptions = {
  reloadAfterSubmit: false, url: 'app/remove-dict'
  beforeShowForm: (form)->
    permanent = $('#permanentDeleteSignId', form)
    $("<tr><td>#{i18n.grid.permanenttext}<td><input align='left' type='checkbox' id='permanentDeleteSignId'>")
    .appendTo $("tbody", form) if permanent.length == 0
    permanent?.removeAttr 'checked'
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
      #    {dictId: rowData.id, refCode: rowData.langrefcode}
        dialogs.langSettings.data "param", rowData
        dialogs.langSettings.dialog 'open'
    'X': title: i18n.dialog.delete.title, handler: (rowData)->$(localIds.dic_grid).jqGrid 'delGridRow', rowData.id, deleteOptions

  dicGrid = $(localIds.dic_grid).jqGrid {
  url: ''
  datatype: 'json'
  width: 1000
  height: 320
  pager: '#dictPager'
  editurl: "app/create-or-add-application"
  rowNum: 10, rowList: [10, 20, 30]
  sortname: 'base.name'
  sortorder: 'asc'
  viewrecords: true, cellEdit: true, cellurl: 'app/update-dict'
  gridview: true, multiselect: true
  caption: 'Dictionary for Application'
  colNames: ['LangRefCode', 'Dictionary', 'Version', 'Format', 'Encoding', 'Labels', 'Action']
  colModel: [
    {name: 'langrefcode', index: 'langrefcode', width: 55, align: 'left', hidden: true}
    {name: 'name', index: 'base.name', width: 200, editable: false, align: 'left'}
    {name: 'version', index: 'version', width: 25, editable: true, edittype: 'select', editoptions: {value: {}}, align: 'left'}
    {name: 'format', index: 'base.format', width: 60, editable: true, edittype: 'select',
    editoptions: {value: "DCT:DCT;Dictionary conf:Dictionary conf;Text properties:Text properties;XML labels:XML labels"},
    align: 'left'}
    {name: 'encoding', index: 'base.encoding', width: 40, editable: true, edittype: 'select',
    editoptions: {value: 'ISO-8859-1:ISO-8859-1;UTF-8:UTF-8;UTF-16LE:UTF-16LE;UTF-16BE:UTF-16BE'}, align: 'left'}
    {name: 'labelNum', index: 'labelNum', width: 20, align: 'right'}
    {name: 'action', index: 'action', width: 80, editable: false, align: 'center'}
  ]
  beforeProcessing: (data, status, xhr)->
    actIndex = $(@).getGridParam('colNames').indexOf('Action')
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
    grid = @
    if name == 'version'
    #        console.log "name=#{name},id=#{id},val=#{val}"
      $.ajax {url: "rest/dict?slibing=#{id}&prop=id,version", async: false, dataType: 'json', success: (json)->
        $("##{iRow}_version", grid).append $(json).map ()->opt = new Option(@version, @id);opt.selected = @version == val; opt
      }
  beforeSubmitCell: (rowid, cellname, value, iRow, iCol)->
    isVersion = cellname == 'version'
    $(@).setGridParam cellurl: if isVersion then '/app/change-dict-version' else '/app/update-dict'
    if isVersion then {appId: $("#selAppVersion").val(), newDictId: value} else {}

  afterSubmitCell: (serverresponse, rowid, cellname, value, iRow, iCol)->
    jsonFromServer = eval "(#{serverresponse.responseText})"
    [0 == jsonFromServer.status, jsonFromServer.message]
  gridComplete: ->
    grid = $(@)

    $('a[id^=action_]', @).click ()->
      [a, action, rowid, col]=@id.split('_')
      rowData = grid.getRowData(rowid)
      delete rowData.action
      rowData.id = rowid
      handlers[action].handler rowData
  }
  dicGrid.jqGrid 'navGrid', '#dictPager', {add: false, edit: false, search: false}, {}, {}, deleteOptions

  ($('#generateDict').button {}).click ->
  #    Test
    dicts = dicGrid.getGridParam('selarrrow')
    if !dicts || dicts.length == 0
      $.msgBox c18n.selrow, null, {title: c18n.warning}
      return

    filename = "#{$('#appDispAppName').text()}_#{$('#selAppVersion option:selected').text()}_#{new Date().format 'yyyyMMdd_hhmmss'}.zip"

    $.blockUI()
    $.post '/app/generate-dict', {dicts: dicts.join(','), filename: filename}, (json)->
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
      $.msgBox c18n.selrow, null, {title: c18n.warning}
      return
    $('#languageSettingGrid').editGridRow "new", {
    url: 'app/add-dict-language'
    onclickSubmit: (params, posdata)->{dicts: dicts.join(',')}
    beforeInitData: ->$('#languageSettingGrid').setColProp 'code', editable: true
    onClose: ->$('#languageSettingGrid').setColProp 'code', editable: false
    afterSubmit: (response, postdata)->
      jsonfromServer = eval "(#{response.responseText})"
      [jsonfromServer.status == 0 , jsonfromServer.message, -1]
    }
  appChanged: (param)->
    prop = "languageReferenceCode,base.name,version,base.format,base.encoding,labelNum"
    dicGrid.setGridParam(url: 'rest/dict', postData: {app: param.app.id, format: 'grid', prop: prop}).trigger "reloadGrid"
    dicGrid.setCaption "Dictionary for Application #{param.base.text} version #{param.app.version}"
