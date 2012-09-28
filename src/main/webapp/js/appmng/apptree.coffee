# Implement the navigation tree on the east
define (require)->
  $ = require 'jqtree'
  layout = require 'appmng/layout'
  productpnl = require 'appmng/product_panel'
  apppnl = require 'appmng/application_panel'
  dialogs = require 'appmng/dialogs'

  console.log layout

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
    ui: {select_limit: 1}, themes: {}, core: {}
    plugins: [ "themes", "json_data", "ui", "core"]
    }).bind "select_node.jstree", (event, node) ->
      appTree = $.jstree._reference "##{ids.navigateTree}"
      parent = appTree._get_parent(node.rslt.obj)
      nodeInfo = {text: appTree.get_text(node.rslt.obj), parent, id: node.rslt.obj.attr("id")}
      if (-1 == nodeInfo.parent)
      # refresh before show
        productpnl.refresh nodeInfo
        layout.showProductPanel()
      else
      # refresh before show
        nodeInfo.parent = {id: parent.attr('id'), text: appTree.get_text(parent)}
        apppnl.refresh nodeInfo
        layout.showApplicationPanel()

  $('#loading-container').remove()

  getSelected: ->
    appTree = $.jstree._reference "##{ids.navigateTree}"
    selectedNode = appTree.get_selected()
    parent = appTree._get_parent(selectedNode)
    {
    id: selectedNode.attr "id"
    text: appTree.get_text(selectedNode)
    parent: if -1 == parent then parent else {id: parent.attr('id'), text: appTree.get_text(parent)}
    }
  delApplictionBaseFromProductBase: (appBaseId)->
    appTree = $.jstree._reference "##{ids.navigateTree}"
    (appTree._get_children appTree.get_selected()).each (index, app)->appTree.delete_node(app) if parseInt(app.id) == appBaseId
  addNewProductBase: (product)->
    ($.jstree._reference "##{ids.navigateTree}").create_node -1, "last", {data: product.name, attr: {id: product.id}}

