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
        params = $(@).data 'params'
        params.rowData.context

        option = $('#contextSelector > option:selected', "#ctxmng")

        # bound to textA id
        $('#ctxALabel', @).text option.text()
        $('#contextA', @).val(option.val()).prop('checked', '[DEFAULT]' == option.text())
        $('#contextA', @).prop("checked", true) unless $("input:radio[name=contextGrp]:checked", @).length

        $('#ctxBLabel', @).text params.rowData.context
        $('#contextB', @).val(params.rowData.contextId).prop('checked', '[DEFAULT]' == params.rowData.context)
        # bound to textB id
      buttons: [
        {text: c18n.ok, click: ->
          params = $(@).data 'params'

          postData =
            contextAId: $('#contextA', @).val()
            contextATextId: $('#contextGrid').getGridParam('selrow')
            contextBId: $('#contextB', @).val()
            contextBTextId: params.id
            mergedToContextId: $("input:radio[name=contextGrp]:checked", @).val()
            reference: params.rowData.reference

#          console.log "postData=", postData

          compareGrid = $('#compareContextGrid')
          $.post urls.context.merge, postData , (json)->
#            console.log "return json=", json
            if(-1 == json.status)
              $.msgBox json.message, null, {title: c18n.error}
              return
            if(1 == json.status)
              $.msgBox json.message, (keyPressed)->
                $('#selVersion',"div[id='ctxmng']").trigger 'change'
                compareGrid.clearGridData()
              , {title: c18n.message}, [c18n.ok]
              return

            $('#contextGrid').trigger 'reloadGrid'
            compareGrid.clearGridData()
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

          $.post urls.context.take_translations, translationPairs: JSON.stringify(results), (json)->
#            console.log "back data=", json
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

