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

  handlers =
    'String':
      url: ''
      title: i18n.dialog.stringsettings.title, handler: (rowData)->
      #        grid.saveCell(lastEditedCell.iRow, lastEditedCell.iCol) if lastEditedCell
        $('#stringSettingsDialog').data("param", rowData).dialog 'open'
    'Language':
      url: ''
      title: i18n.dialog.languagesettings.title, handler: (rowData)->
        $('#languageSettingsDialog').data("param", rowData).dialog 'open'

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
    {name: 'action', index: 'action', width: 70, editable: false, align: 'center', sortable: false
    formatter: (cellvalue, options, rowObject)->
      $.map(handlers,
        (value, index)->"<A id='action_#{index}_#{options.rowId}' style='color:blue' title='#{value.title}'href='javascript:void(0)'>#{index}</A>"
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


  dicGrid = $('#' + dictGridId).jqGrid({
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
  caption: i18n.grid.dictsforapp
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

    filename = "#{$('#appDispAppName').text()}_#{$('#selAppVersion option:selected').text()}_#{new Date().format 'yyyyMMdd_hhmm'}.zip"

    $(@).button 'disable'
    oldLabel = $(@).button 'option', 'label'
    $(@).button 'option', 'label', i18n.generating

    me=$(@)
    pb = util.genProgressBar()
    util.updateProgress(urls.app.generate_dict, {dicts: dicts.join(','), filename: filename}, (json)->
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
      if !dicts.length
        $.msgBox (c18n.selrow.format c18n['dict']), null, title: c18n.warning
        return
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
    prop = "languageReferenceCode,base.name,version,base.format,base.encoding,labelNum"
    dicGrid.setGridParam(url: 'rest/dict', postData: {app: param.app.id, format: 'grid', prop: prop}).trigger "reloadGrid"
    dicGrid.setCaption "Dictionaries for Application #{param.base.text} version #{param.app.version}"

