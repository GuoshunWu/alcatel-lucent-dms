define ['jqgrid', 'util', 'require'], ($, util, require)->

  transDetailGrid = $("#viewDetailGrid").jqGrid {
  url: 'json/transdetailgrid.json'
  mtype: 'POST', postData: {}, editurl: "", datatype: 'json'
  width: $(window).width() * 0.6, height: 200, shrinkToFit: false
  rownumbers: true, loadonce: false # for reload the colModel
  pager: '#ViewDetailPager', rowNum: 60, rowList: [10, 20, 30, 60, 120]
  sortname: 'key', sortorder: 'asc', viewrecords: true, gridview: true, multiselect: true,
  cellEdit: true, cellurl: 'http://127.0.0.1:2000'
  colNames: ['Label', 'Max len', 'Context', 'Reference language','Translation']
  colModel: [
    {name: 'label', index: 'label', width: 100, editable: false, stype: 'select', align: 'left', frozen: true}
    {name: 'maxlen', index: 'maxlen', width: 90, editable: true, align: 'right', frozen: true, search: false}
    {name: 'context', index: 'context', width: 80, align: 'left', frozen: true, search: false}
    {name: 'reflang', index: 'reflang', width: 150, align: 'left', frozen: true, search: false}
    {name: 'trans', index: 'trans', width: 250, align: 'left', frozen: true, search: false}
  ]
  afterCreate: (grid)->
    grid.navGrid '#ViewDetailPager', {edit: false, add: false, del: false, search: false, view: false}
  }
  transDetailGrid.getGridParam('afterCreate') transDetailGrid


#    dialogs.viewDetail.dialog 'open'





