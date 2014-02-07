define [
  'jqueryui'
  'blockui'
  'jqmsgbox'

  'i18n!nls/common'

  'dms-urls'
  'dms-util'
  'ctxmng/layout'

  'ctxmng/context_grid'
  'ctxmng/compare_context_grid'
  'ctxmng/dialogs'

], ($, blockui, jqmsgbox, c18n, urls, util, layout, ctxgrid, comparectxgrid, dialogs)->
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
    layout.layout.resizeAll()
    ctxmngContainerHeight = $('#layout-container', "#ctxmng").height()
    layout.layout.sizePane("south", ctxmngContainerHeight * 0.33)

    $('#contextSearchAction').position(my: 'left center', at: 'right center', of: '#contextSearchText')

  init = ()->

  ready = ()->
    $.post urls.contexts, {prop: 'id, key', sidx: 'key'}, (json)->
      options="<option value='-1'>#{c18n.select.context.tip}</option>" + util.json2Options(json, false, 'key')
      ctxSelector=$('#contextSelector', '#ctxmng').append(options).change(->
#        return if "-1" == @value
        postData = ctxgrid.grid.getGridParam('postData')
        postData.context = @value
        ctxgrid.grid.trigger 'reloadGrid'
      )

      searchActionBtn = $('#contextSearchAction', '#ctxmng').button(
        text: false
        icons:{primary: "ui-icon-search"}
      ).height(20).width(20).click(->
        alert('search text performed.')
      )

      $('#contextSearchText', '#ctxmng').keydown (e)->
        return true if e.which != 13
        searchActionBtn.trigger 'click'
        false

      $('#compareWithContextSelector', '#ctxmng').append(options).change(->
        postData = comparectxgrid.grid.getGridParam('postData')
        postData.context = @value
        comparectxgrid.grid.trigger 'reloadGrid'
      )

      $('#contextShowDiff', '#ctxmng').button(

      ).click(-> alert("to be implemented."))

      $('#contextMerge', '#ctxmng').button(

      ).click(-> alert("to be implemented."))

  init()
  ready()

  nodeSelect: nodeSelectHandler
  onShow: onShow
