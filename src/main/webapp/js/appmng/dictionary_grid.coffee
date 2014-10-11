define [
  'jqgrid'
  'blockui'
  'jqmsgbox'
  'jqueryui'

  'i18n!nls/appmng'
  'i18n!nls/common'
  'dms-util'
  'dms-urls'

  # following dependency are not referenced directly.
  'appmng/langsetting_grid'
  'appmng/stringsettings_grid'
  'appmng/history_grid'
], ($, blockui, msgbox,ui, i18n, c18n, util, urls)->

  dictGridId= 'dictionaryGridList'
#  console?.log "module appmng/dictionary_grid loading."
  #  for form edit delete option

  deleteOptions = {
  msg: i18n.dialog.delete.delmsg.format c18n.dict
  reloadAfterSubmit: false
  url: urls.app.remove_dict
  beforeShowForm: (form)->
    permanent = $('#permanentDeleteSignId', form)
    #    make permanent sign default checked and hide
    $("<tr><td>#{i18n.grid.permanenttext}<td><input align='left'checked type='checkbox' id='permanentDeleteSignId'>")
    .hide().appendTo $("tbody", form) if permanent.length == 0
  #    permanent?.removeAttr 'checked'
  afterShowForm: (formid)->
    $(formid).parent().parent().position(my:'center', at: 'center', of: window)
  onclickSubmit: (params, posdata)->
    $.blockUI()
    pData =
      appId: $("#selAppVersion").val()
      permanent: Boolean($('#permanentDeleteSignId').attr("checked"))
    pData

  afterSubmit: (response, postdata)->
    $.unblockUI()
    jsonFromServer = eval "(#{response.responseText})"
    return [true, jsonFromServer.message] if(0 == jsonFromServer.status)
    #check if theres task attached to dictionaries

    if(1 == jsonFromServer.status)
      taskList = jsonFromServer.message
      confirmInfo = i18n.grid.confirmdeldict.format(taskList)
#      console?.log confirmInfo
      $.msgBox confirmInfo, ((keyPressed)->
        if c18n.yes == keyPressed
          $.blockUI()
          $.post deleteOptions.url, $.extend({}, postdata, deleteTask:true), (json)->
            $.unblockUI()
            if(0!=json.status)
              $.msgBox json.message, null, {title: c18n.error}
              $('#' + dictGridId).trigger 'reloadGrid'
              return
            $.msgBox json.message, null, {title: c18n.info}
        else
          $('#' + dictGridId).trigger 'reloadGrid'

      ),{title: c18n.confirm, width: 510}, [c18n.yes, c18n.no]
      return [true, jsonFromServer.message]

    [1 == jsonFromServer.status, jsonFromServer.message]
  }

  openDialogHandler = (rowData, dialogId)->
    $("##{dialogId}").data("param", rowData).dialog 'open'

  handlers =
    'String':
      handler: (rowData)->openDialogHandler(rowData, 'stringSettingsDialog')
    'Language':
      handler: (rowData)->openDialogHandler(rowData, 'languageSettingsDialog')

  lastEditedCell = null

  colModel = [
    {name: 'langrefcode', index: 'langrefcode', width: 55, align: 'left', hidden: true}
    {name: 'name', index: 'base.name', width: 200, editable: false, align: 'left'}
    {name: 'version', index: 'version', width: 25, editable: true, classes: 'editable-column', edittype: 'select', editoptions: {value: {}}, align: 'left'}
    {name: 'format', index: 'base.format', width: 60, editable: true, edittype: 'select', align: 'left'
    editoptions: {value: c18n.dictformats},
    }
    {name: 'encoding', index: 'base.encoding', width: 40, editable: true, edittype: 'select',
    editoptions: {value: c18n.dictencodings}, align: 'left'}
    {name: 'labelNum', index: 'labelNum', width: 20, align: 'right', firstsortorder: 'desc'}
    {name: 'errors', index: 'errorCount', width: 20, align: 'right'
#      unformat:(cellvalue, options, cell)->$('a', cell).text()
    }
    {name: 'warnings', index: 'warningCount', width: 20, align: 'right'}
    {name: 'action', index: 'action', width: 70, editable: false, align: 'center', sortable: false
    formatter: (cellvalue, options, rowObject)->
      $.map(handlers,
        (value, index)->"<A id='action_#{index}_#{options.rowId}' style='color:blue' title='#{index} Settings'href='javascript:void(0)'>#{index}</A>"
      ).join('&nbsp;&nbsp;&nbsp;&nbsp;')
    }
    {name: 'history', index: 'history', width: 25, editable: false, align: 'center', sortable: false,formatter: (cellvalue, options, rowObject)->
      "<img class='historyAct' id='hisact_#{options.rowId}'  src='images/history.png'>"
    }
    {name: 'cellaction', index: 'cellaction', width: 20, editable: false, align: 'center', sortable: false, formatter: 'actions'
    formatoptions: {keys: true, delbutton: true, delOptions: deleteOptions, editbutton: false}
    }
  ]
  $(colModel).each (index, colModel)->colModel.classes = 'editable-column' if colModel.editable

  prop = "languageReferenceCode,base.name,version,base.format,base.encoding,labelNum, errorCount, warningCount"
  dicGrid = $('#' + dictGridId).jqGrid({
  url: urls.dicts
  postData: {format: 'grid', prop: prop}
  datatype: 'local'
  width: 1000
  height: 320
  pager: '#dictPager'
  editurl: urls.app.add_app
  cellactionurl: urls.app.remove_dict
  rowNum: 999, loadonce: false
  sortname: 'base.name'
  sortorder: 'asc'
  viewrecords: true, cellEdit: true, cellurl: urls.app.update_dict, ajaxCellOptions: {async: false}
  gridview: true, multiselect: true
  caption: i18n.grid.dictsforapp
  colNames: ['LangRefCode', 'Dictionary', 'Version', 'Format', 'Encoding', 'Labels', 'Error', 'Warning', 'Action','History', 'Del']
  colModel: colModel
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
    $('a[id^=action_]', @).click ()->
      [a, action, rowid]=@id.split('_')
      #      save grid edit before get data
      grid.saveCell(lastEditedCell.iRow, lastEditedCell.iCol) if lastEditedCell
      rowData = grid.getRowData(rowid)
      rowData.id = rowid
      delete rowData.action

      handlers[action].handler rowData

    $('a', @).css 'color', 'blue'

    $('a[id^=warnAndErr_]', @).click ()->
      [_, name, rowid]=@id.split '_'
      value = $(@).text()
      return if parseInt(value) == 0
      # open the dialog to show errors or warnings
      rowData = grid.getRowData(rowid)
      rowData.id = rowid
      rowData.type = name
      $("#dictValidationDialog").data("param", rowData).dialog 'open'

    #      high light error rows
    $("tr[class!='jqgfirstrow']", @).each (index, row)->
      rowData = grid.getRowData(row.id)

      $(row).css 'background', '#FFFFAA' if parseInt($(rowData.warnings).text()) > 0
      $(row).css 'background', '#FFD2D2' if parseInt($(rowData.errors).text()) > 0

    $('img.historyAct', @).click(()->
      grid.saveCell(lastEditedCell.iRow, lastEditedCell.iCol) if lastEditedCell
      [_, rowid]=@id.split('_')
      rowData = grid.getRowData(rowid)
      rowData.id = rowid
      delete rowData.action
      $('#historyDialog').data('param', rowData).dialog 'open'
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


  $('#generateDict').button().width(170).attr('privilegeName', util.urlname2Action urls.app.generate_dict).click ->
  #    Test
    dicts = dicGrid.getGridParam('selarrrow')
    if !dicts || dicts.length == 0
      $.msgBox (c18n.selrow.format c18n.dict), null, {title: c18n.warning}
      return
    #Popup a dialog  "Generate options" when export dictionary of type "XML properties".
    checkType = "XML properties"
    dictTypes = (dicGrid.getRowData(id).format for id in dicts when dicGrid.getRowData(id).format is checkType)
#    console.log("dictType=%o", dictTypes)

    d = $.Deferred()
    if(dictTypes.length > 0)
      $('#XMLPropertiesDictionaryExportOptionsDialog').data('param', d).dialog 'open'
    else
      d.resolve(escape: false)

    d.done (param)=>
      filename = "#{$('#appDispAppName').text()}_#{$('#selAppVersion option:selected').text()}_#{new Date().format 'yyyyMMdd_hhmm'}.zip"

      $(@).button 'disable'
      oldLabel = $(@).button 'option', 'label'
      $(@).button 'option', 'label', i18n.generating

      me=$(@)
      pb = util.genProgressBar()
      util.updateProgress(urls.app.generate_dict, {dicts: dicts.join(','), filename: filename, escapeApostrophe: param.escape}, (json)->
        pb.parent().remove()
        me.button 'option', 'label', oldLabel
        me.button 'enable'
        dowloadURL = urls.getURL(urls.app.download_app_dict,'',fileLoc:json.event.msg, filename: filename)
  #      console?.log dowloadURL
        window.location.href = dowloadURL
      , pb)


  $('#batchAddLanguage').button().attr('privilegeName', util.urlname2Action urls.app.add_dict_language).click ->
    dicts = dicGrid.getGridParam('selarrrow')
    if !dicts || dicts.length == 0
      $.msgBox (c18n.selrow.format c18n.dict), null, {title: c18n.warning}
      return
    #    pass parameters to dialog
    $('#addLanguageDialog').data('param', dicts: dicts).dialog 'open'

  $('#batchDeleteLanguage').button().attr('privilegeName', util.urlname2Action urls.app.remove_dict_language).click ->
    dicts = dicGrid.getGridParam('selarrrow')
    if !dicts || dicts.length == 0
      $.msgBox (c18n.selrow.format c18n.dict), null, {title: c18n.warning}
      return
    $('#removeLanguageDialog').data('param', dicts: dicts).dialog 'open'

  capitalizeId = '#dictCapitalize'
  menu = $(capitalizeId + 'Menu', '#DMS_applicationPanel').hide().menu(
    select: ( event, ui )->
      dicts = dicGrid.getGridParam('selarrrow')
      ($.msgBox (c18n.selrow.format c18n['dict']), null, title: c18n.warning; return) unless dicts.length
      $('#capitalizationDialog').data(params: {dicts: dicts.join(','), style: $('a',ui.item).prop('name')}).dialog('open')
  )

  $(capitalizeId, '#DMS_applicationPanel').button(
    create:(event, ui)->
    icons: {
      primary: "ui-icon-triangle-1-n"
    }
  ).on('click', (e)->
    menu.width($(@).width() - 3).show().position(my: "left bottom", at: "left top", of: this)
    $(document).one("click", ()->menu.hide())
    false
  )


  appChanged: (param)->
    dicGrid.setGridParam(postData: {app: param.app.id}).trigger "reloadGrid"
    dicGrid.setCaption "Dictionaries for Application #{param.base.text} version #{param.app.version}"

