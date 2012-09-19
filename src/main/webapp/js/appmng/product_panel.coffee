define ['jquery', 'appmng/application_grid'], ($, grid)->
  URL = {
  # get product by it id url, append product id to this url
  get_product_by_base_id: 'rest/products/'
  }
  localIds = {
  select_product_version: '#selVersion'
  new_product_version: '#newVersion'
  disp_product_name: '#dispProductName'
  }
  # init buttons on product panel

  # initial product version select

  $(localIds.select_product_version).change ()->
    product = {version: $(this).find("option:selected").text(), id: $(this).val()}
    grid.productChanged product

  refresh: (info)->
  # info.id, info.text is productBase id and name
    $(localIds.disp_product_name).html info.text
    $.getJSON URL.get_product_by_base_id + info.id, {}, (json)->
    # update product version select
      $(localIds.select_product_version).empty().append ($(json).map ()-> new Option this.version, this.id)
      $(localIds.select_product_version).trigger 'change'

  getSelectedProduct: -> {version: $(localIds.select_product_version).find("option:selected").text(), id: $(localIds.select_product_version).val()}
  getProductSelectOptions: ->$(localIds.select_product_version).children('option').clone(true)
  addNewProduct:(product)->
    newOption = new Option product.version, product.id
    newOption.selected = true
    $(localIds.select_product_version).append(newOption).trigger 'change'


