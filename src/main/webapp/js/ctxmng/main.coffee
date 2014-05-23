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
#    console.log "in node select handler: nodeInfo=", nodeInfo
    $("#typeLabel","div[id='ctxmng']").text "#{c18n[nodeInfo.type].capitalize()}: "
    $('#versionTypeLabel',"div[id='ctxmng']").text "#{nodeInfo.text}"
    verSel = $('#selVersion',"div[id='ctxmng']").empty()
    if 'products' == nodeInfo.type
      # Load all the contexts in all product versions
      $.post urls.contexts, {prop: 'id, key', sidx: 'key'}, (json)->
        options = util.json2Options(json, false, 'key')
        $('#contextSelector', '#ctxmng').empty().append(options).trigger 'change'
      return

    url = if 'prod' == nodeInfo.type then urls.prod_versions else urls.app_versions1
    $.getJSON url, {base: nodeInfo.id, prop: 'id,version'}, (json)->verSel.append(util.json2Options(json)).trigger 'change'

  ###
  panel show callback
  ###
  onShow = ()->
    layout.layout.resizeAll()
    ctxmngContainerHeight = $('#layout-container', "#ctxmng").height()
    layout.layout.sizePane("south", ctxmngContainerHeight * 0.33)

    $('#contextSearchAction').position(my: 'left center', at: 'right center', of: '#contextSearchText')

  init = ()->

  ready = ()->
    $('#selVersion', "div[id='ctxmng']").change ()->
      return unless @value
      nodeInfo = util.getProductTreeInfo()
      postData =
        prop: 'id, key'
        sidx: 'key'
      postData[nodeInfo.type] = @value unless 'products' == nodeInfo.type
#      console.log "postData=", postData
      $.post urls.contexts, postData, (json)->
        options = util.json2Options(json, false, 'key')
        $('#contextSelector', '#ctxmng').empty().append(options).trigger 'change'


    ctxSelector = $('#contextSelector', '#ctxmng').change(->
      ctxgrid.grid.clearGridData()
      comparectxgrid.grid.clearGridData()
      nodeInfo = util.getProductTreeInfo()
      ver =  $('#selVersion option:selected', "div[id='ctxmng']").text()
      ctxgrid.grid.setCaption "Texts in Context: #{$('option:selected', @).text()} of #{c18n[nodeInfo.type]} version #{ver}"
      return unless @value

      postData = ctxgrid.grid.getGridParam('postData')
      delete postData.prod
      delete postData.app
      postData[nodeInfo.type] =  $('#selVersion', "div[id='ctxmng']").val() unless 'products' == nodeInfo.type
      postData.context = @value
#      console.log "in context selector, postData=", postData
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

    $('#compareWithContextSelector', '#ctxmng').change(->
      postData = comparectxgrid.grid.getGridParam('postData')
      delete postData.prod
      delete postData.app
      postData[type] = currentProductInfo.id unless 'products' == type
      postData.context = @value
      console.log "in context selector, postData=", postData
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
