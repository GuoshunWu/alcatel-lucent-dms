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
    rowNum: 20, rowList: [10, 20, 30, 40]
    sortorder: 'asc'
    caption: 'result'
    viewrecords: true
    multiselect: false, cellEdit: false

    colNames: ['Application', 'Dictionary', 'Label', 'Reference Language', 'Max Length', 'Context', 'T', 'N', 'I']
    colModel: [
      {name: 'dictionary.base.applicationBase.name', index: 'dictionary.base.applicationBase.name', width: 50, editable: false, align: 'left'}
      {name: 'dictionary.base.name', index: 'dictionary.base.name', width: 300, editable: false, align: 'left'}
      {name: 'key', index: 'key', width: 100, editable: false, align: 'left'}
      {name: 'reference', index: 'reference', width: 300, editable: false, align: 'left'}
      {name: 'maxlen', index: 'maxLength', width: 100, editable: false, align: 'left'}
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
        false
  )
  .setGridParam(datatype: 'json').jqGrid('navGrid', pagerId, {edit: false, add: false, del: false, search: false, view: false})
  .setGroupHeaders(useColSpanStyle: true, groupHeaders: [
      {startColumnName: "t", numberOfColumns: 3, titleText: 'Status'}
  ])



