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
      open:()->
        params = $(@).data 'params'
        console.log params
        toCompareTextId = ($('#compareContextGrid').getGridParam 'postData').text

        diffGrid = $('#diffLinkGrid', @)
        postData = diffGrid.getGridParam 'postData'
        postData.text1 = toCompareTextId
        postData.text2 = params.id
        diffGrid.jqGrid('destroyGroupHeader')

        aContext = $('#contextSelector option:selected', '#ctxmng').text()
        diffGrid.setGroupHeaders(useColSpanStyle: true, groupHeaders: [
          {startColumnName: 'translationA', numberOfColumns: 2, titleText: "<em>Context A: #{aContext}</em>"}
          {startColumnName: 'translationB', numberOfColumns: 2, titleText: "<em>Context B: #{params.rowData.context}</em>"}
        ])
        diffGrid.setCaption($.jgrid.format(i18n.diffRefText, params.rowData.reference)).trigger 'reloadGrid'



      create: ()->
        gridId = 'diffLinkGrid'
        hGridId = "##{gridId}"

        $(hGridId, @).jqGrid(
          url: urls.text.diff_text_translations
          width: 1150
          height: 320

#          postData: {format:'grid', prop: 'a.id,b.id,a.language.name,a.translation,a.status,b.translation,b.status'}
          postData: {format:'grid', prop: 'b.id,a.language.name,a.translation,a.status,b.translation,b.status'}
          datatype: 'local', mtype: 'post'
          colNames: ['TextBId','Language', 'Translation', 'Status', 'Translation', 'Status', 'Take']
          colModel: [
            # text a id is the row id
            {name: 'textb.id', index: 'textb.id', width: 140, editable:false, align: 'left', hidden: true}
            {name: 'language', index: 'language', width: 140, editable:false, align: 'center'}
            {name: 'translationA', index: 'translationA', width: 220, editable:false, align: 'left'}
            {name: 'statusA', index: 'statusA', editable:false, width: 60, align: 'center',
            formatter: 'select', stype: 'select', searchoptions: {value: c18n.translation.values}
            edittype: 'select', editoptions: {value: c18n.translation.values}
            }
            {name: 'translationB', index: 'translationB',width: 220, editable:false, align: 'left'}
            {name: 'statusB', index: 'statusB', editable:false, width: 60, align: 'center',
            formatter: 'select', stype: 'select', searchoptions: {value: c18n.translation.values}
            edittype: 'select', editoptions: {value: c18n.translation.values}
            }
            {name: 'take', index: 'take', editable:false, width: 40, align: 'center'
            unformat: (cellvalue, options, cell) ->cellvalue
            formatter: (cellvalue, options, rowObject)->
              "<a href=\"javascript:void(0);\" title='A' id=\"act_#{options.rowId}_#{options.colModel.name}_#{options.pos}_A\" style='color:blue'>A</a>
               &nbsp;&nbsp;
               <a href=\"javascript:void(0);\" title='B' id=\"act_#{options.rowId}_#{options.colModel.name}_#{options.pos}_B\" style='color:blue'>B</a>
              "
            }
          ]
          gridComplete: ->
            grid = $(@)
            $("a[id^='act']", @).click(->
              alert 'To be implemented.'
            )
        ).setGridParam(datatype: 'json')
         .navGrid("#{hGridId}Pager", {edit: false, add: false, del: false, search: false, view: false},{},{},{})
         .setGroupHeaders(useColSpanStyle: true, groupHeaders: [
          {startColumnName: 'translationA', numberOfColumns: 2, titleText: '<em>XXXXXX (Context A)</em>'}
          {startColumnName: 'translationB', numberOfColumns: 2, titleText: '<em>XXXXXX (Context B)</em>'}
         ])
    )

  init()
  ready()

