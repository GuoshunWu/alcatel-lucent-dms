define [
  'jqmsgbox'
  'i18n!nls/transmng'
  'i18n!nls/common'
  'dms-util'
  'dms-urls'
  'transmng/trans_grid'
  'transmng/transdetail_grid'
  'transmng/trans_searchtext_grid'
  'transmng/trans_matchtext_grid'
], ($, i18n, c18n, util, urls, grid, detailgrid, searchgrid, matchgrid)->
#  console?.log "transmng panel dialogs init..."
  transGrid = grid
  refreshGrid = (languageTrigger = false, grid = transGrid)->
    nodeInfo=util.getProductTreeInfo()
    type = nodeInfo.type
    param =
      release:
        {id: $('#selVersion', "div[id='transmng']").val(), version: $("#selVersion option:selected", "div[id='transmng']").text()}
      level: $("input:radio[name='viewOption'][checked]").val()
      type: type
      name: nodeInfo.text
    checkboxes = $("#languageFilterDialog input:checkbox[name='languages']")
    param.languages = checkboxes.map(
      ()-> return {id: @id, name: @value} if @checked).get()
    param.languageTrigger = languageTrigger
    param.release.id = -1 unless param.release.id
    grid.updateGrid param


  ################################################ Create Dialogs #################################################
  languageFilterDialog = $("<div title='#{i18n.select.languagefilter.title}' id='languageFilterDialog'>").dialog(
    autoOpen: false, height: 'auto', width: 1100
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
    width: 1100, height: 'auto', show:
      { effect: 'slide', direction: "down" }
    open: ->
      info = grid.getTotalSelectedRowInfo()
      # tableType is app or dict
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
  )

  taskDialog = $("#createTranslationTaskDialog").dialog(
    autoOpen: false, modal: true
    width: 1100, height: 'auto', show:
      { effect: 'slide', direction: "down" }
    create: ->
    open: ->
      info = grid.getTotalSelectedRowInfo()
      taskname = "#{$('#versionTypeLabel', '#transmng').text()}_#{$('#selVersion option:selected', '#transmng').text()}"
      taskname += "_#{new Date().format('yyyyMMdd')}"
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


        postData =
          language: langids
          dict: dicts
          name: name

        type = util.getProductTreeInfo().type
        type = 'prod' if 'product' == type
        postData[type] = $('#selVersion', '#transmng').val()

        taskDialog.parent().block()
        $.post 'task/create-task', postData, (json)->
          taskDialog.parent().unblock()
          if(json.status != 0)
            $.msgBox json.message, null, {title: c18n.error}
            return
          $.msgBox i18n.msgbox.createtranstask.confirm, ((keyPressed)->
            #            navigate form is in common/pagenavigator.jsp
            if c18n.yes != keyPressed
              $("#transGrid").trigger 'reloadGrid'
              return

            $("span[id^='nav'][value='taskmng']").trigger('click')
          ), {title: c18n.confirm}, [c18n.yes, c18n.no]

          taskDialog.dialog "close"
      }
      {text: c18n.cancel, click: -> $(@).dialog "close"}
    ]
  )

  transDetailDialog = $('#translationDetailDialog').dialog(
    autoOpen: false, width: 860, height: 'auto', modal: true

    open: ()->
      $('#transDetailSearchAction',@).position(my: 'left center', at: 'right center', of: '#transDetailSearchText')
      $('#detailLanguageSwitcher').trigger "change"
    create: ()->
      $(@).dialog 'option', 'width', $('#transDetailGridList').getGridParam('width') + 60
      transDetailGrid = $("#transDetailGridList")
      postData = transDetailGrid.getGridParam('postData')

      $('#transDetailSearchText', @).keydown (e)=>
        return true if e.which != 13
        $('#transDetailSearchAction', @).trigger 'click'
        false

      $('#transDetailSearchAction', @).attr('title', 'Search').button(text: false, icons:{primary: "ui-icon-search"}).click(()=>
        postData.text = $('#transDetailSearchText', @).val()
        transDetailGrid.trigger 'reloadGrid'
      ).height(20).width(20)

      $('#transSameWithRef', @).change (e)->
        postData.nodiff = @checked
        #          console?.log transDetailGrid.getGridParam('postData')
        transDetailGrid.trigger 'reloadGrid'

      $('#detailLanguageSwitcher').change ->
        param = $('#translationDetailDialog').data "param"
        language = {id: $(@).val(), name: $("option:selected", @).text()}
        detailgrid.languageChanged {language: language, dict: param.dict, searchStatus: param.searchStatus, transsrc: param.transsrc}
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


  transUpdateDialog = $('#transmngTranslationUpdate')
  handler = (e)->
    return if !e.clientX
    param = transUpdateDialog.data('param')
    postData = $.extend(confirm: c18n.yes == $(e.target).text() , param)
#    console?.log postData

    $.post urls.trans.update_translation, postData , (json)->
      unless json.status == 0
        $.msgBox(json.message, null, title: c18n.error)
        return
      $("#transDetailGridList").trigger 'reloadGrid'
    transUpdateDialog.dialog 'close'

  transUpdateDialog.dialog(
    autoOpen: false, width: 800, title: c18n.confirm, modal: true

    buttons:[
      {text: c18n.yes, click: handler }
      {text: c18n.no, click: handler }
    ]
  )

  transSearchText = $('#transmngSearchTextDialog').dialog(
    autoOpen: false, width: 1020, height: 'auto', modal: true
    open: ()->
      params = $(@).data 'params'
      grid =  $("#transSearchTextGrid")
      node=util.getProductTreeInfo()
      typeText = if 'prod' == node.type then 'product' else 'application'

      postData = grid.getGridParam('postData')

      postData.format = 'grid'
      postData.text = params.text
      postData.language = params.language.id
      postData.prop = 'app.name,dictionary.name,key,maxLength,context.name,reference,ct.translation,ct.status,ct.id'

      delete postData.app
      delete postData.prod

      postData[node.type] = params.version.id
      name = unless -1 == node.parent then node.text else $('#versionTypeLabel').text()
      grid.setCaption(i18n.searchtext.caption.format params.text, typeText, name, params.version.text, params.language.text)
        .setGridParam(url: urls.labels).trigger 'reloadGrid'

    buttons: [
      {text: c18n.close, click: ()->
        $(@).dialog 'close'
      }
    ]
  )

  showSearchResult = (params)->transSearchText.data('params', params).dialog 'open'

  $('#transmngMatchTextDialog').dialog(
    autoOpen: false
    width: 1000, height: 'auto', modal: true

    buttons: [
      {text: c18n.ok, click: ()->
        grid = $("#transMatchTextGrid")
        selId = grid.getGridParam('selrow')
        if grid.getRowData().length > 0 and not selId
          $.msgBox('Please select translation to apply.', null, title: c18n.error)
          return
        #TODO: apply translation
        console.log "Selected: translation id: " + selId
        $(@).dialog 'close'
      }
    ]
  )


  ready = ()->
#    console?.log "transmng panel dialogs ready..."

  ready()

  taskDialog: taskDialog
  languageFilterDialog: languageFilterDialog,
  transDetailDialog: transDetailDialog
  exportTranslationDialog: exportTranslationDialog

  refreshGrid: refreshGrid
  showTransDetailDialog: (param)->
    #    refresh dialog
    $('#dictionaryName', transDetailDialog).html param.dict.name
    $('#detailLanguageSwitcher', transDetailDialog).empty().append (util.json2Options param.languages, param.language.id, 'name')

    map = 'N': '0', 'I': '1', 'T': '2'
    status = param.language.name.split('.')[1]

    transDetailDialog.data('param', {dict: param.dict, searchStatus: map[status]}, transsrc: '').dialog "open"

  showSearchResult: showSearchResult
