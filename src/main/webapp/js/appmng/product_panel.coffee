define (require)->
  $ = require 'jquery'
  grid = require 'appmng/application_grid'

  URL = {
  # get product by it id url, append product id to this url
  get_product_by_base_id: '/rest/products/version'
  }
  localIds = {
  select_product_version: '#selVersion'
  new_product_version: '#newVersion'
  disp_product_name: '#dispProductName'
  }

  productInfo = {}
  # initial product version select
  $(localIds.select_product_version).change ()->
    product = {version: $(@).find("option:selected").text(), id: $(@).val()}
    product.id = -1 if !product.id
    productInfo.product = product
    grid.productChanged productInfo

  refresh: (info)->
  # info.id, info.text is productBase id and name
    productInfo.base = {id: info.id, text: info.text}
    $(localIds.disp_product_name).html productInfo.base.text
    $.getJSON URL.get_product_by_base_id, {base: productInfo.base.id, prop: 'id,version'}, (json)->
    # update product version select
      $(localIds.select_product_version).empty().append($(json).map ()-> new Option @version, @id)
      $(localIds.select_product_version).trigger 'change'

  getSelectedProduct: -> {version: $(localIds.select_product_version).find("option:selected").text(), id: $(localIds.select_product_version).val()}
  getProductSelectOptions: ->$(localIds.select_product_version).children('option').clone(true)



