define [
  'jqgrid'
  'jqueryui'
  'blockui'
  'jqmsgbox'

  'i18n!nls/common'
  'i18n!nls/ctxmng'

  'dms-urls'
  'dms-util'

  'ctxmng/difflink_grid'
  'ctxmng/reflink_grid'
  'ctxmng/translink_grid'
  'ctxmng/language_grid'

], ($, jqui, blockui, jqmsgbox, c18n, i18n, urls, util, diffGrid, refGrid, transGrid, langGrid)->
  refGrid  = refGrid.grid
  diffGrid = diffGrid.grid
  transGrid = transGrid.grid
  langGrid = langGrid.grid

  init = ()->

  ready = ()->
    $('#ctxLanguagesDialog', '#ctxmng').dialog(
      autoOpen: false
      width : 840
      buttons: [
        {text: c18n.close, click: ->$(@).dialog('close')}
      ]
      create: ()->
      open: ()->
        params = $(@).data 'params'
        $('#transRefText', @).text params.rowData.reference
        langGrid.setGridParam(postData: {text: params.id}).trigger 'reloadGrid'
    )

    $('#ctxReferencesDialog', '#ctxmng').dialog(
      autoOpen: false
      width : 1010

      buttons: [
        {text: c18n.close, click: ->$(@).dialog('close')}
      ]
      create: ()->
      open: ()->
        params = $(@).data 'params'
        $('#refText', @).text params.rowData.reference
        refGrid.setGridParam(postData: {text: params.id}).trigger 'reloadGrid'
    )

    $('#ctxTranslationsDialog', '#ctxmng').dialog(
      autoOpen: false
      width: 840
      buttons: [
        {text: c18n.close, click: ->$(@).dialog('close')}
      ]
      open: ()->
        params = $(@).data 'params'
        statusMap ='n': 0, 'i': 1, 't': 2
        $('#transRefText', @).text params.rowData.reference
        $('#transLinkGrid').setGridParam(postData: {text: params.id, status: statusMap[params.colname]}).trigger 'reloadGrid'
      create: ()->
    )

    $('#ctxDifferencesDialog', '#ctxmng').dialog(
      autoOpen: false
      width: 1180
      height: 500
      buttons: [
        {text: i18n.takeleft, click: ->
        }
        {text: i18n.takeright,  style: 'margin-right: 820px !important', click: ->
        }
        {text: c18n.save, click: ->
          alert 'To be implemented.'
        }
        {text: c18n.close, click: ->$(@).dialog('close')}
      ]
      create: ()->
      open:()->
        params = $(@).data 'params'
#        console.log params
        toCompareTextId = ($('#compareContextGrid').getGridParam 'postData').text
        postData = diffGrid.getGridParam 'postData'
        postData.text1 = toCompareTextId
        postData.text2 = params.id

        aContext = $('#contextSelector option:selected', '#ctxmng').text()
        diffGrid.jqGrid('destroyGroupHeader')

        diffGrid.setGroupHeaders(useColSpanStyle: true, groupHeaders: [
          {startColumnName: 'translationA', numberOfColumns: 2, titleText: "<em>Context A: #{aContext}</em>"}
          {startColumnName: 'translationB', numberOfColumns: 2, titleText: "<em>Context B: #{params.rowData.context}</em>"}
        ])
        diffGrid.setCaption($.jgrid.format(i18n.diffRefText, params.rowData.reference)).trigger 'reloadGrid'
    )

  init()
  ready()

