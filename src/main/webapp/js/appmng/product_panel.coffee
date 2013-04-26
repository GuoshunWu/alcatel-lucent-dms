define [
  'jqmsgbox'
  'i18n!nls/common'
  'dms-urls', 'dms-util'
  'appmng/application_grid'
  'appmng/dialogs'
], ($, c18n, urls, util, grid, dialogs)->
#  console?.log "module appmng/product_panel loading."

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

  searchActionBtn = $('#prodSearchAction', '#appmng').attr('title', 'Search').button(text: false, icons:
    {primary: "ui-icon-search"})
    .height(20).width(20).position(my: 'left center', at: 'right center', of: '#prodSearchText')
    .click((e)=>
      selVer=$("#selVersion", '#DMS_productPanel')


      if !selVer.val() || -1 == selVer.val()
        node=util.getProductTreeInfo()
        $.msgBox c18n.noversion 'Product', node.text
        return

      dialogs.showSearchResult(
        text: $('#prodSearchText', '#appmng').val()
        version:
          id: selVer.val()
          text: $("option:selected", selVer).text()
      )
    )

  $('#prodSearchText', '#appmng').keydown (e)=>
    return true  if e.which != 13
    searchActionBtn.trigger 'click'
    false

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
