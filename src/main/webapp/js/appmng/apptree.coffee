# Implement the navigation tree on the east
define (require)->
  $ = require 'jqtree'
  layout = require 'appmng/layout'
  productpnl = require 'appmng/product_panel'
  apppnl = require 'appmng/application_panel'
  require 'jqmsgbox'
  c18n = require 'i18n!nls/common'

  ids = {
  navigateTree: 'appTree'
  }
  URL = {
  navigateTree: '/rest/products?format=tree&nocache=' + new Date().getTime()
  product:
    {
    create: '/app/create-product'
    del: '/app/remove-product-base'
    }
  app:
    {
    create: ''
    del: '/app/remove-application-base'
    }
  }

  appTree = null

  getNodeInfo = (node)->
    if !appTree
      console.log "Error, appTree is null."
      return null

    selectedNode = if node then node else appTree.get_selected()
    info = id: selectedNode.attr('id'), text: appTree.get_text(selectedNode), type: selectedNode.attr('type')

    parent = appTree._get_parent(selectedNode)
    return info if parent == -1
    info.parent = getNodeInfo(parent)
    info

  nodeCtxMenu =
    products:
      createproduct:
        label: 'New product'
        action: (node)->appTree?.create node, 'last', {data: 'NewProduct', attr: {type: 'product', id: null}}, (->), false
        separator_before: true
    product:
      createapp:
        label: 'New application'
        action: (node)->appTree?.create node, 'last', {data: 'NewApp', attr: {type: 'app', id: null}}, (->), false
      del:
        label: 'Delete product'
        action: (node)->appTree?.remove node
        separator_before: true
    app:
      del:
        label: 'Delete application'
        action: (node)->appTree?.remove node

  $.jstree._themes = "css/jstree/themes/"
  $.getJSON URL.navigateTree, {}, (treeInfo) ->
    $("##{ids.navigateTree}").jstree({
    json_data: {data: treeInfo}
    ui: {select_limit: 1}, themes: {}, core: {}, contextmenu: {items: (node)->nodeCtxMenu[node.attr('type')]}
    plugins: [ "themes", "json_data", "ui", "core", "crrm", "contextmenu"]
    }).bind("select_node.jstree",
      (event, data)->
        appTree = data.inst
        node = data.rslt.obj
        nodeInfo = getNodeInfo node

        switch node.attr('type')
          when 'products'
            layout.showWelcomePanel()
          when 'product'
            productpnl.refresh nodeInfo
            layout.showProductPanel()
          when 'app'
            apppnl.refresh nodeInfo
            layout.showApplicationPanel()

    ).bind('create.jstree',
      (event, data)->
        appTree = data.inst
        node = data.rslt.obj
        name = data.rslt.name
        #        todo: new name need tobe validated here
        validatename = false
        if validatename
          console.log 'name is blank, rollback.'
          $.jstree.rollback(data.rlbk)
          return
        #         validation passed, ask server to create the product(application)
        $.post URL[node.attr('type')].create, {name: name}, (json)->
          if json.status != 0
            $.msgBox json.message, null, {title: c18n.error, width: 300, height: 'auto'}
            $.jstree.rollback(data.rlbk)
            return false
          #            add type for created node
          data.rslt.obj.attr {id: json.id}
    ).bind('remove.jstree', (event, data)->
        appTree = data.inst
        node = data.rslt.obj
        $.post URL[node.attr('type')].del, {id: node.attr('id')}, (json)->
          if json.status != 0
            $.msgBox json.message, null, {title: c18n.error, width: 300, height: 'auto'}
            $.jstree.rollback(data.rlbk)
            return false
          console.log 'remove node ' + appTree.get_text(node)
    )

    #   get tree instance
    appTree = $.jstree._reference("##{ids.navigateTree}")

    $('#loading-container').remove()

  getNodeInfo: getNodeInfo
  delApplictionBaseFromProductBase: (appBaseId)->
    appTree = $.jstree._reference "##{ids.navigateTree}"
    (appTree._get_children appTree.get_selected()).each (index, app)->appTree.delete_node(app) if parseInt(app.id) == appBaseId
  addNewProductBase: (product)->
    ($.jstree._reference "##{ids.navigateTree}").create_node -1, "last", {data: product.name, attr: {id: product.id}}
  addNewApplicationBase: (params)->
    appTree = $.jstree._reference "##{ids.navigateTree}"
    selectedNode = appTree.get_selected()
    $("#appTree").jstree("create_node", selectedNode, "last", {data: params.appBaseName, attr: {id: params.appBaseId}})


