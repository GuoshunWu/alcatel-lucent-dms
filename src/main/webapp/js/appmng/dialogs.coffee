define ['jqueryui', 'jqmsgbox', 'i18n!nls/common'], ($, msgbox, c18n)->
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
        (require 'appmng/apptree' ).addNewProductBase {name: $(ids.productName).val(), id: json.id}
      $(this).dialog "close"
    }
    {text: c18n.cancel, click: -> $(this).dialog "close"}
  ]
  }
  # TODO: implement the rest of the dialogs
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
      $(this).dialog "close"
    }
    {text: c18n.cancel, click: -> $(this).dialog "close"}
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

    $("select", this).css('width', "80px")

    $("#applicationName").change ->
      $("#version").empty().append(new Option('new', -1))
      appBaseId = $(this).val()
      if (-1 == parseInt(appBaseId))
        $(this).data('myinput').val("").show()
        $("#version").trigger("change")
        return
      $(this).data('myinput').hide()

      url = "rest/applications/apps/#{appBaseId}"
      $.getJSON url, {}, (json)->$("#version").append($(json).map ->new Option(this.version, this.id)).trigger "change"

    $("#version").change ->
      appId = $(this).val()
      if -1 == parseInt(appId)
        $(this).data('myinput').val("").show()
        return
      $(this).data('myinput').hide()
  open: (event, ui)->
    productId = $("#selVersion").val()
    url = "rest/applications/base/#{productId}"
    $.getJSON url, {}, (json)->
      appBasesOptions = $("#newOrAddApplicationDialog").find("#applicationName").empty().append(new Option('new', -1))
      appBasesOptions.append($(json).map ->new Option(this.name, this.id)).trigger 'change'
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
        if json.status != 0
          $.msgBox json.message, null, {title: c18n.error, width: 300, height: 'auto'}
          return
        if -1 == params.appBaseId
#          TODO: create node on tree
          (require 'appmng/apptree').getSelected()
        $("#applicationGridList").trigger("reloadGrid")

      $(this).dialog("close")
    }
    {text: c18n.cancel, click: -> $(this).dialog "close"}
  ]
  }

  newProduct: newProduct
  newProductRelease: newProductRelease
  newOrAddApplication: newOrAddApplication