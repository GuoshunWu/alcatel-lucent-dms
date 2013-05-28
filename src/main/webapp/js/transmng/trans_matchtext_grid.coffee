define [
  'jqgrid'
  'jqmsgbox'
  'i18n!nls/transmng'
  'i18n!nls/common'
  'dms-util'
  'dms-urls'
], ($, msgbox, i18n, c18n, util, urls)->


  grid = $("#transMatchTextGrid").jqGrid(
    mtype: 'POST', datatype: 'local'
    width: 'auto', height: 300
    rownumbers: true
    pager: '#transSearchTextGridPager', rowNum: 60, rowList: [10, 20, 30, 60, 120]
    viewrecords: true, gridview: true, multiselect: false
    caption: 'result'
    colNames: ['Reference','Translation','Score']
    colModel: [
      {name: 'reference', index: 'reference', width: 50, editable: false, stype: 'select', align: 'left', frozen: true}
      {name: 'translation', index: 'translation', width: 150, editable: false, align: 'left', frozen: true, search: false}
      {name: 'score', index: 'score', width: 150, editable: false, stype: 'select', align: 'left', frozen: true}
    ]
  )
  .setGridParam('datatype':'json')
  .navGrid('#transMatchTextGridPager', {edit: false, add: false, del: false, search: false, view: false})
