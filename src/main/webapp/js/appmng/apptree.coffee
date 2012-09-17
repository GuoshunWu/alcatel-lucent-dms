# Implement the navigation tree on the east
define ['jqtree', 'cs!appmng/dialogs', 'cs!appmng/layout', 'cs!appmng/product_panel', 'cs!appmng/application_panel'], ($, dialogs, layout, productpnl, apppnl)->
  ids = {
  navigateTree: 'appTree'
  }
  URL = {
  navigateTree: 'rest/products?nocache=' + new Date().getTime()
  }
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
      parent = appTree._get_parent(node.rslt.obj)
      nodeInfo = {text: appTree.get_text(node.rslt.obj), parent , id: node.rslt.obj.attr("id")}

      if (-1 == nodeInfo.parent)
      # refresh before show
        productpnl.refresh nodeInfo
        layout.showProductPanel()
      else
      # refresh before show
        nodeInfo.parent = {id: parent.attr('id'), text: appTree.get_text(parent)}
        apppnl.refresh nodeInfo
        layout.showApplicationPanel()

  getSelected: ->
    info = {}
    appTree = $.jstree._reference "##{ids.navigateTree}"
    selectedNode = appTree.get_selected()

    info.id = selectedNode.attr "id"
    info.text = appTree.get_text(selectedNode)

    parent = appTree._get_parent(selectedNode)
    if -1 == parent
      info.parent = null
      return info
    info.parent = {id: parent.attr('id'), text: appTree.get_text(parent)}
    info
