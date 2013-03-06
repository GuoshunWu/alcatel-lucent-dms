define ['jqueryui', 'dms-util', 'dms-urls', 'taskmng/task_grid', 'taskmng/dialogs'], ($, util, urls, grid, dialogs)->

  nodeSelectHandler = (node, nodeInfo)->
    type=node.attr('type')
    return if 'products' == type

    $('#versionTypeLabel',"div[id='taskmng']").text "#{nodeInfo.text}"
    if 'product' == type
      $.getJSON urls.prod_versions, {base: nodeInfo.id, prop: 'id,version'}, (json)->
        $('#selVersion',"div[id='taskmng']").empty().append(util.json2Options(json)).trigger 'change'
      return
#
    if 'app' == type
      $.getJSON "#{urls.app_versions}#{nodeInfo.id}" , (json)->
        $('#selVersion',"div[id='taskmng']").empty().append(util.json2Options(json)).trigger 'change'

  onShow = ()->
    gridParent = $('.taskGrid_parent')
    $('#taskGrid').setGridWidth(gridParent.width() - 10).setGridHeight(gridParent.height() - 110)


  init = ()->
#    console.log grid
    console?.debug "transmng panel init..."

  ready = (param)->
    console?.debug "transmng panel ready..."
    # initilize version selector
    $('#selVersion', '#taskmng').change ()->
      return if !@value or -1 == parseInt @value
      nodeInfo=(require 'ptree').getNodeInfo()
      #      console?.log nodeInfo
      type = nodeInfo.type
      type = type[..3] if type.startWith('prod')

      postData = {prop: 'id,name'}
      postData[type] = @value

      param =
        release: {id: $(@).val()
        version: $(@).find("option:selected").text()}
        type: type

      return false if !param.release.id || parseInt(param.release.id) == -1

      grid.productVersionChanged param


  init()
  ready(@)

  onShow: onShow
  nodeSelect: nodeSelectHandler