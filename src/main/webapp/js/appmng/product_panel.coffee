define ['jqmsgbox', 'i18n!nls/common', 'dms-urls', 'dms-util', 'appmng/application_grid'],($, c18n, urls, util, grid)->
  console?.log "module appmng/product_panel loading."

  $("#newVersion").button(text: false, label: '&nbsp;', icons:
    {primary: "ui-icon-plus"}).
  attr('privilegeName', util.urlname2Action 'app/create-product-release').
  click () =>
    $("#newProductReleaseDialog").dialog("open")

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
    $.getJSON urls.prod_versions, {base: productInfo.base.id, prop: 'id,version'}, (json)->
      # update product version select
      selVer=$('#selVersion', "div[id='appmng']")
      selVer.empty().append(util.json2Options json)
      #  get the productId from previous panel
      if(param.currentSelected.productId and parseInt(param.currentSelected.productId) != -1)
        selVer.val(param.currentSelected.productId)
        param.currentSelected.productId = null
      selVer.trigger 'change'

  getSelectedProduct: -> {version: $("#selVersion option:selected").text(), id: $('#selVersion').val()}
  getProductSelectOptions: ->$('#selVersion').children('option').clone(true)
  addNewProduct: (product)->$('#selVersion').append(util.newOption product.version, product.id, true).trigger 'change'
