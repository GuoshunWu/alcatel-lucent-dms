define ['jqgrid', 'dms-util'], ($, util)->

  pagerId = "#globalSearchResultGridPager"

  #  console?.log "module appmng/history_grid loading."
  lastEditedCell = null

  grid = $('#globalSearchResultGrid').jqGrid(
    url: 'json/dummy.json', mtype: 'post', datatype: 'local'
    width: 800, height: 300
    pager: pagerId
    editurl: ""
    rowNum: 40, rowList: [20, 40, 80, 160]
    sortorder: 'asc'
    viewrecords: true
    caption: 'result'
    viewrecords: true
    gridview: true, multiselect: false, cellEdit: false
    grouping: true
    groupingView: {
      groupField: ['prod.base.name'
      , 'app.name', 'dictionary.base.name'
      ]
    }
    colNames: ['Prod', 'Prod ver', 'Application', 'App ver', 'Dictionary', 'Dict ver', 'Label', 'Reference Language', 'Max Length', 'Context', 'T', 'N', 'I']
    colModel: [
      {name: 'prod.base.name', index: 'prod.base.name', width: 50, editable: false, align: 'left'}
      {name: 'prod.version', index: 'prod.version', width: 50, editable: false, align: 'left'}

      {name: 'app.name', index: 'app.name', width: 50, editable: false, align: 'left'}
      {name: 'app.version', index: 'app.version', width: 50, editable: false, align: 'left'}
      {name: 'dictionary.base.name', index: 'dictionary.base.name', width: 300, editable: false, align: 'left'}
      {name: 'dict.version', index: 'dictionary.version', width: 50, editable: false, align: 'left'}
      {name: 'key', index: 'key', width: 100, editable: false, align: 'left'}
      {name: 'reference', index: 'reference', width: 300, editable: false, align: 'left'}
      {name: 'maxlen', index: 'maxLength', width: 50, editable: false, align: 'left'}
      {name: 'ctx', index: 'context.name', width: 100, editable: false, align: 'left'}

      {name: 't', index: 't', sortable: true, width: 18, align: 'right', formatter: 'showlink'
      formatoptions:
        baseLinkUrl: '#', addParam: encodeURI("&status=2")
      }
      {name: 'n', index: 'n', formatter: 'showlink', sortable: true, width: 18, align: 'right'
      formatoptions:
        baseLinkUrl: '#', addParam: encodeURI("&status=0")
      }
      {name: 'i', index: 'i', formatter: 'showlink', sortable: true, width: 18, align: 'right'
      formatoptions:
        baseLinkUrl: '#', addParam: encodeURI("&status=1")
      }

    ]
    gridComplete: ->
      grid = $(@)
      $('a', @).each (index, a)->$(a).before(' ').remove() if '0' == $(a).text()
      $('a', @).css('color', 'blue').click ()->
        param = util.getUrlParams(@href)
        rowData = grid.getRowData(param.id)

        param.key = rowData.key
        param.ref = rowData.reference

        $('#stringSettingsTranslationDialog').data param: param
        $('#stringSettingsTranslationDialog').dialog 'open'

  )
    .setGridParam(datatype: 'json').jqGrid('navGrid', pagerId, {edit: false, add: false, del: false, search: false, view: false})
#    .setGroupHeaders(useColSpanStyle: true, groupHeaders: [
#      {startColumnName: "t", numberOfColumns: 3, titleText: 'Status'}
#    ])


