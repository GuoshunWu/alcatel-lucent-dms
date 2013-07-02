define [
  'jqueryui'
  'blockui'
  'jqmsgbox'

  'i18n!nls/common'

  'dms-urls'
  'dms-util'
], ($, blockui, jqmsgbox, c18n, urls, util)->
  ###
  This will be invoked when the navigate tree node selected
  ###
  nodeSelectHandler = (node, nodeInfo)->
    type=node.attr('type')
    return if 'products' == type
    type = 'prod' if type == 'product'

    console?.log nodeInfo
  ###
  This will be invoked when the panel show
  ###
  onShow = ()->

  init = ()->
    console?.log "ctxmng panel init..."

  ready = ()->
    console?.log "ctxmng panel ready..."
  init()
  ready()

  nodeSelect: nodeSelectHandler
  onShow: onShow
