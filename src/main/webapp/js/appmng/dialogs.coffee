define [
  'jqueryui'
  'jqgrid'
  'blockui'
  'jqmsgbox'

  'i18n!nls/common'
  'i18n!nls/appmng'
  'dms-urls'
  'dms-util'



  'appmng/dictlistpreview_grid'
  'appmng/stringsettings_grid'

  'appmng/dictpreviewstringsettings_grid'
  'appmng/previewlangsetting_grid'
  'appmng/searchtext_grid'

], ($, jqgrid, blockui, msgbox, c18n, i18n, urls, util, previewgrid, stgrid)->

  #  console?.log "module appmng/dialogs loading."
  newProductVersion = $("#newProductReleaseDialog").dialog(
    autoOpen: false
    height: 200, width: 500, modal: true
    buttons: [
      {text: c18n.ok, click: ->
        url = urls.product.create_version
        versionName = $('#versionName').val()
        dupVersionId = $("#dupVersion").val()
        productBaseId = util.getProductTreeInfo().id

        if !versionName
          $("#productErrInfo").show()
          return

        $.post url, {version: versionName, dupVersionId: dupVersionId, id: productBaseId}, (json)->
          if (json.status != 0)
            $.msgBox json.message, null, {title: c18n.error, width: 300, height: 'auto'}
            return
          (require 'appmng/product_panel').addNewProduct {version: versionName, id: json.id}
        $(@).dialog "close"
      }
      {text: c18n.cancel, click: -> $(@).dialog "close"}
    ]
    open: (event, ui)->
      $('#dupVersion').empty().append util.newOption '', -1
      (require 'appmng/product_panel').getProductSelectOptions().appendTo $ '#dupVersion'
    close: (event, ui)->
      errDiv = $("#productErrInfo").hide()
  )

  newAppVersion = $("#newApplicationVersionDialog").dialog(
    autoOpen: false
    height: 200, width: 500, modal: true
    buttons: [
      {text: c18n.ok, click: ->
        url = 'app/create-application'
        versionName = $('#appVersionName').val()
        dupVersionId = $("#dupDictsVersion").val()
        appBaseId =  util.getProductTreeInfo().id

        if !versionName
          $("#appErrInfo").show()
          return

        $.post url, {version: versionName, dupVersionId: dupVersionId, id: appBaseId}, (json)->
          if (json.status != 0)
            $.msgBox json.message, null, {title: c18n.error, width: 300, height: 'auto'}
            return
          $('#selAppVersion').append("<option value='#{json.id}' selected>#{versionName}</option>").trigger 'change'
          return unless json.productBaseId
          $('#addNewApplicationVersionToProductVersionDialog').data("param", json).dialog 'open'
        $(@).dialog "close"
      }
      {text: c18n.cancel, click: -> $(@).dialog "close"}
    ]
    open: (event, ui)->
      $("#dupDictsVersion").empty().append(util.newOption '', -1).append $('#selAppVersion').children('option').clone(true)
    close: (event, ui)->
      $("#appErrInfo").hide()
  )

  $('#addNewApplicationVersionToProductVersionDialog').dialog(
    autoOpen: false
    width: 350, modal: true
    open: ->
      param = $(@).data 'param'
      $('#productBaseName', @).text param.productBaseName
      $('#productVersions', @).empty().append util.json2Options param.versions
    buttons: [
      {text: c18n.ok, click: ()->
        url = 'app/add-application'
        params =
          productId: $('#productVersions', @).val()
          appId: ($(@).data 'param').id

        $.post url, params, (json)->
          ($.msgBox json.message, null, {title: c18n.error}; return) if json.status != 0
        $(@).dialog 'close'
      }
      {text: c18n.cancel, click: ()->$(@).dialog 'close'}
    ]
  )

  addApplication = $("#addApplicationDialog").dialog(
    autoOpen: false, height: 'auto', width: 300, modal: true, position: "center",
    show:
      { effect: 'drop', direction: "up" }
    create: (event, ui)->
      $("select", @).css('width', "80px")
      $("#applicationName").change ->
        $("#version").empty()
        appBaseId = $(@).val()
        return if (!appBaseId or -1 == parseInt(appBaseId))

        url = "rest/applications/apps/#{appBaseId}"
        $.getJSON url, {}, (json)->$("#version").append(util.json2Options json).trigger "change"


    open: (event, ui)->
      productId = $("#selVersion").val()
      $.getJSON "rest/applications/base/#{productId}", {}, (json)=>
        options = util.json2Options json, false, 'name'
        if !options
          $(@).dialog 'close'
          $.msgBox i18n.dialog.addapplication.tip, null, {title: c18n.warn}
          return
        $('#applicationName', @).empty().append(options).trigger 'change'
    buttons: [
      {text: c18n.ok, click: ->
        url = 'app/add-application'
        params = {
        productId: parseInt($("#selVersion").val())
        appId: parseInt($('#version').val())
        }
        $.post url, params, (json)->
          ($.msgBox json.message, null, {title: c18n.error}; return) if json.status != 0

          # TODO: analysis here, what is this doing?
          if -1 == params.appBaseId
            params.appBaseId = json.appBaseId
          #            (require 'appmng/apptree').addNewApplicationBase(params)
          $("#applicationGridList").trigger("reloadGrid")

        $(@).dialog("close")
      }
      {text: c18n.cancel, click: -> $(@).dialog "close"}
    ]
  )

  langSettings = $('#languageSettingsDialog').dialog(
    autoOpen: false
    modal: true
    title: i18n.dialog.languagesettings.title
    width: 540
    #    create: ->$(@).dialog 'option', 'width', $('#languageSettingGrid').getGridParam('width') + 40
    open: (e, ui)->

      # param must be attached to the dialog before the dialog open
      param = $(@).data "param"
      $('#refCode').val param.langrefcode
      postData = dict: param.id, format: 'grid', prop: 'languageCode,language.name,charset.name'
      $('#languageSettingGrid').setGridParam(url: 'rest/dictLanguages', page: 1, postData: postData).trigger "reloadGrid"
    close: (event, ui)->
      (require 'appmng/langsetting_grid').saveLastEditedCell()
    buttons: [
      {text: c18n.close, click: ()->
        $(@).dialog 'close'
      }
    ]
  )

  lockLabels = (lock = true)->
    grid = $('#stringSettingsGrid')
    alert 'Hello'
    return


  stringSettings = $('#stringSettingsDialog').dialog(
    autoOpen: false
    title: i18n.dialog.stringsettings.title, modal: true
    width: 900
    create: (e, ui)->
      $('#searchText', @).keydown (e)=>$('#searchAction', @).trigger 'click' if e.which == 13

      $('#searchAction', @).attr('title', 'Search').button(text: false, icons:
        {primary: "ui-icon-search"}).click(()=>
        grid = $('#stringSettingsGrid')
        grid.getGridParam('postData').text = $('#searchText', @).val()
        grid.trigger 'reloadGrid'
      ).height(20).width(20)

    open: (e, ui)->
      stgrid.lockLabels()
      $('#searchAction', @).position(my: 'left center', at: 'right center', of: '#searchText')

      # param must be attached to the dialog before the dialog open
      param = $(@).data "param"
      return if !param

      $('#dictName', @).val(param.name)
      $('#dictVersion', @).val(param.version)
      $('#dictFormat', @).val(param.format)
      $('#dictEncoding', @).val(param.encoding)

      postData = dict: param.id, format: 'grid', prop: "key,reference,t,n,i,maxLength,context.name,description"
      $('#stringSettingsGrid').setGridParam(url: 'rest/labels', page: 1, postData: postData).trigger "reloadGrid"
    close: (event, ui)->
      postData =  $('#stringSettingsGrid').getGridParam('postData')

      $('#transSameWithRef', @).attr('checked', false)
      delete postData.nodiff

      $('#searchText', @).val("")
      delete postData.text
      (require 'appmng/stringsettings_grid').saveLastEditedCell()
    #  # resize: (event, ui)->$('#stringSettingsGrid').setGridWidth(ui.size.width - 35, true).setGridHeight(ui.size.height - 210, true)
    buttons: [
      text: c18n.close, click: ()->
        $(@).dialog 'close'
    ]
  )

  setContextTo = (context = 'Default', labelids = $('#stringSettingsGrid').getGridParam('selarrrow'))->
    # console?.log "context=#{context}, labelids =#{labelids}."
    ($.msgBox(i18n.dialog.customcontext.labeltip, null, {title: c18n.warn});return) if labelids.length == 0
    $.post 'app/update-label', {id: labelids.join(','), context: context}, (json)->
      ($.msgBox json.message, null, {title: c18n.error}; return) if json.status != 0
      $('#stringSettingsGrid').trigger 'reloadGrid'

  $('#customContext').dialog(
    autoOpen: false
    modal: true
    create: ()->
      $('#setContextMenu').menu().hide().find("li").on 'click', (e)->
        if e.target.name != 'Custom'
          setContextTo(e.target.name)
          return
        $('#customContext').dialog 'open'

      $('#setContexts').attr('privilegeName', util.urlname2Action 'app/update-label').button(
        icons:
          primary: 'ui-icon-gear'
          secondary: "ui-icon-triangle-1-n"
      ).click (e)->
        menu = $('#setContextMenu').show().width($(@).width() - 3).position(my: "right bottom", at: "right top", of: @)
        $(document).one "click", ()->menu.hide()
        false

      $('#setContextMenu').menu().hide().find("li").on 'click', (e)->
        if e.target.name != 'Custom'
          setContextTo(e.target.name)
          return
        $(@).dialog 'open'

    buttons: [
      {
      text: c18n.ok, click: ()->
        if !(context = $('#contextName', @).val())
          $('#customCtxErrorMsg').empty().html i18n.dialog.customcontext.namerequired
          return
        setContextTo(context)
        $(@).dialog 'close'
      }
      {text: c18n.cancel, click: ()->$(@).dialog 'close'}
    ]
    close: ()->

  )


  dictListPreview = $('#dictListPreviewDialog').dialog(
    autoOpen: false
    modal: true, zIndex: 900
    title: i18n.dialog.dictlistpreview.title
    create: ->$(@).dialog 'option', 'width', $('#dictListPreviewGrid').getGridParam('width') + 40
    buttons: [
      {text: i18n.dialog.dictlistpreview['import'], click: ()->
        param = dictListPreview.data "param"
        postData = handler: param.handler, app: $('#selAppVersion').val()
        ($.msgBox i18n.dialog.dictlistpreview.check, null, {title: c18n.error};return) if previewgrid.gridHasErrors()
        dictListPreview.dialog 'close'

        pb = util.genProgressBar()
        util.updateProgress('app/deliver-dict', postData, (json)->
          pb.parent().remove()
          appInfo = "#{$('#appDispAppName').text()} #{$('#selAppVersion option:selected').text()}"
          $.msgBox (i18n.dialog.dictlistpreview.success.format appInfo, json.event.msg), null, {title: c18n.info}
          $('#selAppVersion').trigger 'change'
        , pb)
      }
    ]
    open: ->
      #    param need to be initilize before the dialog open
      param = $(@).data 'param'
      return if !param

      postData =
        appId: param.appId
        format: 'grid',
        handler: param.handler
        prop: 'languageReferenceCode,base.name,version,base.format,base.encoding,labelNum,errorCount,warningCount'
      $('#dictListPreviewGrid').setGridParam(url: 'rest/delivery/dict', page: 1, postData: postData).trigger 'reloadGrid'
  )

  dictPreviewStringSettings = $('#dictPreviewStringSettingsDialog').dialog(
    autoOpen: false
    modal: true, zIndex: 920
    title: i18n.dialog.dictpreviewstringsettings.title
    create: ->$(@).dialog 'option', 'width', $('#dictPreviewStringSettingsGrid').getGridParam('width') + 40
    open: ->
      param = $(@).data 'param'
      return unless param

      $('#previewDictName', @).val(param.name)
      $('#previewDictVersion', @).val(param.version)
      $('#previewDictFormat', @).val(param.format)
      $('#previewDictEncoding', @).val(param.encoding)

      postData =
        handler: param.handler,
        dict: param.id
        format: 'grid', prop: "key,reference,maxLength,context.name,description"

      $('#dictPreviewStringSettingsGrid').setGridParam(url: 'rest/delivery/labels', page: 1, postData: postData).trigger "reloadGrid"
    close: (event, ui)->
      (require 'appmng/dictpreviewstringsettings_grid').saveLastEditedCell()
    buttons: [
      {text: c18n.close, click: ()->
        $(@).dialog 'close'
      }
    ]
  )

  dictPreviewLangSettings = $('#dictPreviewLanguageSettingsDialog').dialog(
    autoOpen: false
    modal: true, zIndex: 920
    title: i18n.dialog.languagesettings.title
    open: ->
      $(@).dialog 'option', 'width', $('#previewLanguageSettingGrid').getGridParam('width') + 40
      param = $(@).data 'param'
      return if !param

      $('#previewRefCode').val param.langrefcode
      postData = handler: param.handler, dict: param.id, format: 'grid', prop: 'languageCode,language.name,charset.name'
      $('#previewLanguageSettingGrid').setGridParam(url: 'rest/delivery/dictLanguages', page: 1, postData: postData).trigger "reloadGrid"
    close: (event, ui)->
      (require 'appmng/previewlangsetting_grid').saveLastEditedCell()
    buttons: [
      {text: c18n.close, click: ()->
        $(@).dialog 'close'
      }
    ]
  )

  addLanguage = $('#addLanguageDialog').dialog(
    autoOpen: false
    create: (event, ui)->
      $('#languageName', @).change (e)=>
        postData =
          prop: 'languageCode,charset.id'
          'language': $('#languageName', @).val()
          dict: $(@).data('param').dicts.join(',')
        # send the selected dictionary list ids, langId to server, expect language code and charset id response from server
        $.post 'rest/preferredCharset', postData, (json)=>
          $('#addLangCode', @).val json.languageCode
          $('#charset', @).val json['charset.id']

        $.getJSON 'rest/charsets', {prop: 'id,name'}, (charsets)=>$('#charset', @)
          .append("<option value='-1'>#{c18n.selecttip}</option>")
          .append(util.json2Options charsets, false, 'name')


    open: (event, ui)->
      $.getJSON 'rest/languages', {prop: 'id,name'}, (languages)=>
        $('#languageName', @)
          .append("<option value='-1'>#{c18n.selecttip}</option>")
          .append(util.json2Options languages, false, 'name').trigger 'change'

      #    get selected dictionary ids
      #    console?.log $(@).data('param').dicts
      $('#addLangCode', @).select()
      $('#charset', @).val '-1'
      $('#languageName', @).val '-1'

    buttons: [
      {text: 'Add', icons:
        {primary: "ui-icon-locked"}
      click: (e)->
        postData =
          dicts: $('#addLanguageDialog').data('param').dicts.join(',')
          languageId: $('#addLanguageDialog #languageName').val()
          charsetId: $('#addLanguageDialog #charset').val()
          code: $('#addLanguageDialog #addLangCode').val()

        #      validate postData such as code is blank
        $('#errorMsg', @).empty()
        if !postData.code || '-1' == postData.languageId || '-1' == postData.charsetId
          $('#errorMsg', @).append($("<li>#{i18n.dialog.addlanguage.coderequired}</li>")) if !postData.code
          $('#errorMsg', @).append($("<li>#{i18n.dialog.addlanguage.languagetip}</li>")) if '-1' == postData.languageId
          $('#errorMsg', @).append($("<li>#{i18n.dialog.addlanguage.charsettip}</li>")) if '-1' == postData.charsetId

          return

        $.post 'app/add-dict-language', postData, (json)=>
          ($.msgBox(json.message, null, {title: c18n.error});return) if json.status != 0
          $('#languageSettingGrid').trigger("reloadGrid") if -1 == postData.dicts.indexOf(',')
          $(@).dialog 'close'
          $.msgBox i18n.dialog.addlanguage.successtip.format $('#languageName option:selected').text(), null, {title: c18n.error}
      },
      {text: 'Cancel', click: (e)->$(@).dialog 'close'}
    ]
  )

  stringSettingsTranslation = $('#stringSettingsTranslationDialog').dialog(
    autoOpen: false, modal: true
    width: 840
    #    create: -> $(@).dialog 'option', 'width', $('#stringSettingsTranslationGrid').getGridParam('width') + 40
    open: (event, ui)->
      param = $(@).data('param')
      return unless param
      #      console?.log param
      $('#stringSettingsTranslationGrid').setGridParam(
        url: 'rest/label/translation'
        postData:
          {label: param.id, format: 'grid', status: param.status, prop: 'languageCode,language.name,translation'}
      ).setCaption(i18n.dialog.stringsettingstrans.caption.format param.key, param.ref)
        .trigger "reloadGrid"
    buttons: [
      {text: c18n.close, click: (e)->
        $(@).dialog 'close'
      }
    ]
  )

  historyDlg = $('#historyDialog').dialog(
    autoOpen: false, modal: true
    width: 845
    open: (event, ui)->
      param = $(@).data('param')
      return unless param

      $('#historyGrid').setGridParam(
        url: 'rest/dictHistory'
        postData:
          {dict: param.id, format: 'grid', status: param.status, prop: 'operationTime,operationType,task.name,operator.name'}
      ).setCaption(i18n.dialog.history.caption.format param.name).trigger "reloadGrid"

    buttons: [
      {text: c18n.close, click: (e)->
        $(@).dialog 'close'
      }
    ]
  )

  $('#addLabelDialog').dialog(
    autoOpen: false, modal: true
    width: 500
    create: ->
      @.addHandler = (me)->
        postData =$(me).data('param')
        # validation
        errMsg = []
        for val in ['key', 'reference', 'maxLength', 'context', 'description']
          postData[val] = $("##{val}", me).val()
          continue if val in ['maxLength', 'description']
          errMsg.push c18n.required.format $("label[for='#{val}']", me).text().trim()[..-2] unless $("##{val}", me).val()

        #        console?.log postData
        if errMsg.length > 0
          $('#errMsg', me).html "<hr/><ul><li>#{errMsg.join '</li><li>'}</li></ul>"
          return false


        $.post urls.label.create, postData, (json)->
          ($.msgBox json.message, null, {title: c18n.error}; return) if json.status != 0
          $('#stringSettingsGrid').trigger("reloadGrid")

        $('#errMsg', me).empty()
        $('#' + ['key', 'reference', 'maxLength', 'description'].join(', #'), me).val('')
      true
    open: ()->
      $('#errMsg', @).empty()
      $('#' + ['key', 'reference', 'maxLength', 'description'].join(', #'), @).val('')
    buttons: [
      {text: i18n.dialog.stringsettings.add, click: ->@addHandler(@)}
      {text: i18n.dialog.stringsettings.addandclose, click: ->
        $(@).dialog("close") if @addHandler(@)
      }
      {text: c18n.cancel, click: -> $(@).dialog "close"}
    ]
  )

  searchResult=$('#searchTextDialog').dialog(
    autoOpen: false, modal: true
    width: 920
    open: ->
      params = $(@).data 'params'
      node=util.getProductTreeInfo()
      typeText = if 'prod' == node.type then 'product' else 'application'

      grid = $('#searchTextGrid')

#      if 'app' == node.type
#        grid.hideCol('app')
#      else
#        grid.showCol('app')


      postData =
        format: 'grid'
        text: params.text
        prop: 'app.name,dictionary.name,key,reference,maxLength,context.name,t,n,i'
      postData[node.type] = node.id

      grid.setCaption(i18n.dialog.searchtext.caption.format params.text, typeText, node.text, params.versionText)
        .setGridParam(url: urls.labels, postData: postData).trigger 'reloadGrid'

    buttons: [
      {text: c18n.close, click: -> $(@).dialog "close"}
    ]
  )

  showSearchResult = (params)->searchResult.data('params', params).dialog 'open'


  addLanguage: addLanguage
  dictPreviewLangSettings: dictPreviewLangSettings
  dictPreviewStringSettings: dictPreviewStringSettings
  dictListPreview: dictListPreview
  stringSettings: stringSettings
  newProductVersion: newProductVersion
  newAppVersion: newAppVersion
  addApplication: addApplication
  langSettings: langSettings
  stringSettingsTranslation: stringSettingsTranslation
  historyDlg: historyDlg

  showSearchResult: showSearchResult

