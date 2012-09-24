define ['jqgrid', 'util', 'require'], ($, util, require)->
  transDetailGrid = $("#transDetailGridList").jqGrid {
  url: 'json/transdetailgrid.json'
  mtype: 'POST', postData: {}, editurl: "", datatype: 'json'
  width: 'auto', height: 200, shrinkToFit: false
  rownumbers: true, loadonce: false # for reload the colModel
  pager: '#transDetailsPager', rowNum: 60, rowList: [10, 20, 30, 60, 120]
  sortname: 'key', sortorder: 'asc', viewrecords: true, gridview: true, multiselect: true, cellEdit: true
  colNames: ['ID', 'Label', 'Max Length', 'Context', 'Reference language', 'Translation', 'Status']
  colModel: [
    {name: 'id', index: 'id', width: 55, align: 'center', hidden: true, frozen: true}
    {name: 'key', index: 'key', width: 100, editable: false, stype: 'select', align: 'left', frozen: true}
    {name: 'maxlen', index: 'maxlen', width: 90, editable: true, align: 'right', frozen: true, search: false}
    {name: 'context', index: 'context', width: 80, align: 'left', frozen: true, search: false}
    {name: 'reflang', index: 'reflang', width: 150, align: 'left', frozen: true, search: false}
    {name: 'trans', index: 'trans', width: 150, align: 'left', search: false}
    {name: 'status', index: 'status', width: 150, align: 'left', editable: true, edittype: 'select',
    editoptions: {value: "0:Not translated;1:In progress;2:Translated"},formatter:'select'
    }
  ]
  afterCreate: (grid)->
    grid.navGrid '#transDetailsPager', {edit: false, add: false, del: false, search: false, view: false}
  }
  transDetailGrid.getGridParam('afterCreate') transDetailGrid

  languageChanged:(param)->
    transDetailGrid = $("#transDetailGridList")
    url="/rest/labels"
    prop="key,maxLength,context,reference,ct.translation,ct.status"
    transDetailGrid.setGridParam({url: url, datatype: "json", postData:{dict:param.dict.id,language:param.language.id,format:'grid',prop:prop}}).trigger("reloadGrid")





