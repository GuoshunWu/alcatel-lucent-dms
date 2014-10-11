define [
  'jqgrid'
  'blockui'
  'jqmsgbox'
  'jqueryui'

  'i18n!nls/common'
  'dms-util'
  'dms-urls'

], ($, blockui, msgbox, ui, c18n, util, urls)->
  gridId = 'dictValidationGrid'
  hGridId = '#' + gridId
  pagerId = gridId + 'Pager'
  hPagerId = '#' + pagerId

  grid = $(hGridId).after($("<div>").attr("id", pagerId)).jqGrid(
    url: urls.dict_validation
    datatype: 'local', mtype: 'post'
    postData: { format:'grid', prop: 'code,message'}
    rowNum: 20, rowList: [20, 50, 100, 200]
    viewrecords: true
    gridview: true,
    pager: pagerId,
    width: 810, height: 400
    colNames: ['Type', 'Message']
    colModel: [
      {name: 'code', width: 200, index: 'code', editable:false, align: 'left'}
      {name: 'message', width: 610, index: 'message', editable:false, align: 'left'}
    ]
  ).setGridParam(datatype: 'json')
  .navGrid hPagerId, {edit: false, add: false, del: false, search: false, view: false},{},{},{}

  grid: grid



