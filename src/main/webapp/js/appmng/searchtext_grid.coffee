define [
  'jqgrid'
  'dms-util'
  'dms-urls'
  'i18n!nls/common', 'i18n!nls/appmng'
], ($, util, urls, c18n, i18n)->
  pagerId = "#searchTextGridPager"
  grid = $('#searchTextGrid').jqGrid(
    mtype: 'post', datatype: 'local'
    width: 880, height: 300
    pager: pagerId
    rowNum: 10, rowList: [10, 20, 30]
    sortorder: 'asc'
    viewrecords: true
    gridview: true, multiselect: false, cellEdit: false
    colNames: ['Application', 'Dictionary', 'Label', 'Reference Language', 'Max Length', 'Context', 'T', 'N', 'I']
    colModel: [
      {name: 'app', index: 'app', width: 100, editable: false, align: 'left'}
      {name: 'dict', index: 'dict', width: 100, editable: false, align: 'left'}
      {name: 'label', index: 'label', width: 100, editable: false, align: 'left'}
      {name: 'ref', index: 'ref', width: 100, editable: false, align: 'left'}
      {name: 'maxlen', index: 'maxlen', width: 100, editable: false, align: 'left'}
      {name: 'ctx', index: 'ctx', width: 100, editable: false, align: 'left'}

      {name: 't', index: 't', sortable: true, width: 15, align: 'right', formatter: 'showlink'
      formatoptions:
        baseLinkUrl: '#', addParam: encodeURI("&status=2")
      }
      {name: 'n', index: 'n', formatter: 'showlink', sortable: true, width: 15, align: 'right'
      formatoptions:
        baseLinkUrl: '#', addParam: encodeURI("&status=0")
      }
      {name: 'i', index: 'i', formatter: 'showlink', sortable: true, width: 15, align: 'right'
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

        console?.log rowData
        console?.log param

  )
  .setGridParam(datatype: 'json').jqGrid('navGrid', pagerId, {edit: false, add: false, del: false, search: false, view: false})
  .setGroupHeaders(useColSpanStyle: true, groupHeaders: [
      {startColumnName: "t", numberOfColumns: 3, titleText: 'Status'}
  ])



