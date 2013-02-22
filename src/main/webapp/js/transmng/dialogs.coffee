define (require)->
  i18n = require 'i18n!nls/transmng'
  c18n = require 'i18n!nls/common'
  grid = require 'transmng/trans_grid'

  util = require 'dms-util'
  #  detailgrid = require 'transmng/transdetail_grid'

  transGrid = grid
  refreshGrid = (languageTrigger = false, grid = transGrid)->

    nodeInfo=(require 'ptree').getNodeInfo()

    type = nodeInfo.type
    type = type[..3] if type.startWith('prod')

    param =
      release:
        {id: $('#selVersion', "div[id='transmng']").val(), version: $("#selVersion option:selected", "div[id='transmng']").text()}
      level: $("input:radio[name='viewOption'][checked]").val()
      type: type

    checkboxes = $("#languageFilterDialog input:checkbox[name='languages']")

    param.languages = checkboxes.map(
      ()-> return {id: @id, name: @value} if @checked).get()
    param.languageTrigger = languageTrigger

    grid.updateGrid param


    console?.debug "transmng panel dialogs init..."
  ################################################ Create Dialogs #################################################
  languageFilterDialog = $("<div title='#{i18n.select.languagefilter.title}' id='languageFilterDialog'>").dialog(
    autoOpen: false, position: [23, 126], height: 'auto', width: 1100
    show:
      { effect: 'slide', direction: "up" }
    buttons: [
      { text: c18n.ok, click: ()->
        $(@).dialog "close"
        refreshGrid(true)
      }
      {text: c18n.cancel, click: ()->$(@).dialog "close"}
    ]
  )

  exportTranslationDialog = $('#ExportTranslationsDialog').dialog(
    autoOpen: false, modal: true
    width: 1100, height: 'auto', position: [25, 100], show:
      { effect: 'slide', direction: "down" }
    open: ->
      info = grid.getTotalSelectedRowInfo()
      #      tableType is app or dict
      tableType = grid.getTableType()

      langFilterTableId = "languageFilter_#{$(@).attr('id')}"
      $("##{langFilterTableId}").remove()
      postData = {prop: 'id,name'}
      postData[tableType] = info.rowIds.join(',')

      $.getJSON 'rest/languages', postData, (languages)=>$(@).append util.generateLanguageTable languages, langFilterTableId if languages.length > 0
    buttons: [
      {text: c18n.export, click: ->
        me = $(@)
        languages = ($(":checkbox[name='languages']", @).map -> {id: @id, name: @value} if @checked).get()
        if(languages.length == 0)
          $.msgBox (i18n.msgbox.createtranstask.msg.format c18n.language), null, title: (c18n.warning)
          return

        langids = $(languages).map(
          ()->@id).get().join ','
        dicts = $(grid.getTotalSelectedRowInfo().rowIds).map(
          ()->@).get().join(',')
        window.location.href = "trans/export-translation-details?dict=#{dicts}&lang=#{langids}"
        $(@).dialog 'close'
      }
      {text: c18n.cancel, click: ->$(@).dialog 'close'}
    ]
  )

  taskDialog = $("#createTranslationTaskDialog").dialog(
    autoOpen: false, modal: true
    width: 1100, height: 'auto', position: [25, 100], show:
      { effect: 'slide', direction: "down" }
    create: ->
    open: ->
      info = grid.getTotalSelectedRowInfo()
      taskname = "#{$('#productBase option:selected').text()}_#{$('#productRelease option:selected').text()}"
      taskname += "_#{new Date().format('yyyyMMddhhmmss')}"
      $('#taskName').val(taskname).select()
      #      tableType is app or dict
      tableType = grid.getTableType()
      nums = info.rowIds.length

      $("#dictSelected").html "<b>#{nums}</b>"

      if 'app' == tableType
        nums = -1
      #        get dictionary number of selected application from rest

      $("#totalLabels").html "<b>#{info.totalLabels}</b>"
      # update target languages from rest

      langFilterTableId = "languageFilter_#{$(@).attr('id')}"
      $("##{langFilterTableId}").remove()
      postData = {prop: 'id,name'}
      postData[tableType] = info.rowIds.join(',')

      $.getJSON 'rest/languages', postData, (languages)=>$(@).append util.generateLanguageTable languages, langFilterTableId if languages.length > 0
    close: -> $('#transTaskErr').hide()
    buttons: [
      {text: c18n.create
      click: ->
        taskDialog = $(@)
        languages = ($(":checkbox[name='languages']", @).map -> {id: @id, name: @value} if @checked).get()
        if(languages.length == 0)
          $.msgBox (i18n.msgbox.createtranstask.msg.format c18n.language), null, title: (c18n.warning)
          return
        name = $('#taskName').val()
        if '' == name
          $('#transTaskErr').show()
          return
        langids = $(languages).map(
          ()->@id).get().join ','
        dicts = $(grid.getTotalSelectedRowInfo().rowIds).map(
          ()->@).get().join(',')

        taskDialog.parent().block()
        $.post 'task/create-task', {prod: $('#productRelease').val(), language: langids, dict: dicts, name: name }, (json)->
          taskDialog.parent().unblock()
          if(json.status != 0)
            $.msgBox json.message, null, {title: c18n.error}
            return
          $.msgBox i18n.msgbox.createtranstask.confirm, ((keyPressed)->
            #            navigate form is in common/pagenavigator.jsp
            if c18n.yes != keyPressed
              $("#transGrid").trigger 'reloadGrid'
              return
            $('#pageNavigator').val 'taskmng.jsp'
            $('#naviForm').submit()
          ), {title: c18n.confirm}, [c18n.yes, c18n.no]

          taskDialog.dialog "close"
      }
      {text: c18n.cancel, click: -> $(@).dialog "close"}
    ]
  )

  transDetailDialog = $('#translationDetailDialog').dialog(
    autoOpen: false, width: 860, height: 'auto', modal: true
    create: ()->
      $(@).dialog 'option', 'width', $('#transDetailGridList').getGridParam() + 60
      $('#detailLanguageSwitcher').change ->
        param = $('#translationDetailDialog').data "param"
        language = {id: $(@).val(), name: $("option:selected", @).text()}
        detailgrid.languageChanged {language: language, dict: param.dict, searchStatus: param.searchStatus}
    close: (event, ui)->
      detailgrid.saveLastEditedCell()
    buttons: [
      {text: c18n.close, click: ()->
        $(@).dialog 'close'
      }
    ]
  )

  ready = ()->
    console?.debug "transmng panel dialogs ready..."

  ready()

  taskDialog: taskDialog
  languageFilterDialog: languageFilterDialog,
  transDetailDialog: transDetailDialog
  exportTranslationDialog: exportTranslationDialog

  refreshGrid: refreshGrid