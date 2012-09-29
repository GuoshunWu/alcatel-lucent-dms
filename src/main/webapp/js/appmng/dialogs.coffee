define ['jqueryui','jqmsgbox','i18n!nls/common','i18n!nls/appmng','require'], ($,jqmsgbox,c18n,i18n,require)->
  ids = {
  button:
    {
    new_product: 'newProduct'
    new_release: 'newVersion'
    }
  dialog:
    {
    new_product: 'newProductDialog',
    new_product_release: 'newProductReleaseDialog',
    new_or_add_application: 'newOrAddApplicationDialog'
    }
  productName: '#productName'
  product_duplication: '#dupVersion'
  }

  # Create new product dialog
  newProduct = $("##{ids.dialog.new_product}").dialog {
  autoOpen: false, height: 200, width: 400, modal: true,
  buttons: [
    {text: c18n.ok, click: ->
    # TODO: validate the product name...
      $.post 'app/create-product', {name: $(ids.productName).val()}, (json)->
        if (json.status != 0)
          $.msgBox json.message, null, {title: c18n.error, width: 300, height: 'auto'}
          return false
        appptree.addNewProductBase {name: $(ids.productName).val(), id: json.id}
      $(@).dialog "close"
    }
    {text: c18n.cancel, click: -> $(@).dialog "close"}
  ]
  }
  # create new product button below the tree
  $("##{ids.button.new_product}").button().click (e) =>
    newProduct.dialog("open")
  #  Create new product release dialog
  newProductRelease = $("##{ids.dialog.new_product_release}").dialog {
  autoOpen: false
  height: 200
  width: 500
  modal: true
  buttons: [
    {text: c18n.ok, click: ->
      url = 'app/create-product-release'
      versionName = $('#versionName').val()
      dupVersionId = $("#dupVersion").val()
      productBaseId = (require 'appmng/apptree').getSelected().id
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
    $(ids.product_duplication).append new Option '', -1
    (require 'appmng/product_panel').getProductSelectOptions().appendTo $ ids.product_duplication
  }

  $("##{ids.button.new_release}").button({text: false, icons: {primary: "ui-icon-plus"}}).click (e) =>
    newProductRelease.dialog("open")

  # Create new application or add application to product dialog
  newOrAddApplication = $("##{ids.dialog.new_or_add_application}").dialog {
  autoOpen: false, height: 200, width: 400, modal: true, position: "center",
  show: { effect: 'drop', direction: "up" }
  create: (event, ui)->
    input = $('<input>').insertAfter($('#applicationName')).hide()
    $('#applicationName').data('myinput', input)

    input = $('<input>').insertAfter($("#version")).hide()
    $('#version').data('myinput', input)

    $("select", @).css('width', "80px")

    $("#applicationName").change ->
      $("#version").empty().append(new Option('new', -1))
      appBaseId = $(@).val()
      if (-1 == parseInt(appBaseId))
        $(@).data('myinput').val("").show()
        $("#version").trigger("change")
        return
      $(@).data('myinput').hide()

      url = "rest/applications/apps/#{appBaseId}"
      $.getJSON url, {}, (json)->$("#version").append($(json).map ->new Option(@version, @id)).trigger "change"

    $("#version").change ->
      appId = $(@).val()
      if -1 == parseInt(appId)
        $(@).data('myinput').val("").show()
        return
      $(@).data('myinput').hide()
  open: (event, ui)->
    productId = $("#selVersion").val()
    url = "rest/applications/base/#{productId}"
    $.getJSON url, {}, (json)->
      appBasesOptions = $("#newOrAddApplicationDialog").find("#applicationName").empty().append(new Option('new', -1))
      appBasesOptions.append($(json).map ->new Option(@name, @id)).trigger 'change'
  buttons: [
    {text: c18n.ok, click: ->
      url = 'app/create-or-add-application'
      params = {
      productId: parseInt($("#selVersion").val())
      appBaseId: parseInt($('#applicationName').val())
      appId: parseInt($('#version').val())
      appBaseName: $('#applicationName').data('myinput').val()
      appVersion: $('#version').data('myinput').val()
      }
      $.post url, params, (json)->
        ($.msgBox json.message, null, {title: c18n.error}; return) if json.status != 0
        (require 'appmng/apptree').addNewApplicationBase(params) if -1 == params.appBaseId
        $("#applicationGridList").trigger("reloadGrid")
      $(@).dialog("close")
    }
    {text: c18n.cancel, click: -> $(@).dialog "close"}
  ]
  }

  langSettings = $('#languageSettingsDialog').dialog {
  autoOpen: false
  width: 'auto'
  height: 'auto'
  title: i18n.dialog.languagesettings.title
  open: (e, ui)->
  # param must be attached to the dialog before the dialog open
    param = $(@).data "param"
    $('#refCode').val param.refCode
    $('#languageSettingGrid').setGridParam({url: '/rest/dictLanguages', postData: {dict: param.dictId, format: 'grid', prop: 'language.name,languageCode,charset.name'}}).trigger "reloadGrid"
  }

  stringSettings = $('#stringSettingsDialog').dialog {
  autoOpen: false
  width: 'auto'
  height: 'auto'
  title: i18n.dialog.stringsettings.title
  open: (e, ui)->
  # param must be attached to the dialog before the dialog open
    dict = $(@).data "param"
    return if !dict
    console.log dict

    $('#dictName').val(dict.name)
    $('#dictVersion').val(dict.version)
    $('#dictFormat').val(dict.format)
    $('#dictEncoding').val(dict.encoding)

    prop = "key,reference,maxLength,context.name,description"

    $('#stringSettingsGrid').setGridParam({url: '/rest/labels', postData: {dict: dict.id, format: 'grid', prop: prop}}).trigger "reloadGrid"
  }

  stringSettings: stringSettings
  newProduct: newProduct
  newProductRelease: newProductRelease
  newOrAddApplication: newOrAddApplication
  langSettings: langSettings