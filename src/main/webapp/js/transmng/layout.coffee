define ['jqlayout', 'require', 'blockui', 'jqmsgbox', 'i18n!nls/common', 'i18n!nls/transmng', 'transmng/trans_grid', 'transmng/transdetail_grid'], ($, require, blockui, msgbox, c18n, i18n, grid, detailgrid)->
#  console.log module
#  private variables
  util = require 'util'
  ids = {
  languageFilterTableId: 'languageFilterTable'
  languageFilterDialogId: 'languageFilterDialog'
  container:
    {
    page: 'optional-container'
    }
  }

  $('#pageNavigator').val(window.location.pathname)
  pageLayout = $("##{ids.container.page}").layout {resizable: true, closable: true}

  $(".header-footer").hover (->$(@).addClass "ui-state-hover"), -> $(@).removeClass "ui-state-hover"

  dialogs = null

  createDialogs = ->
  #dialog
    languageFilterDialog = $("<div title='#{i18n.select.languagefilter.title}' id='#{ids.languageFilterDialogId}'>").dialog {
    autoOpen: false, position: [23, 126], height: 'auto', width: 'auto'
    show: { effect: 'slide', direction: "up" }
    open: ->
      param = $(@).data 'param'
      if param then id = param.id else id = -1
      $.getJSON 'rest/languages', {prod: id, prop: 'id,name'}, (languages)=>
        $(@).text('').append util.generateLanguageTable languages
    buttons: [
      { text: c18n.ok, click: ()->
        $('#productRelease').trigger "change"
        $(@).dialog "close"
      }
      {text: c18n.cancel, click: ()->$(@).dialog "close"}
    ]
    }
    taskDialog = $("#createTranslationTaskDialog").dialog {
    autoOpen: false
    width: 'auto', height: 'auto', position: [25, 100], show: { effect: 'slide', direction: "down" }
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
        $.post '/task/create-task', {prod: $('#productRelease').val(), language: langids, dict: dicts, name: name }, (json)->
          taskDialog.parent().unblock()
          if(json.status != 0)
            $.msgBox json.message, null, {title: c18n.error}
            return
          if(confirm('Do you want to manage the task now?'))
            window.location = "/taskmng.jsp?productBase=#{escape $('#productBase').val()}&product=#{escape $('#productRelease').val()}"

          #            $('<form>').attr(method: 'post', action: '/taskmng.jsp').
          #            append($('<input>').attr(name: 'productBase').val($('#productBase').val()))
          taskDialog.dialog "close"
      }
      {text: c18n.cancel, click: -> $(@).dialog "close"}
    ]
    }
    transDetailDialog = $('#translationDetailDialog').dialog {
    autoOpen: false, width: 'auto', height: 400
    create: ()->
      $('#detailLanguageSwitcher').change ->
        dict = $('#translationDetailDialog').data "dict"
        language = {id: $(@).val(), name: $(@).find("option:selected").text()}
        detailgrid.languageChanged {language: language, dict: dict}
    }

    {taskDialog: taskDialog, languageFilterDialog: languageFilterDialog, transDetailDialog: transDetailDialog}

  createSelects = ->
  # selects on summary panel
    $.getJSON 'rest/products', {prop: 'id,name'}, (json)->
      $('#productBase').append new Option(c18n.select.product.tip, -1)
      $('#productBase').append $(json).map ()->new Option @name, @id

    #  load product in product base
    $('#productBase').change ()->
      $('#productRelease').empty()
      return false if parseInt($('#productBase').val()) == -1

      $.getJSON "/rest/products/version", {base: $(@).val(), prop: 'id,version'}, (json)->
        $('#productRelease').append new Option(c18n.select.release.tip, -1)
        $('#productRelease').append $(json).map ()->new Option @version, @id
        $('#productRelease').trigger "change"

    $('#productRelease').change ->
      param = {
      release: {id: $(@).val(), version: $(@).find("option:selected").text()}
      languages: ($(":checkbox[name='languages']", $("#" + ids.languageFilterDialogId)).map () -> {id: @id, name: @value} if @checked).get()
      level: $(":radio[name='viewOption'][checked]").val()
      }
      if !$('#productBase').val() || parseInt($('#productBase').val()) == -1
      #        $.msgBox i18n.select.product.msg, null,title: i18n.select.product.msgtitle
        return false
      $(languageFilterDialog).data 'param', param.release

      if !param.release.id || parseInt(param.release.id) == -1
        return false
      grid.productReleaseChanged param

  createButtons = (taskDialog, languageFilterDialog) ->
  #   buttons summary panel
    $("#create").button().click ->
      require('jqmsgbox')
      info = grid.getTotalSelectedRowInfo()
      type = $(':radio[name=viewOption][checked]').val()
      if !info.rowIds.length
        $.msgBox (i18n.msgbox.createtranstask.msg.format c18n[grid.getTableType()]), null, title: c18n.warning
        return
      taskDialog.dialog "open"

    $('#languageFilter').button().click ()->languageFilterDialog.dialog "open"
    #    for view level
    $(':radio[name=viewOption]').change -> $('#productRelease').trigger "change"


  #  private method
  initPage = ->
  ###################################### Elements in summary panel ######################################
    createSelects()
    ###################################### Initialize elements in north panel ######################################
    dialogs = createDialogs()
    createButtons(dialogs.taskDialog, dialogs.languageFilterDialog, dialogs.transDetailDialog)
    #   show main page.
    $('#optional-container').show()
    $('#loading-container').remove()

  # initialize page
  initPage()


  #    public variables and methods
  name: 'layout'

  showTransDetailDialog: (param)->
  #    refresh dialog
    $('#dictionaryName', dialogs.transDetailDialog).html param.dict.name
    $('#detailLanguageSwitcher', dialogs.transDetailDialog).append ($(param.languages).map (index) ->
      opt = new Option @name, @id
      opt.selected = @name == param.language.name
      opt
    )
    $('#translationDetailDialog').data 'dict', param.dict
    $('#detailLanguageSwitcher').trigger "change"

    dialogs.transDetailDialog.dialog "open"
