# Implement the navigation tree on the east
define ['require','jqtree', 'jqmsgbox', 'dms-util', 'dms-urls', 'i18n!nls/common'], (require , jqtree, $, util, urls, c18n)->

  appTree = null
  getNodeInfo = util.getProductTreeInfo
  removeNode = (node)->
    $.post urls[node.attr('type')].del, {id: node.attr('id')}, (json)->
      if json.status != 0
        $.msgBox json.message, null, {title: c18n.error, width: 300, height: 'auto'}
        return false
      appTree?.remove node

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
      plugins: [ "themes", "json_data", "ui", "core", "crrm", "contextmenu", "search"]
    ).bind('create.jstree',
      (event, data)->
        appTree = data.inst
        node = data.rslt.obj
        name = data.rslt.name
        if '' == name
          $.jstree.rollback(data.rlbk)
          return
        #         validation passed, ask server to create the product(application)
        pbId = appTree._get_parent(node).attr 'id'if 'app' == node.attr 'type'
        $.post urls[node.attr('type')].create, {name: name, prod: pbId}, (json)->
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
        if window.param.currentSelected.productBaseId
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

            currentTab=$("#pageNavigator").val()
#            console?.log "current tab= #{currentTab}"
            currentModule = currentTab.split('.')[0]
            moduleToLoad = "#{currentModule}/main"
#            console?.log "module to load: #{moduleToLoad}"
            module = require moduleToLoad
            module?.nodeSelect?(node, nodeInfo)
        , 300)

    ).bind('dblclick_node.jstree', (event, data)->
      #  Cancel the last time delay unexecuted method
        clearTimeout(timeFunName)
        data.inst.toggle_node data.rslt.obj
    )
  getNodeInfo: getNodeInfo

