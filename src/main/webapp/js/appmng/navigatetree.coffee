# Implement the navigation tree on the east
define (require)->
  $ = require 'jqtree'
  c18n = require 'i18n!nls/common'
  require 'jqmsgbox'
  layout = require 'appmng/layout'
  productpnl = require 'appmng/product_panel'
  apppnl = require 'appmng/application_panel'


  ids = {
  navigateTree: 'appTree'
  }
  URL = {
  navigateTree: 'rest/products?format=tree&nocache=' + new Date().getTime()
  product:
    {
    create: 'app/create-product'
    del: 'app/remove-product-base'
    }
  app:
    {
    create: 'app/create-application-base'
    del: 'app/remove-application-base'
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

  removeNode = (node)->
    $.post URL[node.attr('type')].del, {id: node.attr('id')}, (json)->
      if json.status != 0
        $.msgBox json.message, null, {title: c18n.error, width: 300, height: 'auto'}
        return false
      appTree?.remove node
  #      console.log 'remove node ' + appTree.get_text(node)

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
        action: removeNode
        separator_before: true
    app:
      del:
        label: 'Delete application'
        action: removeNode


  $.jstree._themes = "css/jstree/themes/"
  $.getJSON URL.navigateTree, {}, (treeInfo) ->
    $("##{ids.navigateTree}").jstree(
      json_data: {data: treeInfo}
      ui: {select_limit: 1}, themes: {}, core: {initially_open: ["-1"]}, contextmenu: {items: (node)->nodeCtxMenu[node.attr('type')]}
      plugins: [ "themes", "json_data", "ui", "core", "crrm", "contextmenu"]
    ).bind("select_node.jstree",
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

    ).bind('create.jstree', (event, data)->
        appTree = data.inst
        node = data.rslt.obj
        name = data.rslt.name
        #        todo: new name need tobe validated here
        if '' == name
        #          console.log 'name is blank, rollback.'
          $.jstree.rollback(data.rlbk)
          return
        #         validation passed, ask server to create the product(application)
        pbId = appTree._get_parent(node).attr 'id'if 'app' == node.attr 'type'
        $.post URL[node.attr('type')].create, {name: name, prod: pbId}, (json)->
          if json.status != 0
            $.msgBox json.message, null, {title: c18n.error, width: 300, height: 'auto'}
            $.jstree.rollback(data.rlbk)
            return false
          #            add type for created node
          data.rslt.obj.attr {id: json.id}
    )


    #   get tree instance
    appTree = $.jstree._reference("##{ids.navigateTree}")


    $('#loading-container').remove()
    $('#optional-container').show()

  getNodeInfo: getNodeInfo



