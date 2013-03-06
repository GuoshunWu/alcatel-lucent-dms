dependencies = [
  'jqgrid'
  'dms-util'

  'appmng/product_panel'
  'appmng/application_panel'
  'appmng/layout'
]

define dependencies, ($, util, productpnl, apppnl, layout)->
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


