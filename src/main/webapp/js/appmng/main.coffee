define (require)->
  $ = require 'jqgrid'
  util = require 'dms-util'

  productpnl = require 'appmng/product_panel'
  apppnl = require 'appmng/application_panel'
  layout = require 'appmng/layout'

  ########## reference legency codes here ###########
  dialogs = require 'appmng/dialogs'


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
    #binds product tree actions for appmng.jsp panel
  init()
  ready(@)

  onShow: onShow
  nodeSelect: nodeSelectHandler


