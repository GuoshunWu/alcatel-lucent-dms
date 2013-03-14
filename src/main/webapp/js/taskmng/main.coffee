define [
  'jqgrid'
  'i18n!nls/common'
  'dms-util'
  'dms-urls'
  'taskmng/task_grid'
  'taskmng/dialogs'
], ($, c18n, util, urls, grid)->

  nodeSelectHandler = (node, nodeInfo)->
    type = node.attr('type')
    return if 'products' == type
    type = 'prod' if type == 'product'

    $('#versionTypeLabel',"div[id='taskmng']").text "#{nodeInfo.text}"
    $('#typeLabel',"div[id='taskmng']").text "#{c18n[type].capitalize()}: "

    if 'prod' == type
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
    console?.log "transmng panel init..."

  ready = (param)->
    console?.log "transmng panel ready..."
    # initilize version selector
    $('#selVersion', '#taskmng').change ()->
      return if !@value or -1 == parseInt @value
      nodeInfo=util.getProductTreeInfo()
      #      console?.log nodeInfo
      type = nodeInfo.type

      postData = {prop: 'id,name'}
      postData[type] = @value

      param =
        release: {id: $(@).val()
        version: $(@).find("option:selected").text()}
        base: nodeInfo.text
        type: type

      return false if !param.release.id || parseInt(param.release.id) == -1

      grid.versionChanged param


  init()
  ready(@)

  onShow: onShow
  nodeSelect: nodeSelectHandler