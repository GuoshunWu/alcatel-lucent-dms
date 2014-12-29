define ['jqgrid', 'dms-util'], ($, util)->

#  console?.log "module appmng/history_grid loading."

  grid = $('#historyGrid').jqGrid(
    url: 'json/dummy.json', mtype: 'post', datatype: 'local'
    width: 800, height: 300
    pager: '#historyGridPager'
    editurl: ""
    rowNum: 20, rowList: [20, 50, 100]
    sortorder: 'desc'
    sortname: 'operationTime'
    viewrecords: true
    gridview: true, multiselect: false, cellEdit: false
    colNames: ['Operation Time', 'Operation Type', 'Task Name', 'Operator']
    colModel: [
      {name: 'operationTime', index: 'operationTime', width: 50, editable: false, align: 'left'}
      {name: 'operationType', index: 'operationType', width: 40, editable: false, align: 'left'}
      {name: 'task.name', index: 'task.name', width: 70, editable: true, align: 'left'}
      {name: 'operator.name', index: 'operator.name', width: 50, editable: true, align: 'left'}
    ]
    gridComplete: ->
  ).setGridParam(datatype: 'json')


