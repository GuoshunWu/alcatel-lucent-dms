# Implement the navigation tree on the east

$.getJSON URL.navigateTree, {}, (treeInfo) ->
  $.jstree._themes = "css/jstree/themes/"

  ($("##{ids.navigateTree}").jstree {
  json_data: {data: treeInfo}
  ui: {select_limit: 1}
  themes: {}
  core: {}
  plugins: [ "themes", "json_data", "ui", "core"]
  }).bind "select_node.jstree", (event, node) =>
    appTree = $.jstree._reference "##{ids.navigateTree}"
    nodeInfo = {text: appTree.get_text(node.rslt.obj), parent: appTree._get_parent(node.rslt.obj), id: node.rslt.obj.attr("id")}

    if (-1 == nodeInfo.parent)
      # refresh before show
      exports.product_panel.refresh nodeInfo
      exports.layout.showCenterPanel ids.panel.product
    else
    # refresh before show
      exports.application_panel.refresh nodeInfo
      exports.layout.showCenterPanel ids.panel.application

# create new product button below the tree
$("##{ids.button.new_product}").button().click (e) =>
  $("##{ids.dialog.new_product}").dialog("open")
