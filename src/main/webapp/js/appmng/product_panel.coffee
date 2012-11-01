define (require)->
  $ = require 'jquery'
  grid = require 'appmng/application_grid'
  dialogs = require 'appmng/dialogs'
  require 'jqmsgbox'
  c18n = require 'i18n!nls/common'

  URL = {
  # get product by it id url, append product id to this url
  get_product_by_base_id: '/rest/products/version'
  }
  localIds = {
  select_product_version: '#selVersion'
  new_product_version: '#newVersion'
  remove_product_version: '#removeVersion'
  disp_product_name: '#dispProductName'
  }

  $("#{localIds.new_product_version}").button({text: false, label: '&nbsp;', icons: {primary: "ui-icon-plus"}}).click () =>
    dialogs.newProductVersion.dialog("open")

  $("#{localIds.remove_product_version}").button({text: false, label: '&nbsp;', icons: {primary: "ui-icon-minus"}}).click () =>
    id = $("#{localIds.select_product_version}").val()
    return if !id
    $.post '/app/remove-product', {id: id}, (json)->
      if json.status != 0
        $.msgBox json.message, null, {title: c18n.error}
        return
      $("#{localIds.select_product_version} option:selected").remove().trigger 'change'


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
      $("#{localIds.select_product_version} option:last").attr('selected', true)
      $(localIds.select_product_version).trigger 'change'

  getSelectedProduct: -> {version: $(localIds.select_product_version).find("option:selected").text(), id: $(localIds.select_product_version).val()}
  getProductSelectOptions: ->$(localIds.select_product_version).children('option').clone(true)
  addNewProduct: (product)->
    newOption = new Option product.version, product.id
    newOption.selected = true
    $(localIds.select_product_version).append(newOption).trigger 'change'
  removeProduct: (product)->
  #    $(localIds.select_product_version)


