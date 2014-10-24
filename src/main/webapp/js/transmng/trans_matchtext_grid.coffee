define [
  'jqgrid'
  'jqmsgbox'
  'i18n!nls/transmng'
  'i18n!nls/common'
  'dms-util'
  'dms-urls'
], ($, msgbox, i18n, c18n, util, urls)->


  grid = $("#transMatchTextGrid").jqGrid(
    mtype: 'POST', datatype: 'local', url: urls.translations
    width: 'auto', height: 350
    rownumbers: true
    pager: '#transMatchTextGridPager', rowNum: 200, rowList: [100, 200,500]
    viewrecords: true, gridview: true
    multiselect: false
    caption: 'result'
    sortname: '__HSearch_Score', sortorder: 'desc'
    colNames: ['Reference','Translation','Score']
    colModel: [
      {name: 'reference', index: 'reference_forSort', width: 430, editable: false, stype: 'select', align: 'left', sortable: false, frozen: true}
      {name: 'translation', index: 'translation', width: 430, editable: false, align: 'left', frozen: true, sortable: false, search: false}
      {name: 'score', index: '__HSearch_Score', width: 50, editable: false, stype: 'select', align: 'left', sortable: false, frozen: true}
    ]
  )
  .setGridParam('datatype':'json')
  .navGrid('#transMatchTextGridPager', {edit: false, add: false, del: false, search: false, view: false})
