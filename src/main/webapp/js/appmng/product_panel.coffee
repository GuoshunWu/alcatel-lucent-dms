define (require)->
  $ = require 'jquery'
  require 'jqmsgbox'
  util = require 'util'
  c18n = require 'i18n!nls/common'
  grid = require 'appmng/application_grid'
  dialogs = require 'appmng/dialogs'


  URL = {
  # get product by it id url, append product id to this url
  get_product_by_base_id: 'rest/products/version'
  }


  $("#newVersion").button(text: false, label: '&nbsp;', icons:
    {primary: "ui-icon-plus"}).
  attr('privilegeName', util.urlname2Action 'app/create-product-release').
  click () =>
    dialogs.newProductVersion.dialog("open")

  $("#removeVersion").button(text: false, label: '&nbsp;', icons:
    {primary: "ui-icon-minus"})
  .attr('privilegeName', util.urlname2Action 'app/remove-product').click(()=>
    id = $("#selVersion").val()
    return if !id
    $.post 'app/remove-product', {id: id}, (json)->
      if json.status != 0
        $.msgBox json.message, null, {title: c18n.error}
        return
    $("#selVersion option:selected").remove()
    $('#selVersion').trigger 'change'
  )


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
      $('#selVersion').empty().append(util.json2Options json)
      #      here use global var param from env.jsp
      $('#selVersion').val(param.currentSelected.productId) if(param.currentSelected.productId)
      $('#selVersion').trigger 'change'



  getSelectedProduct: -> {version: $("#selVersion option:selected").text(), id: $('#selVersion').val()}
  getProductSelectOptions: ->$('#selVersion').children('option').clone(true)
  addNewProduct: (product)->$('#selVersion').append(util.newOption product.version, product.id, true).trigger 'change'
