define ['jqgrid', 'util', 'require'], ($, util, require)->
  transDetailGrid = $("#taskGrid").jqGrid {
  url: 'json/transdetailgrid.json'
  mtype: 'POST', postData: {}, editurl: "", datatype: 'json'
  width: 'auto', height: 200, shrinkToFit: false
  rownumbers: true, loadonce: false # for reload the colModel
  pager: '#taskPager', rowNum: 60, rowList: [10, 20, 30, 60, 120]
  sortname: 'key', sortorder: 'asc', viewrecords: true, gridview: true, multiselect: true,
  cellEdit: true, cellurl: 'http://127.0.0.1:2000'
  colNames: ['Task', 'Create time', 'Last upload time', 'Actions']
  colModel: [
    {name: 'task', index: 'task', width: 100, editable: false, stype: 'select', align: 'left', frozen: true}
    {name: 'createtime', index: 'createtime', width: 90, editable: true, align: 'right', frozen: true, search: false}
    {name: 'lastuploadtime', index: 'lastuploadtime', width: 80, align: 'left', frozen: true, search: false}
    {name: 'actions', index: 'reflang', width: 150, align: 'left', frozen: true, search: false}
  ]
  afterCreate: (grid)->
    grid.navGrid '#taskPager', {edit: false, add: false, del: false, search: false, view: false}
  }
  transDetailGrid.getGridParam('afterCreate') transDetailGrid




