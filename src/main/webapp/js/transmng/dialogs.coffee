define [
  'edialog'
  'jqmsgbox'
  'i18n!nls/transmng'
  'i18n!nls/common'
  'dms-util'
  'dms-urls'

  'transmng/trans_grid'
  'transmng/transdetail_grid'
  'transmng/trans_searchtext_grid'
  'transmng/trans_matchtext_grid'
  'transmng/translation_history_grid_detail_view'

  'transmng/trans_histories_grid'

], ($, msgbox, i18n, c18n, util, urls, grid, detailgrid, searchgrid, matchgrid, historygrid, historiesGrid)->
  transGrid = grid

  refreshGrid = (languageTrigger = false, grid = transGrid)->
    nodeInfo=util.getProductTreeInfo()
    type = nodeInfo.type
    param =
      release:
        {id: $('#selVersion', "div[id='transmng']").val(), version: $("#selVersion option:selected", "div[id='transmng']").text()}
      level: $("input:radio:checked[name='viewOption']").val()
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

      $.getJSON urls.languages, postData, (languages)=>$(@).append util.generateLanguageTable languages, langFilterTableId if languages.length > 0
    buttons: [
      {text: c18n['export'], click: ->
        me = $(@)
        languages = ($(":checkbox[name='languages']", @).map -> {id: @id, name: @value} if @checked).get()
        if(languages.length == 0)
          $.msgBox (i18n.msgbox.createtranstask.msg.format c18n.language), null, title: (c18n.warning)
          return

        langIds = $(languages).map(()->@id).get().join ','
        dictIds = $(grid.getTotalSelectedRowInfo().rowIds).map(()->@).get().join(',')

        $(@).dialog 'close'

        pb = util.genProgressBar()
        util.updateProgress(urls.trans.generate_translation_details, {dict: dictIds, lang: langIds}, (json)->
          pb.parent().remove()
          console.log(json);
          downloadURL = urls.getURL(urls.trans.export_translation_details,'',translationDetailId: json.event.msg)
#          console?.log downloadURL
          location.href = downloadURL
        , pb)
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

      $.getJSON urls.languages, postData, (languages)=>
#        append language options in translation task secondary reference

        languageOptions = util.json2Options [{name: '', id: -1}].concat(languages), '', "name"
        $('#translationTaskSecondaryReference').empty().append languageOptions
#        append language table
        $(@).append util.generateLanguageTable languages, langFilterTableId if languages.length > 0
    close: -> $('#transTaskErr').hide()
    buttons: [
      {text: c18n.create
      click: ->
        taskDialog = $(@)
        languages = ($(":checkbox[name='languages']", @).map -> {id: @id, name: @value} if @checked).get()
        if(languages.length == 0)
          $.msgBox c18n.languagerequired, null, title: (c18n.warning)
          return
        name = $('#taskName').val()
        if '' == name
          $('#transTaskErr').show()
          return
        langids = $(languages).map(
          ()->@id).get().join ','
        dicts = $(grid.getTotalSelectedRowInfo().rowIds).map(
          ()->@).get().join(',')
        secondaryRefLanguage = parseInt $('#translationTaskSecondaryReference').val()

        postData =
          language: langids
          dict: dicts
          name: name

        postData.secondaryRefLanguage = secondaryRefLanguage if secondaryRefLanguage != -1

#        console.log "postData=%o", postData

        type = util.getProductTreeInfo().type
        type = 'prod' if 'product' == type
        postData[type] = $('#selVersion', '#transmng').val()

        taskDialog.parent().block()
        $.post 'task/create-task', postData, (json)->
          taskDialog.parent().unblock()
          if(json.status != 0)
            $.msgBox json.message, null, {title: c18n.error}
            return
          $('#taskGrid').trigger 'reloadGrid'
          $.msgBox i18n.msgbox.createtranstask.confirm, ((keyPressed)->
            #            navigate form is in common/pagenavigator.jsp
            if c18n.yes != keyPressed
              $("#transGrid").trigger 'reloadGrid'
              return
            # refresh taskGrid
            $("span[id^='nav'][value='taskmng']").trigger('click')
          ), {title: c18n.confirm}, [c18n.yes, c18n.no]

          taskDialog.dialog "close"
      }
      {text: c18n.cancel, click: -> $(@).dialog "close"}
    ]
  )
  transDetailDialog = $('#translationDetailDialog').dialog(
    autoOpen: false, width: 860, height: 'auto'

    open: ()->
      me= $(@)
      util.adjustDialogAndInnerGridSize(me, $("#transDetailGridList"), {width: 100, height: 50}, {width: 50, height: 270})
      $('#transDetailSearchAction',@).position(my: 'left center', at: 'right center', of: '#transDetailSearchText')
      param = $(@).data "param"
      $('#dictionaryName', @).html param.dict.name
      util.getDictLanguagesByDictId param.dict.id, (languages)=>
        $('#detailLanguageSwitcher', @).empty().append(util.json2Options languages, param.language.id, 'name').trigger 'change'
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
  ).dialogExtend(
#    "maximizable" : true
    "minimizable" : true
#    "collapsable" : true
#    "dblclick" : "collapse"
  )


  transUpdateDialog = $('#transmngTranslationUpdate')
  handler = (e)->
    return if !e.clientX
    param = transUpdateDialog.data('param')
    gridToReload = param.gridToReload
    delete param.gridToReload
    postData = $.extend(confirm: c18n.yes == $(e.target).text() , param)
#    console?.log postData

    $.post urls.trans.update_translation, postData , (json)->
      ($.msgBox(json.message, null, title: c18n.error); return) unless json.status == 0
      $("#transDetailGridList").trigger 'reloadGrid'
      gridToReload?.trigger 'reloadGrid'

    transUpdateDialog.dialog 'close'

  transUpdateDialog.dialog(
    autoOpen: false, width: 800, maxHeight: 600, title: c18n.confirm, modal: true
    buttons:[
      {text: c18n.yes, click: handler }
      {text: c18n.no, click: handler }
    ]
  )

  transSearchText = $('#transmngSearchTextDialog').dialog(
    autoOpen: false, width: 1020, height: 'auto', modal: true
    open: ()->
      me=$(@)
      transSearchGrid =  $("#transSearchTextGrid")

      util.adjustDialogAndInnerGridSize(me, transSearchGrid, {width: 100, height: 50}, {width: 30, height: 190})

      params = me.data 'params'
#      console.log "params= ", params
      node = util.getProductTreeInfo()

      caption = i18n.searchtext.caption.format(
        params.text,
        if 'prod' == node.type then 'product' else 'application',
        unless -1 == node.parent then node.text else $('#versionTypeLabel').text(),
        params.version.text,
        params.language.text)

      postData = transSearchGrid.getGridParam('postData')


      postData.exact = $('#transSearchText_exact').prop('checked')
      postData.text = params.text
      postData.language = params.language.id
      delete postData.app
      delete postData.prod
      postData[node.type] = params.version.id

#      console.log("postData=", postData)
      transSearchGrid.setGridParam page: 1

      transSearchGrid.setCaption(caption).trigger 'reloadGrid'

    close: ()->
      searchgrid.saveLastEditedCell()
    buttons: [
      {text: c18n.close, click: ()->
        $(@).dialog 'close'
      }
    ]
  )

  showSearchResult = (params)->transSearchText.data('params', params).dialog 'open'

  transHistoriesDialog = $('#transHistoriesDialog').dialog(
    autoOpen: false, width: 1020, height: 'auto', modal: true
    open: ()->
      me = $(@)
      params = me.data "params"
      grid = $('#transHistoriesGrid')
      util.adjustDialogAndInnerGridSize(me, grid)

      grid.setCaption(params.caption).setGridParam("postData":{
        appId: params.id, page: 1
        from: $('#operationTimeBegin').val(), to: $('#operationTimeEnd').val()
      })
      sgrid = grid[0]
      sgrid.clearToolbar()

    close: ()->
    buttons: [
      {text: c18n.close, click: ()->
        $(@).dialog 'close'
      }
    ]

  )

  transMatchText = $('#transmngMatchTextDialog').dialog(
    autoOpen: false
    open: ->
      transMatchGrid = $("#transMatchTextGrid")
#      $(":button:contains('#{c18n.ok}')", $(this).next('div.ui-dialog-buttonpane')).prop("disabled", !hasData)
      param = $(@).data('param')
      # +_hibernate_class:com.alcatel_lucent.dms.model.Translation +text.reference:text~0.8 + status:2 + language.id:46
      languageId = $('#detailLanguageSwitcher').val()

      postData = transMatchGrid.getGridParam('postData')
      postData.language = languageId
      postData.text = param.refText
      postData.format = 'grid'
      postData.fuzzy = true
      postData.transId = param.transId
      postData.labelId = param.labelId
      postData.prop = 'reference, translation, score'

      transMatchGrid.setGridParam(page: 1).trigger 'reloadGrid'

    width: 1000, height: 'auto', modal: true

    buttons: [
      {text: c18n.ok, click: ()->
        grid = $("#transMatchTextGrid")
        selId = grid.getGridParam('selrow')
        hasData = grid.getRowData().length > 0

        if hasData or not selId
          $.msgBox('Please select translation to apply.', null, title: c18n.error)
          return
        rowData=grid.getRowData(selId)
#        console?.log rowData
        postData = grid.getGridParam('postData')
        $.post(urls.trans.update_translation, {
          oper: 'edit'
          translation: rowData.translation
          id: postData.labelId
          ctid: postData.transId
        },(json)->
          if json.status != 1
            $("#transDetailGridList").trigger 'reloadGrid'
            return

          dictList = "<ul>\n  <li>#{json.dicts.join('</li>\n  <li>')}</li>\n</ul>"
          showMsg = i18n.msgbox.updatetranslation.msg.format dictList
          delete json.dicts
          delete json.message
          delete json.status
          $('#transmngTranslationUpdate').html(showMsg).data('param', json).dialog 'open'

        )
        $(@).dialog 'close'
      }
      {text: c18n.cancel, click: ()->  $(@).dialog 'close'}
    ]
  )

  transHistoryDialogInDetailView = $('#translationHistoryDialogInDetailView').dialog(
    autoOpen: false, modal: true
    width: 845
    open: (event, ui)->
      param = $(@).data('param')
      return unless param

      $('#detailViewTranslationHistoryGrid').setGridParam(
        url: urls.translation_histories
        page: 1
        postData: {transId: param.transId, format: 'grid', prop: 'operationTime,operationType,operator.name,translation, status, memo'}
      ).setCaption(c18n.history.caption.format param.reflang).trigger "reloadGrid"

    buttons: [
      {text: c18n.close, click: (e)->
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
  showSearchResult: showSearchResult
