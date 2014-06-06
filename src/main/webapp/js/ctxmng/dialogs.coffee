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

    $('#ctxMergesDialog', '#ctxmng').dialog(
      autoOpen: false
      width: 650
      open:()->
        grid = diffGrid.grid
        params = $(@).data 'params'
        option = $('#contextSelector > option:selected', "#ctxmng")
        params.rowData.context

        $('#ctxALabel', @).text option.text()
        # bound to textA id
        $('#contextA', @).val($('#contextGrid').getGridParam('selrow')).prop('checked', '[DEFAULT]' == option.text())
        $('#ctxBLabel', @).text params.rowData.context
        # bound to textB id
        $('#contextB', @).val(params.id).prop('checked', '[DEFAULT]' == params.rowData.context)

        $('#contextA', @).prop('checked',true) unless $("input:radio[name=contextGrp]:checked", @).length
      buttons: [
        {text: c18n.ok, click: ->
          params = $(@).data 'params'
          result =
            ctxA: $('#contextA', @).val()
            ctxB: $('#contextB', @).val()
            selected: $("input:radio[name=contextGrp]:checked", @).val()
          console.log result
          alert(JSON.stringify result)
          $(@).dialog('close')
        }
      ]
    )

    $('#ctxDifferencesDialog', '#ctxmng').dialog(
      autoOpen: false
      width: 1180
      height: 500
      buttons: [
        {text: i18n.takeleft, click: ->diffGrid.takeForAll 'A'
        }
        {text: i18n.takeright,  style: 'margin-right: 820px !important', click: ->
          diffGrid.takeForAll 'B'
        }
        {text: c18n.save, click: ->
          # convert take translation result to array
          results = for textAId, val of diffGrid.getTakeTranslationResult()
            textA: textAId, textB: val.other, take: val.selected
          console.log results

          $.post urls.trans.take_translations, translationPairs: JSON.stringify(results), (json)->
            console.log "back data=", json
            diffGrid.grid.trigger 'reloadGrid'
            $('#compareContextGrid').trigger 'reloadGrid'
        }
        {text: c18n.close, click: ->$(@).dialog('close')}
      ]
      create: ()->
      open:()->
        grid = diffGrid.grid
        params = $(@).data 'params'
#        console.log params
        toCompareTextId = ($('#compareContextGrid').getGridParam 'postData').text
        postData = grid.getGridParam 'postData'
        postData.text1 = toCompareTextId
        postData.text2 = params.id

        aContext = $('#contextSelector option:selected', '#ctxmng').text()
        grid.jqGrid('destroyGroupHeader')

        grid.setGroupHeaders(useColSpanStyle: true, groupHeaders: [
          {startColumnName: 'translationA', numberOfColumns: 2, titleText: "<em>Context A: #{aContext}</em>"}
          {startColumnName: 'translationB', numberOfColumns: 2, titleText: "<em>Context B: #{params.rowData.context}</em>"}
        ])
        grid.setCaption($.jgrid.format(i18n.diffRefText, params.rowData.reference)).trigger 'reloadGrid'

        # reset takeTranslationResult
        diffGrid.setTakeTranslationResult()
    )

  init()
  ready()

