define ['jqgrid', 'util', 'require'], ($, util, require)->
  common =
    {
    colNames: ['ID', 'Label', 'Max Length', 'Context', 'Reference language', 'Translation', 'Status']
    colModel: [
      {name: 'id', index: 'id', width: 55, align: 'center', hidden: true, frozen: true}
      {name: 'label', index: 'label', width: 100, editable: false, stype: 'select', align: 'left', frozen: true}
      {name: 'maxlen', index: 'maxlen', width: 90, editable: true, align: 'right', frozen: true, search: false}
      {name: 'context', index: 'context', width: 80, align: 'left', frozen: true, search: false}
      {name: 'reflang', index: 'reflang', width: 150, align: 'left', frozen: true, search: false}
      {name: 'trans', index: 'trans', width: 150, align: 'left', search: false}
      {name: 'status', index: 'status', width: 150, align: 'left', editable: true, edittype: 'select',
      editoptions: {value: "1:Translated;2:Not translated;3:In progress"}
      }
    ]
    }

  grid = {
  dictionary:
    {
    colNames: common.colNames.slice(0)
    colModel: common.colModel.slice(0)
    }
  application:
    {
    colNames: common.colNames
    colModel: common.colModel
    }
  }
  transGrid = $("#transDetailGridList").jqGrid {
  url: 'json/transdetailgrid.json'
  mtype: 'POST', postData: {}, editurl: "", datatype: 'json'
  width: 'auto', height: 200, shrinkToFit: false
  rownumbers: true, loadonce: false # for reload the colModel
  pager: '#transDetailsPager', rowNum: 60, rowList: [10, 20, 30, 60, 120]
  sortname: 'base.name', sortorder: 'asc', viewrecords: true, gridview: true, multiselect: true
  cellEdit:true
  colNames: grid.dictionary.colNames, colModel: grid.dictionary.colModel
  afterCreate: (grid)->
    grid.navGrid '#transDetailsPager', {edit: false, add: false, del: false, search: false, view: false}
  }
  transGrid.getGridParam('afterCreate') transGrid



