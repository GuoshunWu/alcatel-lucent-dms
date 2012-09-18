define ['jqueryui', 'jqmsgbox', 'i18n!nls/common'], ($, msgbox, c18n)->
  ids = {
  button:
    {
    new_product: 'newProduct'
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
  console.log c18n.cancel
  newProduct = $("##{ids.dialog.new_product}").dialog {
  autoOpen: false, height: 200, width: 400, modal: true,
  buttons: [
    {text: c18n.ok, click: ->
      # TODO: validate the product name...
      $.post 'app/create-product', {name: $(ids.productName).val()}, (json)->
        if (json.status != 0)
          $.msgBox json.message, null, {title: 'Error', width: 300, height: 'auto'}
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
  buttons:
    {
    'OK': ()->
      alert 'OK'
      $(this).dialog "close"
    'Cancel': ()->
      alert 'Cancel'
      $(this).dialog "close"
    }
  open: (event, ui)->
    console.log($(exports.product_panel.select_product_version_id)[0])
  #    $(module_ids.product_duplication).append new Option '', -1
  #    $(exports.product_panel.select_product_version_id).children('option').clone(true).appendTo $ module_ids.product_duplication
  }

  # Create new application or add application to product dialog
  newOrAddApplication = $("##{ids.dialog.new_or_add_application}").dialog {
  autoOpen: false
  }

  newProduct: newProduct
  newProductRelease: newProductRelease
  newOrAddApplication: newOrAddApplication

