localIds = {
select_product_version: '#selVersion'
new_product_version: '#newVersion'
disp_product_name: '#dispProductName'
}
# init buttons on product panel
$(localIds.new_product_version).button({ text: false, icons: {primary: "ui-icon-plus"}}).click ()->
#  todo: update the element in product before open
  $("##{ids.dialog.new_product_release}").dialog "open"

$("#newApp").button().click ()-> alert "to be implemented."
$("#addApp").button().click ()-> alert "to be implemented."
$("#removeApp").button().click ()-> alert "to be implemented."
$("#download").button().click ()-> alert "Hello, test"

# initial product version select

$(localIds.select_product_version).change ()->
  product = {version: $(this).find("option:selected").text(), id: $(this).val()}
  url = URL.get_application_by_product_id + product.id
  appGridId = "#{exports.application_grid.application_grid_id}"
  $(appGridId).jqGrid('setGridParam', {url: url, datatype: "json"}).trigger("reloadGrid")
  appTree = $.jstree._reference "##{ids.navigateTree}"
  productBaseName=appTree.get_text appTree.get_selected()
  $(appGridId).jqGrid 'setCaption', "Applications for Product #{productBaseName} version #{product.version}"

# refresh product element on the panel
refresh = (info)->
# info.id, info.text is productBase id and name
  $(localIds.disp_product_name).html info.text
  $.getJSON URL.get_product_by_base_id + info.id, {}, (json)->
  # update product version select
    $(localIds.select_product_version).empty().append ($(json).map ()-> new Option this.version, this.id)
    $(localIds.select_product_version).trigger 'change'

exports.product_panel.refresh = refresh
exports.product_panel.select_product_version_id = localIds.select_product_version