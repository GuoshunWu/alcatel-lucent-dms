# Implement the navigation tree on the east
define (require)->
  require 'jqtree'
  require 'jqmsgbox'

  util = require 'dms-util'
  urls = require 'dms-urls'
  c18n = require 'i18n!nls/common'

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
    $.post urls[node.attr('type')].del, {id: node.attr('id')}, (json)->
      if json.status != 0
        $.msgBox json.message, null, {title: c18n.error, width: 300, height: 'auto'}
        return false
      appTree?.remove node
  #      console.log 'remove node ' + appTree.get_text(node)

  nodeCtxMenu =
    products:
      createproduct:
        label: 'New product'
        "_disabled": (util.urlname2Action urls.product.create) in param.forbiddenPrivileges
        action: (node)->appTree?.create node, 'last', {data: 'NewProduct', attr: {type: 'product', id: null}}, (->), false
        separator_before: true
    product:
      createapp:
        label: 'New application'
        "_disabled": (util.urlname2Action urls.app.create) in param.forbiddenPrivileges
        action: (node)->appTree?.create node, 'last', {data: 'NewApp', attr: {type: 'app', id: null}}, (->), false
      del:
        label: 'Delete product'
        "_disabled": (util.urlname2Action urls.product.del) in param.forbiddenPrivileges
        action: removeNode
        separator_before: true
    app:
      del:
        label: 'Delete application'
        "_disabled": (util.urlname2Action urls.app.del) in param.forbiddenPrivileges
        action: removeNode


  $.jstree._themes = "css/jstree/themes/"

  #  single click node event
  timeFunName = null

  $.getJSON urls.navigateTree, {}, (treeInfo) ->
    $("#appTree").jstree(
      json_data: {data: treeInfo}
      ui: {select_limit: 1}, themes: {}, core: {initially_open: ["-1"]}, contextmenu: {items: (node)->nodeCtxMenu[node.attr('type')]}
      plugins: [ "themes", "json_data", "ui", "core", "crrm", "contextmenu"]
    ).bind('create.jstree',
      (event, data)->
        appTree = data.inst
        node = data.rslt.obj
        name = data.rslt.name
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
    ).bind('loaded.jstree',
      (event, data)->
        appTree = data.inst
        #   productBase should be selected if param.currentSelected.productBaseId is not -1
        if param.currentSelected.productBaseId
          appTree.select_node $("#appTree li [id=#{param.currentSelected.productBaseId}][type=product]")
    ).bind("select_node.jstree",
      (event, data)->
      #  Cancel the last time delay unexecuted method
        clearTimeout(timeFunName)
        # Delay 300 milliseconds executive click
        timeFunName = setTimeout(
          ()->
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
          , 300)

    ).bind('dblclick_node.jstree', (event, data)->
      #  Cancel the last time delay unexecuted method
        clearTimeout(timeFunName)
        data.inst.toggle_node data.rslt.obj
    )
  #
  #    layout.showWelcomePanel()
  #    util.afterInitilized(this)
  #    $('#loading-container').fadeOut 'slow', 'swing', ()->$(@).remove()
  #
  getNodeInfo: getNodeInfo



