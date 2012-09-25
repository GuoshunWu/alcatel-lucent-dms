define ['jqlayout', 'jquery', 'i18n!nls/transmng', 'i18n!nls/common', 'transmng/trans_grid', 'require', 'jqmsgbox', 'transmng/transdetail_grid'], ($, jq, i18n, c18n, grid, require, msgbox, detailgrid)->
#  console.log module
#  private variables
  ids = {
  languageFilterTableId: 'languageFilterTable'
  languageFilterDialogId: 'languageFilterDialog'
  container:
    {
    page: 'optional-container'
    }
  }
  dialogs = null
  generateLanguageTable = (languages, tableId, colNum)->
    tableId = ids.languageFilterTableId if !tableId
    colNum = 5 if !colNum
    rowCount = Math.ceil(languages.length / colNum)

    languageFilterTable = $("<table id='#{tableId}' align='center' border='0'><tr valign='top' /></table>")
    outerTableFirstRow = $("tr:eq(0)", languageFilterTable)

    languageCells = $(languages).map ()->$("<td><input type='checkbox' checked value=\"#{@name}\" name='languages' id=#{@id} /><label for=#{@id}>#{@name}</label></td>").css('width', '180px')

    innerColTable = null
    languageCells.each (index)->
      if 0 == index % rowCount
        innerColTable = $("<table border='0'/>")
        outerTableFirstRow.append $("<td/>").append innerColTable
      innerColTable.append $("<tr/>").append @

    checkedAll = $("<input type='checkbox'id='all_#{tableId}' checked><label for='all_#{tableId}'>All</label>").change ()->
      $(":checkbox[name='languages']", languageFilterTable).attr('checked', @checked)
    #    hr line
    languageFilterTable.append $('<tr/>').append $("<td colspan='#{colNum}'/>").append $("<hr width='100%'>")
    #    check all line
    languageFilterTable.append $('<tr/>').append $("<td colspan='#{colNum}'></td>").append checkedAll

  createDialogs = ->
  #dialog
    languageFilterDialog = $("<div title='#{i18n.select.languagefilter.title}' id='#{ids.languageFilterDialogId}'>").dialog {
    autoOpen: false, position: [23, 126], height: 'auto', width: 'auto'
    show: { effect: 'slide', direction: "up" }
    create: ->$.getJSON 'rest/languages?prop=id,name', {}, (languages)=>$(@).append(generateLanguageTable languages)
    buttons: [
      { text: c18n.ok, click: ()->
        $('#productRelease').trigger "change"
        $(@).dialog "close"
      }
      {text: c18n.cancel, click: ()->$(@).dialog "close"}
    ]
    }
    taskDialog = $("#createTranslationTaskDialog").dialog {
    autoOpen: false, width: 'auto', height: 'auto', position: [25, 100], show: { effect: 'slide', direction: "down" }
    open: ->
      info = grid.getTotalSelectedRowInfo()
      #      tableType is app or dict
      tableType = grid.getTableType()
      nums = info.selectedNum

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
      #      postData = ($({prop: 'id,name'}).attr tableType, info.rowIds.join(',')).get(0)

      $.getJSON 'rest/languages', postData, (languages)=>$(@).append generateLanguageTable languages, langFilterTableId if languages.length > 0

    buttons: [
      {text: c18n.create
      click: ->
        languages = ($(":checkbox[name='languages']", $(@)).map -> {id: @id, name: @value} if @checked).get()
        if(languages.length == 0)
          $.msgBox (i18n.msgbox.createtranstask.msg.format c18n.language), null,
            title: (i18n.msgbox.createtranstask.title.format c18n.language),
            width: 300, height: "auto"
        return
        $(@).dialog "close"
      }
      {text: c18n.cancel, click: -> $(@).dialog "close"}
    ]
    }
    transDetailDialog = $('#translationDetailDialog').dialog {
    autoOpen: false, width: 'auto', height: 400
    create:()->
      $('#detailLanguageSwitcher').change ->
        dict=$('#translationDetailDialog').data "dict"
        language={id:$(@).val(), name:$(@).find("option:selected").text()}
        detailgrid.languageChanged {language:language,dict:dict}
    }

    {taskDialog: taskDialog, languageFilterDialog: languageFilterDialog, transDetailDialog: transDetailDialog}

  createSelects = ->
  # selects on summary panel
    $.getJSON 'rest/products/trans/productbases', {}, (json)->
      $('#productBase').append new Option(i18n.select.product.tip, -1)
      $('#productBase').append $(json).map ()->new Option @name, @id

    #  load product in product base
    $('#productBase').change ()->
      $('#productRelease').empty()
      return false if parseInt($('#productBase').val()) == -1

      $.getJSON "rest/products/#{$('#productBase').val()}", {}, (json)->
        $('#productRelease').append new Option(i18n.select.release.tip, -1)
        $('#productRelease').append $(json).map ()->new Option @version, @id
        $('#productRelease').trigger "change"

    $('#productRelease').change ->
      param = {
      release: {id: $(@).val(), version: $(@).find("option:selected").text()}
      languages: ($(":checkbox[name='languages']", $("#" + ids.languageFilterDialogId)).map () -> {id: @id, name: @value} if @checked).get()
      level: $(":radio[name='viewOption'][checked]").val()
      }
      if !$('#productBase').val() || parseInt($('#productBase').val()) == -1
      #        $.msgBox i18n.select.product.msg, null,title: i18n.select.product.msgtitle, width: 300, height: "auto"
        return false

      if !param.release.id || parseInt(param.release.id) == -1
      #        $.msgBox i18n.select.release.msg, null, title: i18n.select.release.msgtitle, width: 300 , height: "auto"
        return false
      grid.productReleaseChanged param

  createButtons = (taskDialog, languageFilterDialog) ->
  #   buttons summary panel
    $("#create").button().click ->
      require('jqmsgbox')
      info = grid.getTotalSelectedRowInfo()
      type = $(':radio[name=viewOption][checked]').val()
      if !info.selectedNum
        $.msgBox (i18n.msgbox.createtranstask.msg.format c18n[grid.getTableType()]), null,
          title: (i18n.msgbox.createtranstask.title.format c18n[grid.getTableType()]),
          width: 300, height: "auto"
        return
      taskDialog.dialog "open"

    $('#languageFilter').button().click ()->languageFilterDialog.dialog "open"

    #    for view level
    $(':radio[name=viewOption]').change -> $('#productRelease').trigger "change"

    (($("#translated").button().click ->
      selectedRowIds = $("#transGridList").getGridParam('selarrrow');
      console.log selectedRowIds
    ).next().button().click ->
      selectedRowIds = $("#transGridList").getGridParam('selarrrow');
      console.log selectedRowIds
      alert "N"
    ).parent().buttonset()

    (($("#detailTranslated").button().click ->
      selectedRowIds = $("#transDetailGridList").getGridParam('selarrrow');
    ).next().button().click ->
      selectedRowIds = $("#transDetailGridList").getGridParam('selarrrow');
    ).parent().buttonset()

  #  private method
  initPage = ->
    pageLayout = $("##{ids.container.page}").layout {resizable: true, closable: true}
    ###################################### Elements in summary panel ######################################
    createSelects()
    ###################################### Initialize elements in north panel ######################################
    dialogs = createDialogs()
    createButtons(dialogs.taskDialog, dialogs.languageFilterDialog, dialogs.transDetailDialog)
  # initialize page
  initPage()

  #    public variables and methods
  name: 'layout'

  showTransDetailDialog: (param)->
  #    refresh dialog
    $('#dictionaryName', dialogs.transDetailDialog).html param.dict.name
    $('#detailLanguageSwitcher', dialogs.transDetailDialog).append ($(param.languages).map (index) ->
      opt = new Option  @name,@id
      opt.selected = @name == param.language.name
      opt
    )
    $('#translationDetailDialog').data 'dict',param.dict
    $('#detailLanguageSwitcher').trigger "change"

    dialogs.transDetailDialog.dialog "open"


