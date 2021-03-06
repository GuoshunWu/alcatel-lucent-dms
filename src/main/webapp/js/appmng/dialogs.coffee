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
  'appmng/report_chart'

  'appmng/dictpreviewstringsettings_grid'
  'appmng/previewlangsetting_grid'
  'appmng/searchtext_grid'
  'appmng/stringsettings_translation_history_grid'

  'appmng/dict_validation_grid'

], ($, jqgrid, blockui, msgbox, c18n, i18n, urls, util, previewgrid, stgrid, chart, shistorygrid)->

  #  console?.log "module appmng/dialogs loading."
  dialogDefaultOptions = $.ui.dialog.prototype.options


  $('#XMLPropertiesDictionaryExportOptionsDialog').dialog(
#    autoOpen: true
    width: 450, height: 200
    buttons: [
      {text: c18n.ok, click: ->
        $(@).data('param').resolve(
          escape: $('#XMLPropertiesDictEscapeApostrophe', @).prop('checked')
          convert: $('#XMLPropertiesDictApostropheConvert', @).prop('checked')
        )
        $(@).dialog "close"
      }
#      {text: c18n.cancel, click: -> $(@).dialog "close"}
    ]
  )

  capitalizationDialog = $('#capitalizationDialog').dialog(
    width: 1100, height: 'auto'
    show: { effect: 'slide', direction: "up" }

    open: (e, ui) ->
      params = $(@).data 'params'
      $.getJSON urls.languages, {prop: 'id,name', dict: params.dicts}, (languages)=>
        langTable = util.generateLanguageTable languages
        $(@).empty().append(langTable)

    buttons: [
      {text: c18n.ok, click: (e)->
        languages =($("input:checkbox[name='languages']:checked", @).map (index, element)->element.id).get()
        params = $(@).data 'params'
        postData =
          lang: languages.join(",")
          style: params.style
          dict: params.dicts
          label: params.labels

        # language id list, empty for reference only
        delete postData.lang if(!postData.lang)

        $.blockUI(message: '')
        pb = util.genProgressBar()
        util.updateProgress(urls.app.capitalize, postData, (json)->
          $.unblockUI()
          pb.parent().remove()
          msg = json.event.msg
          $.msgBox msg, null, {title: c18n.info, width: 300, height: 'auto'}
          window.param.dirty = true
          $('#stringSettingsGrid').trigger 'reloadGrid' if postData.label
        , pb)

#        $.post urls.app.capitalize, postData, (json)->
#          ($.msgBox json.message, null, {title: c18n.error, width: 300, height: 'auto'};return) unless json.status == 0
#          $('#stringSettingsGrid').trigger 'reloadGrid' if postData.label
#          $.msgBox json.message, null, {title: c18n.info, width: 300, height: 'auto'}

        $(@).dialog "close"
      }
      {text: c18n.cancel, click: (e)->$(@).dialog "close"}
    ]
  )

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

          if -1 == params.appBaseId
            params.appBaseId = json.appBaseId
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
    width: 910,
    height: 630
    create: (e, ui)->
      # create search text component
      $('#searchText', @).keydown (e)=>
        return true if e.which != 13
        $('#searchAction', @).trigger 'click'
        false

      $('#searchAction', @).attr('title', 'Search').button(text: false, icons:
        {primary: "ui-icon-search"}).click(()=>
        grid = $('#stringSettingsGrid')
        grid.getGridParam('postData').text = $('#searchText', @).val()
        grid.trigger 'reloadGrid'
      ).height(20).width(20)

      # create set translation status component
      $('#makeStringSettingsLabelTranslateStatus').button(
        icons:
          primary: "ui-icon-triangle-1-n"
          secondary: "ui-icon-gear"
      )
      .attr('privilegeName', util.urlname2Action urls.label.update_status)
      .click (e)->
        menu = $('#stringSettingsTranslationStatus').show().width($(@).width()).position(my: "left bottom", at: "left top", of: @)
        $(document).one "click", ()->menu.hide()
        false

      $('#stringSettingsTranslationStatus').menu().hide().find("li").on 'click', (e)->
        grid = $("#stringSettingsGrid")
        ids = grid.getGridParam('selarrrow').join(",")
        ($.msgBox (c18n.selrow.format c18n.label), null, title: c18n.warning; return) unless ids
        $.post urls.label.update_status, {type: 'trans', transStatus: e.target.name, id: ids}, (json)->
          ($.msgBox json.message, null, title: c18n.warning; return) unless json.status == 0
          grid.trigger 'reloadGrid'
          window.param.dirty = true
      capitalizeId = '#stringCapitalize'

      menu = $(capitalizeId + 'Menu').hide().menu(
        select: ( event, ui )->
          grid = $("#stringSettingsGrid")
          ids = grid.getGridParam('selarrrow').join(",")
          ($.msgBox (c18n.selrow.format c18n.label), null, title: c18n.warning; return) unless ids

          $('#capitalizationDialog').data(params: {
            labels: ids,
            style: $('a',ui.item).prop('name')
            dicts: stringSettings.data("param").id
          }).dialog('open')
      )

      $(capitalizeId).button(
        create:(event, ui)->
        icons: {
          primary: "ui-icon-triangle-1-n"
        }
      ).on('click', (e)->
        menu.width($(@).width() - 3).show().position(my: "left bottom", at: "left top", of: this)
        $(document).one("click", ()->menu.hide())
        false
      )

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
      #  used for restore filter
      $("select[id='gs_context'][name='context.name']", '#stringSettingsDialog').val('')
      $('#stringSettingsGrid').setGridParam(url: 'rest/labels', page: 1, postData: postData)[0].triggerToolbar()
    close: (event, ui)->
      postData =  $('#stringSettingsGrid').getGridParam('postData')

      $('#transSameWithRef', @).attr('checked', false)
      delete postData.nodiff

      $('#searchText', @).val("")
      delete postData.text
    #  # resize: (event, ui)->$('#stringSettingsGrid').setGridWidth(ui.size.width - 35, true).setGridHeight(ui.size.height - 210, true)
    buttons: [
      text: c18n.close, click: ()->
        $(@).dialog 'close'
    ]
  )

  setContextTo = (context = 'Default', labelids = $('#stringSettingsGrid').getGridParam('selarrrow'))->
    # console?.log "context=#{context}, labelids =#{labelids}."
#    console.log "About to post %s, postData=%o", urls.label.update, {id: labelids.join(','), context: context}
#    return
    $.post urls.label.update, {id: labelids.join(','), context: context}, (json)->
      ($.msgBox json.message, null, {title: c18n.error}; return) if json.status != 0
      window.param.dirty = true
      $('#stringSettingsGrid').trigger 'reloadGrid'

  $('#customContext').dialog(
    autoOpen: false
    modal: true
    create: ()->
      $('#setContextMenu').menu().hide().find("li").on 'click', (e)=>
        labelids = $('#stringSettingsGrid').getGridParam('selarrrow')
        ($.msgBox(i18n.dialog.customcontext.labeltip, null, {title: c18n.warn});return) if labelids.length == 0
        (setContextTo(e.target.name, labelids);return) if e.target.name != 'Custom'
        $(@).dialog 'open'

      $('#setContexts').attr('privilegeName', util.urlname2Action urls.label.update).button(
        icons:
          primary: 'ui-icon-gear'
          secondary: "ui-icon-triangle-1-n"
      ).click (e)->
        menu = $('#setContextMenu').show().width($(@).width() - 3).position(my: "right bottom", at: "right top", of: @)
        $(document).one "click", ()->menu.hide()
        false

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
        isAutoCreateLanguage= $('#isAutoCreateLanguage', @).prop("checked")
        postData = handler: param.handler, app: $('#selAppVersion').val(), autoCreateLang: isAutoCreateLanguage
        ($.msgBox(i18n.dialog.dictlistpreview.check, null, {title: c18n.error});return) if previewgrid.gridHasErrors()
        dictListPreview.dialog 'close'

        pb = util.genProgressBar()
        $.blockUI(message: '')
        util.updateProgress(urls.app.deliver_dict, postData, (json)->
          $.unblockUI()
          pb.parent().remove()
          retJson = $.parseJSON(json.event.msg)
          $('#importReportDialog').data('params', retJson).dialog 'open'
          $('#selAppVersion').trigger 'change'
        , pb)
      }
    ]
    open: ->
      $('#isAutoCreateLanguage', @).attr("checked", true)
      #    param need to be initilize before the dialog open
      param = $(@).data 'param'
      return if !param

      postData =
        appId: param.appId
        handler: param.handler
      $('#dictListPreviewGrid').setGridParam(page: 1, postData: postData).trigger 'reloadGrid'
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
    buttons: [
      {text: c18n.close, click: ()->
        $(@).dialog 'close'
      }
    ]
  )

  addLanguage = $('#addLanguageDialog').dialog(
    autoOpen: false
    create: (event, ui)->
      # fill in charset
      $.getJSON urls.charsets, {prop: 'id,name'}, (charsets)=>
        $('#charset', @)
        .append("<option value='-1'>#{c18n.selecttip}</option>")
        .append(util.json2Options charsets, false, 'name')

      $('#languageName', @).change (e)=>
        postData =
          prop: 'languageCode,charset.id'
          'language': $('#languageName', @).val()
          dict: $(@).data('param').dicts.join(',')
        # send the selected dictionary list ids, langId to server, expect language code and charset id response from server
        $.post 'rest/preferredCharset', postData, (json)=>
#         get current selected dictionary row
          dictRowData = $('#languageSettingsDialog').data('param')
          languageCode = json.languageCode
          return unless languageCode
#          console.log "dictRowData=%o, languageCode=%o", dictRowData, languageCode
          if 'XML android' == dictRowData.format
            sep = '-'
#            console.log "language code=%o", languageCode
            index = (languageCode.lastIndexOf sep)+1
            languageCode = languageCode.substring(0 , index) + 'r' + languageCode.substring(index) if index > 0

          $('#addLangCode', @).val languageCode
          $('#charset', @).val json['charset.id']

    open: (event, ui)->
      $('#addLangCode', @).val ''
      $.getJSON urls.languages, {prop: 'id,name'}, (languages)=>
        $('#languageName', @).empty()
          .append("<option value='-1'>#{c18n.selecttip}</option>")
          .append(util.json2Options languages, false, 'name').trigger 'change'

      #    get selected dictionary ids
      #    console?.log $(@).data('param').dicts
      $('#addLangCode', @).select()
      $('#charset', @).val '-1'
      $('#languageName', @).val '-1'

    buttons: [
      {text: c18n.add, icons: {primary: "ui-icon-locked"}, click: (e)->
        postData =
          dicts: $(@).data('param').dicts.join(',')
          languageId: $('#languageName', @).val()
          charsetId: $('#charset',@).val()
          code: $('#addLangCode', @).val()

        #      validate postData such as code is blank
        $('#errorMsg', @).empty()
        if !postData.code || '-1' == postData.languageId || '-1' == postData.charsetId
          $('#errorMsg', @).append($("<li>#{i18n.dialog.addlanguage.coderequired}</li>")) if !postData.code
          $('#errorMsg', @).append($("<li>#{i18n.dialog.addlanguage.languagetip}</li>")) if '-1' == postData.languageId
          $('#errorMsg', @).append($("<li>#{i18n.dialog.addlanguage.charsettip}</li>")) if '-1' == postData.charsetId

          return
        $(@).dialog 'close'

        $.blockUI()
        $.post urls.app.add_dict_language, postData, (json)=>
          $.unblockUI()
          ($.msgBox(json.message, null, {title: c18n.error});return) if json.status != 0
          $('#languageSettingGrid').trigger("reloadGrid") if -1 == postData.dicts.indexOf(',')
          window.param.dirty = true
          $.msgBox i18n.dialog.addlanguage.successtip.format $('#languageName option:selected').text(), null, {title: c18n.info}
      },
      {text: c18n.cancel, click: (e)->$(@).dialog 'close'}
    ]
  )

  removeLanguage = $('#removeLanguageDialog').dialog(
    autoOpen: false
    create: (event, ui)->
    open: (e, ui)->$('#removeLanguageDialog_errorMsg', @).empty()
    buttons: [
      {text: c18n['del'], icons:{primary: "ui-icon-locked"}, click: (e)->

        postData =
          dicts: $(@).data('param').dicts.join(',')
          code: $('#removeLanguageDialog_addLangCode', @).val()

        #      validate postData such as code is blank
        $('#removeLanguageDialog_errorMsg', @).empty()
        if !postData.code
          $('#removeLanguageDialog_errorMsg', @).append($("<li>#{i18n.dialog.addlanguage.coderequired}</li>")) if !postData.code
          return
        $.blockUI()
#        $(@).parent().block(message: '<img src="images/busy.gif" />&nbsp;Please wait...')
        $.post urls.app.remove_dict_language, postData, (json)=>
          $.unblockUI()
#          $(@).parent().unblock()
          ($.msgBox(json.message, null, {title: c18n.error});return) if json.status != 0
          $('#languageSettingGrid').trigger("reloadGrid") if -1 == postData.dicts.indexOf(',')
          $.msgBox json.message, null, title: c18n.info
          $(@).dialog 'close'

      },
      {text: c18n.cancel, click: (e)->$(@).dialog 'close'}
    ]
  )

  stringSettingsTranslation = $('#stringSettingsTranslationDialog').dialog(
    autoOpen: false, modal: true
    width: 840
    create: ->
#      $(@).dialog 'option', 'width', $('#stringSettingsTranslationGrid').getGridParam('width') + 40
      $('#labelReferenceId', @).attr('privilegeName', util.urlname2Action urls.label.update_ref_translations)
      $(@).next('div.ui-dialog-buttonpane').find("button.ui-button:contains(#{c18n.save})")
      .attr('privilegeName', util.urlname2Action urls.label.update_ref_translations)

    open: (event, ui)->
      $(@).data('saved', false)
      param = $(@).data('param')
      return unless param
#      console?.log param
      $('#labelReferenceId',@).val(param.ref)
      $('#stringSettingsTranslationGrid').setGridParam(
        page: 1
        postData:  {label: param.id, status: param.status }
      ).setCaption(i18n.dialog.stringsettingstrans.caption.format $.jgrid.htmlEncode(param.key))
        .trigger "reloadGrid"
    buttons: [
      {text: c18n.save, attr: {id: 'saveLabelAndTranslations'}, click: (e)->
        me = $(@)
        grid = $('#stringSettingsTranslationGrid')
        editedCell = grid.data("editedCell")
        labelParam = $(@).data('param')
        grid.jqGrid("saveCell", editedCell.iRow, editedCell.iCol) unless($.isEmptyObject(editedCell))

        postData =
          id: labelParam.id  # label id
          reference: $('#labelReferenceId',@).val()
        $.each grid.jqGrid('getChangedCells'), (index, row)-> postData["newTranslations[#{row['language.id']}]"] = row["ct.translation"]

#        console.log "postData=%o", postData
        blockMe = $(@).parent().block()
        $.post(urls.label.update_ref_translations, postData).done((json)->
          blockMe.unblock()
          ($.msgBox(json.message, null, {title: c18n.error});return) if json.status != 0
#          console.log "json=%o", json
          me.data('saved', true)
          $('#stringSettingsGrid').trigger 'reloadGrid'
          me.dialog 'close'
        )


      }
      {text: c18n.cancel, click: (e)->
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
        page: 1
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
          window.param.dirty = true

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

      postData = grid.getGridParam('postData')
      postData.fuzzy = params.fuzzy
      postData.format = 'grid'
      postData.text = params.text
      postData.prop = 'dictionary.base.applicationBase.name,dictionary.base.name,key,reference,maxLength,context.name,t,n,i'

      delete postData.app
      delete postData.prod

      postData[node.type] = params.version.id

      grid.setCaption(i18n.dialog.searchtext.caption.format params.text, typeText, node.text, params.version.text)
        .setGridParam(url: (if postData.fuzzy then urls.labels else urls.labels_normal), page: 1).trigger 'reloadGrid'

    buttons: [
      {text: c18n.close, click: -> $(@).dialog "close"}
    ]
  )

  importReport = $('#importReportDialog').dialog(
    autoOpen: false
    width: 600, modal: true

    open: ()->
      msg = """
            {
            "dictNum": 5,
            "labelNum": 247,
            "translationNum": 5435,
            "translationWC": 34141,
            "distinctTranslationNum": 4503,
            "distinctTranslationWC": 30813,
            "untranslatedNum": 299,
            "untranslatedWC": 1301,
            "translatedNum": 4204,
            "translatedWC": 29512,
            "matchedNum": 391,
            "matchedWC": 2656
            }
            """
      json = $.parseJSON(msg)
      json = $(@).data 'params'

      appInfo = "#{$('#appDispAppName').text()} #{$('#selAppVersion option:selected').text()}".trim()
      appInfo = 'Demo 1.0' if !appInfo

      statisticsTabId = '#importReportStatistics'
#      console?.log "original data"
#      console?.log json

      # number ajdust
      json.translatedNum -= json.matchedNum
      json.translatedWC -= json.matchedWC

      json.untranslatedNum +=json.matchedNum
      json.untranslatedWC +=json.matchedWC

#      console?.log "after adjust data:"
#      console?.log json


      $('#dupTrans', statisticsTabId).html(json.translationNum - json.distinctTranslationNum)
        .parent().next().children('span').html("#{json.translationWC- json.distinctTranslationWC}")
      $('#distinctTrans1', statisticsTabId).html(json.distinctTranslationNum)
        .parent().next().children('span').html("#{json.distinctTranslationWC}")
      $('#totalTrans', statisticsTabId).html(json.translationNum)
        .parent().next().children('span').html("#{json.translationWC}")
      $('#dupRatio', statisticsTabId).html(((1 - json.distinctTranslationNum/json.translationNum)*100).toFixed(2) + '%')
        .parent().next().children('span').html("#{((1 - json.distinctTranslationWC/json.translationWC)*100).toFixed(2)}%")

      $('#distinctTrans2', statisticsTabId).html(json.distinctTranslationNum)
        .parent().next().children('span').html("#{json.distinctTranslationWC}")
      $('#translated', statisticsTabId).html(json.translatedNum)
        .parent().next().children('span').html("#{json.translatedWC}")
      $('#untranslated', statisticsTabId).html(json.untranslatedNum)
        .parent().next().children('span').html("#{json.untranslatedWC}")
      $('#transRatio', statisticsTabId).html((json.translatedNum/json.distinctTranslationNum*100).toFixed(2) + '%')
        .parent().next().children('span').html("#{(json.translatedWC/json.distinctTranslationWC*100).toFixed(2)}%")

      $('#untranslated1', statisticsTabId).html(json.untranslatedNum)
        .parent().next().children('span').html("#{json.untranslatedWC}")
      $('#autoTrans', statisticsTabId).html(json.matchedNum)
        .parent().next().children('span').html("#{json.matchedWC}")
      $('#noMatch', statisticsTabId).html(json.untranslatedNum - json.matchedNum)
        .parent().next().children('span').html("#{json.untranslatedWC - json.matchedWC}")
      $('#autoRatio', statisticsTabId).html((json.matchedNum/json.untranslatedNum*100).toFixed(2)+ '%')
        .parent().next().children('span').html("#{(json.matchedWC/json.untranslatedWC*100).toFixed(2)}%")

      appInfo = "#{$('#appDispAppName').text()} #{$('#selAppVersion option:selected').text()}".trim()
      appInfo = 'Demo 1.0' if !appInfo
      title = i18n.dialog.dictlistpreview.success.format json.labelNum, json.dictNum, appInfo

      $('#title', @).html(title)
      chart.showChart(title, json)

    buttons: [
      {text: c18n.ok, click: -> $(@).dialog "close"}
    ]
  )

  stringSettingsTransHistoryDialog = $('#stringSettingsTranslationHistoryDialog').dialog(
    autoOpen: false, modal: true
    width: 845
    open: (event, ui)->
      param = $(@).data('param')
      return unless param
      stringSettingsTransGridCaption  = $('#stringSettingsTranslationGrid').getGridParam('caption')
      param.reference = $('#labelReferenceId', '#stringSettingsTranslationDialog').val()
#      console.log param
      $('#stringSettingsTranslationHistoryGrid').setGridParam(
        url: urls.translation_histories
        page: 1
        postData: {transId: param['ct.id'], format: 'grid', prop: 'operationTime,operationType,operator.name,translation,status,memo'}
      ).setCaption(c18n.history.caption.format param.reference).trigger "reloadGrid"

    buttons: [
      {text: c18n.close, click: (e)->
        $(@).dialog 'close'
      }
    ]
  )

  dictValidationDialog = $('#dictValidationDialog').dialog(
    open: ()->
      me = $(@)
      grid = $("table.ui-jqgrid-btable", @)
      util.adjustDialogAndInnerGridSize(me, grid, {width: 200, height: 100}, {width: 37, height: 180})
      param = $(@).data('param')
      grid.setCaption("Dictionary #{param.name} version #{param.version} #{param.type}(s)")
      .setGridParam(postData: {dict: param.id, type: param.type})
      .trigger 'reloadGrid'


    buttons: $.merge([
      {text: c18n.ok, click: (e)->
        $(@).dialog 'close'
      }
    ] ,dialogDefaultOptions.buttons)
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

