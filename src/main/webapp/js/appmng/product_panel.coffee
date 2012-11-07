define (require)->
  $ = require 'jquery'
  grid = require 'appmng/application_grid'
  dialogs = require 'appmng/dialogs'
  require 'jqmsgbox'
  c18n = require 'i18n!nls/common'

  URL = {
  # get product by it id url, append product id to this url
  get_product_by_base_id: 'rest/products/version'
  }


  $("#newVersion").button({text: false, label: '&nbsp;', icons: {primary: "ui-icon-plus"}}).click () =>
    dialogs.newProductVersion.dialog("open")

  $("#removeVersion").button({text: false, label: '&nbsp;', icons: {primary: "ui-icon-minus"}}).click () =>
    id = $("#selVersion").val()
    return if !id
    $.post 'app/remove-product', {id: id}, (json)->
      if json.status != 0
        $.msgBox json.message, null, {title: c18n.error}
        return
      $("#selVersion option:selected").remove()
      $('#selVersion').trigger 'change'


  productInfo = {}
  # initial product version select
  $('#selVersion').change ()->
    product = {version: $(@).find("option:selected").text(), id: $(@).val()}
    product.id = -1 if !product.id
    productInfo.product = product
    grid.productChanged productInfo

  refresh: (info)->
  # info.id, info.text is productBase id and name
    productInfo.base = {id: info.id, text: info.text}
    $('#dispProductName').html productInfo.base.text
    $.getJSON URL.get_product_by_base_id, {base: productInfo.base.id, prop: 'id,version'}, (json)->
    # update product version select
      $('#selVersion').empty().append($(json).map ()-> new Option @version, @id)
      $("#selVersion option:last").attr('selected', true)
      $('#selVersion').trigger 'change'

  getSelectedProduct: -> {version: $("#selVersion option:selected").text(), id: $('#selVersion').val()}
  getProductSelectOptions: ->$('#selVersion').children('option').clone(true)
  addNewProduct: (product)->
    newOption = new Option product.version, product.id
    newOption.selected = true
    $('#selVersion').append(newOption).trigger 'change'
