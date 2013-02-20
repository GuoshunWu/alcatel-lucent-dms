define ['jqgrid', 'require'], ($, require)->
  lastEditedCell = null
  util = require 'util'

  grid = $('#historyGrid').jqGrid(
    url: 'json/dummy.json', mtype: 'post', datatype: 'local'
    width: 800, height: 300
    pager: '#historyGridPager'
    editurl: ""
    rowNum: 10, rowList: [10, 20, 30]
    sortorder: 'asc'
    viewrecords: true
    gridview: true, multiselect: false, cellEdit: false
    colNames: ['Operation Time', 'Operation Type', 'Task Name', 'Operator']
    colModel: [
      {name: 'operationTime', index: 'operationTime', width: 50, editable: false, align: 'left'}
      {name: 'operationType', index: 'operationType', width: 40, editable: false, align: 'left'}
      {name: 'task.name', index: 'task.name', width: 70, editable: true, classes: 'editable-column', align: 'left'}
      {name: 'operator.name', index: 'operator.name', width: 50, editable: true, classes: 'editable-column', align: 'left'}
    ]
    gridComplete: ->


    afterEditCell: (rowid, cellname, val, iRow, iCol)->lastEditedCell = {iRow: iRow, iCol: iCol, name: name, val: val}
  ).setGridParam(datatype: 'json')
  saveLastEditedCell: ()->grid.saveCell(lastEditedCell.iRow, lastEditedCell.iCol) if lastEditedCell


