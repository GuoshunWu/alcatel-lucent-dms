define [
  'jqgrid'
  'jqueryui'
  'blockui'
  'jqmsgbox'

  'i18n!nls/common'
  'i18n!nls/ctxmng'

  'dms-urls'
  'dms-util'

], ($, jqui, blockui, jqmsgbox, c18n, i18n, urls, util)->

  init = ()->

  ready = ()->
    $('#ctxReferencesDialog', '#ctxmng').dialog(
      autoOpen: false
      width : 700
      buttons: [
        {text: c18n.close, click: ->$(@).dialog('close')}
      ]
      create: ()->
        gridId = 'refLinkGrid'
        hGridId = "##{gridId}"

        $(hGridId, @).jqGrid(
          width: 600
          url: urls.text.refs
          datatype: 'local', mtype: 'post'
          postData: {
            format:'grid'
            prop: '''dictionary.base.applicationBase.productBase.name,
                      dictionary.base.applicationBase.name,
                      dictionary.base.name,
                      key'''
          }
          buttons: [
            {text: c18n.close, click: ->$(@).dialog('close')}
          ]
          colNames: ['Product', 'Application', 'Dictionary', 'Label Key']
          colModel: [
            {name: 'product', index: 'dictionary.base.applicationBase.productBase.name', editable:false, align: 'left'}
            {name: 'application', index: 'dictionary.base.applicationBase.name', editable:false, align: 'left'}
            {name: 'dictionary', index: 'dictionary.base.name', editable:false, align: 'left'}
            {name: 'labelkey', index: 'key', editable:false, align: 'left'}
          ]
        ).setGridParam(datatype: 'json')
        .navGrid "#{hGridId}Pager", {edit: false, add: false, del: false, search: false, view: false},{},{},{}
      open: ()->
        params = $(@).data 'params'
        $('#refText', @).text params.rowData.reference
        $('#refLinkGrid').setGridParam(postData: {text: params.id}).trigger 'reloadGrid'
    )

    $('#ctxTranslationsDialog', '#ctxmng').dialog(
      autoOpen: false
      width: 650
      buttons: [
        {text: c18n.close, click: ->$(@).dialog('close')}
      ]
      open: ()->
        params = $(@).data 'params'
        statusMap ='n': 0, 'i': 1, 't': 2
        $('#transRefText', @).text params.rowData.reference
        $('#transLinkGrid').setGridParam(postData: {text: params.id, status: statusMap[params.colname]}).trigger 'reloadGrid'
      create: ()->
        gridId = 'transLinkGrid'
        hGridId = "##{gridId}"

        $(hGridId, @).jqGrid(
          url: urls.text.translations
          datatype: 'local', mtype: 'post'
          postData: { format:'grid', prop: 'language.name, translation'}
          width: 600, height: 400
          colNames: ['Language', 'Translation']
          colModel: [
            {name: 'language', index: 'language', editable:false, align: 'left'}
            {name: 'tramslation', index: 'translation', editable:false, align: 'left'}
          ]
        ).setGridParam(datatype: 'json')
        .navGrid "#{hGridId}Pager", {edit: false, add: false, del: false, search: false, view: false},{},{},{}
    )

    $('#ctxDifferencesDialog', '#ctxmng').dialog(
      autoOpen: false
      width: 800
      buttons: [
        {text: i18n.takeleft, click: ->}
        {text: i18n.takeright, click: ->}
        {text: c18n.close, click: ->$(@).dialog('close')}
      ]

      create: ()->
        gridId = 'diffLinkGrid'
        hGridId = "##{gridId}"

        $(hGridId, @).jqGrid(
          colNames: ['Language', 'Translation', 'Status', 'Take', 'Translation', 'Status']
          colModel: [
            {name: 'language', index: 'language', width: 140, editable:false, align: 'left'}
            {name: 'tramslationA', index: 'translationA', width: 220, editable:false, align: 'left'}
            {name: 'statusA', index: 'statusA', editable:false, width: 60, align: 'left'}
            {name: 'take', index: 'take', editable:false, width: 40, align: 'left'}
            {name: 'tramslationB', index: 'translationB',width: 220, editable:false, align: 'left'}
            {name: 'statusB', index: 'statusB', editable:false, width: 60, align: 'left'}
          ]
        ).setGridParam(datatype: 'json')
         .navGrid("#{hGridId}Pager", {edit: false, add: false, del: false, search: false, view: false},{},{},{})
         .setGroupHeaders(useColSpanStyle: true, groupHeaders: [
          {startColumnName: 'tramslationA', numberOfColumns: 2, titleText: '<em>XXXXXX (Context A)</em>'}
          {startColumnName: 'tramslationB', numberOfColumns: 2, titleText: '<em>XXXXXX (Context B)</em>'}
         ])
    )

  init()
  ready()

