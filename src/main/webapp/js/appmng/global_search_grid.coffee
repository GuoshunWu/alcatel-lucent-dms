define ['jqgrid',   'i18n!nls/appmng', 'dms-util'], ($, i18n, util)->

  pagerId = "#globalSearchResultGridPager"

  #  console?.log "module appmng/history_grid loading."
  lastEditedCell = null

  grid = $('#globalSearchResultGrid').jqGrid(
    url: 'json/dummy.json', mtype: 'post', datatype: 'local'
    width: 800, height: 300
    pager: pagerId
    editurl: ""
    rowNum: 500, rowList: [500, 1000, 2000]
    sortorder: 'asc'
    viewrecords: true
    caption: 'result'
    viewrecords: true
    gridview: true, multiselect: false, cellEdit: false
    grouping: true
    groupingView: {
      groupField: ['dictionary.base.applicationBase.productBase.name', 'dictionary.base.applicationBase.name', 'dictionary.nameVersion']
      groupColumnShow: [false, false, false]
      groupText : ['<b style="color:blue">{0} - {1} Item(s)</b>', '<b style="color:darkblue">{0} - {1} Item(s)</b>', '<b style="color:black">{0} - {1} Item(s)</b>']
      groupCollapse : true
#      groupSummary: [true, true, true]
#      showSummaryOnHide: true
      groupOrder: ['asc', 'asc', 'asc']
    }
    colNames: ['Prod', 'Application', 'Dictionary', 'Label', 'Reference Language', 'Max Length', 'Context', 'T', 'N', 'I']
    colModel: [
      {name: 'dictionary.base.applicationBase.productBase.name', index: 'dictionary.base.applicationBase.productBase.name', width: 50, editable: false, align: 'left'}
      {name: 'dictionary.base.applicationBase.name', index: 'dictionary.base.applicationBase.name', width: 50, editable: false, align: 'left'}
      {name: 'dictionary.nameVersion', index: 'dictionary.nameVersion', width: 300, editable: false, align: 'left'}
      {name: 'key', index: 'key', width: 100, editable: false, align: 'left'
      formatter: (cellvalue, options, rowObject)->'&nbsp;'.repeat(7) + cellvalue
      unformat: (cellvalue, options)->cellvalue.replace(/(&nbsp;)+/,'')
      }
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
      grid.setCaption(i18n.dialog.searchtext.globalcaption.format grid.getRowData().length, grid.getGridParam('postData').text)

      $('a', @).each (index, a)->$(a).before(' ').remove() if '0' == $(a).text()
      $('a', @).css('color', 'blue').click ()->
        param = util.getUrlParams(@href)
        rowData = grid.getRowData(param.id)

        param.key = rowData.key
        param.ref = rowData.reference

        $('#stringSettingsTranslationDialog').data param: param
        $('#stringSettingsTranslationDialog').dialog 'open'
        false
  )
    .setGridParam(datatype: 'json').jqGrid('navGrid', pagerId, {edit: false, add: false, del: false, search: false, view: false})
#    .setGroupHeaders(useColSpanStyle: true, groupHeaders: [
#      {startColumnName: "t", numberOfColumns: 3, titleText: 'Status'}
#    ])


