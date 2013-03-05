define ['jqlayout', 'blockui', 'jqmsgbox', 'i18n!nls/common', 'i18n!nls/transmng', 'transmng/trans_grid', 'transmng/transdetail_grid', 'util', 'require'], ($, blockui, msgbox, c18n, i18n, grid, detailgrid, util)->
  util = require 'util'
  ids =
    languageFilterTableId: 'languageFilterTable'
    languageFilterDialogId: 'languageFilterDialog'


  dialogs = null

  refreshGrid = (languageTrigger = false)->
    param =
      release: {id: $('#productRelease').val(), version: $("#productRelease option:selected").text()}
      level: $("input:radio[name='viewOption'][checked]").val()

    checkboxes = $("##{ids.languageFilterDialogId} input:checkbox[name='languages']")
    param.languages = checkboxes.map(
      ()-> return {id: @id, name: @value} if @checked).get()
    param.languageTrigger = languageTrigger
    grid.productReleaseChanged param

  createDialogs = ->
  #dialog
    languageFilterDialog = $("<div title='#{i18n.select.languagefilter.title}' id='#{ids.languageFilterDialogId}'>").dialog {
    autoOpen: false, position: [23, 126], height: 'auto', width: 1100
    show: { effect: 'slide', direction: "up" }
    buttons: [
      { text: c18n.ok, click: ()->
        $(@).dialog "close"
        refreshGrid(true)
      }
      {text: c18n.cancel, click: ()->$(@).dialog "close"}
    ]
    }

    exportTranslationDialog = $('#ExportTranslationsDialog').dialog {
    autoOpen: false,
    modal: true
    width: 1100, height: 'auto', position: [25, 100], show: { effect: 'slide', direction: "down" }
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
      {text: c18n['export'], click: ->
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
    }
    taskDialog = $("#createTranslationTaskDialog").dialog {
    autoOpen: false, modal: true
    width: 1100, height: 'auto', position: [25, 100], show: { effect: 'slide', direction: "down" }
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
    }

    transDetailDialog = $('#translationDetailDialog').dialog(
      autoOpen: false, width: 860, height: 'auto', modal: true
      open: ()->
        $('#searchAction',@).position(my: 'left center', at: 'right center', of: '#searchText')
        $('#detailLanguageSwitcher').trigger "change"

      create: ()->
        $(@).dialog 'option', 'width', $('#transDetailGridList').getGridParam('width') + 60
        transDetailGrid = $("#transDetailGridList")
        postData = transDetailGrid.getGridParam('postData')

        $('#searchText', @).keydown (e)=>$('#searchAction', @).trigger 'click' if e.which == 13
        $('#searchAction', @).attr('title', 'Search').button(text: false, icons:{primary: "ui-icon-search"}).click(()=>
          postData.text = $('#searchText', @).val()
          transDetailGrid.trigger 'reloadGrid'
        ).height(20).width(20)

        $('#transSameWithRef', @).change (e)->
          postData.nodiff = @checked
#          console?.debug transDetailGrid.getGridParam('postData')
          transDetailGrid.trigger 'reloadGrid'

        $('#detailLanguageSwitcher').change ->
          param = $('#translationDetailDialog').data "param"
          language = {id: $(@).val(), name: $("option:selected", @).text()}
          detailgrid.languageChanged {language: language, dict: param.dict, searchStatus: param.searchStatus}
      close: (event, ui)->
        detailgrid.saveLastEditedCell()
        postData = $("#transDetailGridList").getGridParam('postData')

        $('#transSameWithRef', @).attr('checked', false)
        delete postData.nodiff

        $('#searchText', @).val("")
        delete postData.text

      buttons: [
        {text: c18n.close, click: ()->
          $(@).dialog 'close'
        }
      ]
    )


    taskDialog: taskDialog, languageFilterDialog: languageFilterDialog,
    transDetailDialog: transDetailDialog, exportTranslationDialog: exportTranslationDialog


  debugIntervalHandler = ()->
    console.log('Hello world.')

  createSelects = ->
  # selects on summary panel
  #  load product in product base
    $('#productBase').change ()->
      $('#productRelease').empty()
      return false if parseInt($('#productBase').val()) == -1

      $.getJSON "rest/products/version", {base: $(@).val(), prop: 'id,version'}, (json)->
        $('#productRelease').append util.newOption(c18n.select.release.tip, -1)
        $('#productRelease').append util.json2Options json, json[json.length - 1].id
        $('#productRelease').trigger "change"


    $('#productRelease').change ->
      return if -1 == parseInt @value
      $.ajax {url: "rest/languages", async: false, data: {prod: @value, prop: 'id,name'}, dataType: 'json', success: (languages)->
        langTable = util.generateLanguageTable languages
        $("#languageFilterDialog").empty().append langTable
      }
      refreshGrid()


    $('#productRelease').trigger 'change'

  createButtons = (dialogs) ->
  #   buttons summary panel
    $("#create").button()
    .attr('privilegeName', util.urlname2Action 'task/create-task')
    .click ->
      info = grid.getTotalSelectedRowInfo()
      if !info.rowIds.length
        $.msgBox (c18n.selrow.format c18n[grid.getTableType()]), null, title: c18n.warning
        return
      dialogs.taskDialog.dialog "open"

    $('#languageFilter').button().click ()->dialogs.languageFilterDialog.dialog "open"
    #    for view level
    $(':radio[name=viewOption]').change ->refreshGrid()

    $("#exportTranslation").button()
    .attr('privilegeName', util.urlname2Action 'trans/export-translation-details')
    .click ->
      info = grid.getTotalSelectedRowInfo()
      if !info.rowIds.length
        $.msgBox (c18n.selrow.format c18n[grid.getTableType()]), null, title: c18n.warning
        return
      dialogs.exportTranslationDialog.dialog 'open'

  exportAppOrDicts = (ftype)->
    id = $('#productRelease').val()
    return if !id
    id = parseInt(id)
    return if -1 == id

    checkboxes = $("#languageFilterDialog input:checkbox[name='languages']:checked")
    languages = checkboxes.map(
      ()-> return @id
    ).get().join(',')

    type = $("input:radio[name='viewOption'][checked]").val()
    type = type[..3]
    type = type[..2] if type[0] == 'a'

    $("#exportForm input[name='prod']").val id
    $("#exportForm input[name='language']").val languages
    $("#exportForm input[name='type']").val type
    $("#exportForm input[name='type']").val ftype if ftype
    $("#exportForm").submit()

  #  private method
  initPage = ->
  ###################################### Initialize elements in north panel ######################################
    dialogs = createDialogs()
    ###################################### Elements in summary panel ######################################
    createSelects()

    createButtons(dialogs)

    #    add action for export
    $("#exportExcel").click ()->exportAppOrDicts 'excel'
    $("#exportPDF").click ()->exportAppOrDicts 'pdf'


    #   show main page.
    $('#loading-container').fadeOut 'slow', ()->$(@).remove()
    util.afterInitilized(this)
    $('#optional-container').show()
    gridParent = $('.transGrid_parent')
    $('#transGrid').setGridWidth(gridParent.width() - 10).setGridHeight(gridParent.height() - 110)
  # initialize page
  initPage()


  #    public variables and methods
  name: 'layout'

  showTransDetailDialog: (param)->
  #    refresh dialog
    $('#dictionaryName', dialogs.transDetailDialog).html param.dict.name
    $('#detailLanguageSwitcher', dialogs.transDetailDialog).empty().append (util.json2Options param.languages, param.language.id, 'name')

    #   set status toolbar search to selected column
    transDetailGrid = $("#transDetailGridList")

    map = 'N': '0', 'I': '1', 'T': '2'
    status = param.language.name.split('.')[1]

    $('#translationDetailDialog').data 'param', {dict: param.dict, searchStatus: map[status]}
    dialogs.transDetailDialog.dialog "open"



