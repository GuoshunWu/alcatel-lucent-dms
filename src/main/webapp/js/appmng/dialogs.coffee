define ['require', 'appmng/dictlistpreview_grid', 'appmng/dictpreviewstringsettings_grid', 'appmng/previewlangsetting_grid'], (require, grid, sgrid, lgrid)->
  $ = require 'jqueryui'
  c18n = require 'i18n!nls/common'
  i18n = require 'i18n!nls/appmng'
  require 'blockui'
  require 'jqmsgbox'

  ids = {
  button:
    {
    new_product: 'newProduct'
    }
  dialog:
    {
    new_product: 'newProductDialog',
    new_product_release: 'newProductReleaseDialog',
    new_or_add_application: 'addApplicationDialog'
    }
  productName: '#productName'
  product_duplication: '#dupVersion'
  }

  #  Create new product release dialog
  newProductVersion = $("##{ids.dialog.new_product_release}").dialog {
  autoOpen: false
  height: 200, width: 500, modal: true
  buttons: [
    {text: c18n.ok, click: ->
      url = 'app/create-product-release'
      versionName = $('#versionName').val()
      dupVersionId = $("#dupVersion").val()
      tree = require 'appmng/navigatetree'
      productBaseId = tree.getNodeInfo().id

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
    $(ids.product_duplication).empty().append new Option '', -1
    (require 'appmng/product_panel').getProductSelectOptions().appendTo $ ids.product_duplication
  close: (event, ui)->
    errDiv = $("#productErrInfo").hide()
  }

  #  Create new application release dialog
  newAppVersion = $("#newApplicationVersionDialog").dialog {
  autoOpen: false
  height: 200, width: 500, modal: true
  buttons: [
    {text: c18n.ok, click: ->
      url = 'app/create-application'
      versionName = $('#appVersionName').val()
      dupVersionId = $("#dupDictsVersion").val()
      appBaseId = (require 'appmng/navigatetree').getNodeInfo().id

      if !versionName
        $("#appErrInfo").show()
        return

      $.post url, {version: versionName, dupVersionId: dupVersionId, id: appBaseId}, (json)->
        if (json.status != 0)
          $.msgBox json.message, null, {title: c18n.error, width: 300, height: 'auto'}
          return
        (require 'appmng/application_panel').addNewApplication {version: versionName, id: json.id}
      $(@).dialog "close"
    }
    {text: c18n.cancel, click: -> $(@).dialog "close"}
  ]
  open: (event, ui)->
    $("#dupDictsVersion").empty().append(new Option '', -1).append (require 'appmng/application_panel').getApplicationSelectOptions()
  close: (event, ui)->
    $("#appErrInfo").hide()
  }

  # Add application to product dialog
  addApplication = $("##{ids.dialog.new_or_add_application}").dialog {
  autoOpen: false, height: 'auto', width: 'auto', modal: true, position: "center",
  show: { effect: 'drop', direction: "up" }
  create: (event, ui)->
    $("select", @).css('width', "80px")

    $("#applicationName").change ->
      $("#version").empty()
      appBaseId = $(@).val()
      return if (-1 == parseInt(appBaseId))

      url = "rest/applications/apps/#{appBaseId}"
      $.getJSON url, {}, (json)->$("#version").append($(json).map ->new Option(@version, @id)).trigger "change"


  open: (event, ui)->
    productId = $("#selVersion").val()
    console.log productId
    url = "rest/applications/base/#{productId}"
    $.getJSON url, {}, (json)=>$('#applicationName', @).empty().append($(json).map ->new Option @name, @id).trigger 'change'
  buttons: [
    {text: c18n.ok, click: ->
      url = 'app/add-application'
      params = {
      productId: parseInt($("#selVersion").val())
      #      appBaseId: parseInt($('#applicationName').val())
      appId: parseInt($('#version').val())
      #      appBaseName: $('#applicationName').data('myinput').val()
      #      appVersion: $('#version').data('myinput').val()
      }
      $.post url, params, (json)->
        ($.msgBox json.message, null, {title: c18n.error}; return) if json.status != 0
        #        todo:Bug here, appBaseId should be -1, it is used in addNewApplicationBase
        if -1 == params.appBaseId
          params.appBaseId = json.appBaseId
          (require 'appmng/apptree').addNewApplicationBase(params)
        $("#applicationGridList").trigger("reloadGrid")

      $(@).dialog("close")
    }
    {text: c18n.cancel, click: -> $(@).dialog "close"}
  ]
  }

  langSettings = $('#languageSettingsDialog').dialog {
  autoOpen: false
  modal: true, zIndex: 900
  width: 'auto', height: 'auto', title: i18n.dialog.languagesettings.title

  open: (e, ui)->
  # param must be attached to the dialog before the dialog open
    param = $(@).data "param"
    $('#refCode').val param.langrefcode
    postData = dict: param.id, format: 'grid', prop: 'languageCode,language.name,charset.name'
    $('#languageSettingGrid').setGridParam(url: 'rest/dictLanguages', postData: postData).trigger "reloadGrid"
  close: (event, ui)->
    (require 'appmng/langsetting_grid').saveLastEditedCell()
  buttons: [
    {text: c18n.close, click: ()->
      $(@).dialog 'close'
    }
  ]

  }

  stringSettings = $('#stringSettingsDialog').dialog {
  autoOpen: false
  width: 'auto', height: 'auto', title: i18n.dialog.stringsettings.title
  modal: true, zIndex: 900
  open: (e, ui)->
  # param must be attached to the dialog before the dialog open
    param = $(@).data "param"
    return if !param

    $('#dictName', @).val(param.name)
    $('#dictVersion', @).val(param.version)
    $('#dictFormat', @).val(param.format)
    $('#dictEncoding', @).val(param.encoding)

    postData = dict: param.id, format: 'grid', prop: "key,reference,maxLength,context.name,description"
    $('#stringSettingsGrid').setGridParam(url: 'rest/labels', postData: postData).trigger "reloadGrid"
  close: (event, ui)->
    (require 'appmng/stringsettings_grid').saveLastEditedCell()
  buttons: [
    {text: c18n.close, click: ()->
      $(@).dialog 'close'
    }
  ]
  }

  dictListPreview = $('#dictListPreviewDialog').dialog {
  autoOpen: false
  modal: true, zIndex: 900
  width: 'auto', height: 'auto', title: i18n.dialog.dictlistpreview.title
  buttons: [
    {text: i18n.dialog.dictlistpreview.import, click: ()->
      param = dictListPreview.data "param"
      postData = handler: param.handler, app: $('#selAppVersion').val()
      ($.msgBox i18n.dialog.dictlistpreview.check, null, {title: c18n.error};return) if grid.gridHasErrors()
      dictListPreview.dialog 'close'

      $.blockUI()
      $.post 'app/deliver-dict', postData, (json)->
        $.unblockUI()
        (return;$.msgBox json.message, null, {title: c18n.error}) if(json.status != 0)
        appInfo = "#{$('#appDispAppName').text()} #{$('#selAppVersion option:selected').text()}"

        $.msgBox i18n.dialog.dictlistpreview.success.format appInfo, null, {title: c18n.info}

        $('#selAppVersion').trigger 'change'
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
    $('#dictListPreviewGrid').setGridParam(url: 'rest/delivery/dict', postData: postData).trigger 'reloadGrid'
  }


  dictPreviewStringSettings = $('#dictPreviewStringSettingsDialog').dialog {
  autoOpen: false
  modal: true, zIndex: 920
  width: 'auto', height: 'auto', title: i18n.dialog.dictpreviewstringsettings.title
  open: ->
    param = $(@).data 'param'
    return if !param

    $('#previewDictName', @).val(param.name)
    $('#previewDictVersion', @).val(param.version)
    $('#previewDictFormat', @).val(param.format)
    $('#previewDictEncoding', @).val(param.encoding)

    postData =
      handler: param.handler,
      dict: param.id
      format: 'grid', prop: "key,reference,maxLength,context.name,description"

    $('#dictPreviewStringSettingsGrid').setGridParam(url: 'rest/delivery/labels', postData: postData).trigger "reloadGrid"
  close: (event, ui)->
    (require 'appmng/dictpreviewstringsettings_grid').saveLastEditedCell()
  buttons: [
    {text: c18n.close, click: ()->
      $(@).dialog 'close'
    }
  ]
  }

  dictPreviewLangSettings = $('#dictPreviewLanguageSettingsDialog').dialog {
  autoOpen: false
  modal: true, zIndex: 920
  width: 'auto', height: 'auto', title: i18n.dialog.languagesettings.title
  open: ->
    param = $(@).data 'param'
    return if !param

    $('#previewRefCode').val param.langrefcode
    postData = handler: param.handler, dict: param.id, format: 'grid', prop: 'languageCode,language.name,charset.name'
    $('#previewLanguageSettingGrid').setGridParam(url: 'rest/delivery/dictLanguages', postData: postData).trigger "reloadGrid"
  close: (event, ui)->
    (require 'appmng/previewlangsetting_grid').saveLastEditedCell()
  buttons: [
    {text: c18n.close, click: ()->
      $(@).dialog 'close'
    }
  ]
  }

  dictPreviewLangSettings: dictPreviewLangSettings
  dictPreviewStringSettings: dictPreviewStringSettings
  dictListPreview: dictListPreview
  stringSettings: stringSettings
  newProductVersion: newProductVersion
  newAppVersion: newAppVersion
  addApplication: addApplication
  langSettings: langSettings