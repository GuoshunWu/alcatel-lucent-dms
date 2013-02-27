define [],(require)->
  $ = require 'jqgrid'
  util = require 'dms-util'

  require 'appmng/dialogs'
  productpnl = require 'appmng/product_panel'
  apppnl = require 'appmng/application_panel'
  layout = require 'appmng/layout'

  nodeSelectHandler = (node, nodeInfo)->
    switch node.attr('type')
      when 'products'
        layout.showWelcomePanel()
      when 'product'
        productpnl.refresh nodeInfo
        layout.showProductPanel()
      when 'app'
        apppnl.refresh nodeInfo
        layout.showApplicationPanel()

  onShow = ()->

  init = ()->
  ################################################## Initilaize #####################################################
    # initialize appmng panels
    layout.showWelcomePanel()
  ################################################## Initilaized ####################################################

  ready = (param)->
    console?.debug "appmng panel ready..."
  init()
  ready(@)

  onShow: onShow
  nodeSelect: nodeSelectHandler


