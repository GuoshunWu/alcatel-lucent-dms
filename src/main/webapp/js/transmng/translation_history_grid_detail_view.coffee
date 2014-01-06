define ['jqgrid', 'dms-util', 'i18n!nls/common'], ($, util, c18n)->

#  console?.log "module appmng/history_grid loading."

  lastEditedCell = null

  grid = $('#detailViewTranslationHistoryGrid').jqGrid(
    url: 'json/dummy.json', mtype: 'post', datatype: 'local'
    width: 800, height: 300
    pager: '#detailViewTranslationHistoryGridPager'
    editurl: ""
    rowNum: 10, rowList: [10, 20, 30]
    sortorder: 'asc'
    viewrecords: true
    gridview: true, multiselect: false, cellEdit: false
    colNames: ['Operation Time', 'Operation Type', 'Memo', 'Operator']
    colModel: [
      {name: 'operationTime', index: 'operationTime', width: 50, editable: false, align: 'left'}
      {name: 'operationType', index: 'operationType', width: 40, editable: false, align: 'left'
      formatter: 'select', editoptions:{value: c18n.transoptype}
      }
      {name: 'memo', index: 'memo', width: 70, editable: true, align: 'left'}
      {name: 'operator.name', index: 'operator.name', width: 50, editable: true, align: 'left'}
    ]
    gridComplete: ->


    afterEditCell: (rowid, cellname, val, iRow, iCol)->lastEditedCell = {iRow: iRow, iCol: iCol, name: name, val: val}
  ).setGridParam(datatype: 'json')
  saveLastEditedCell: ()->grid.saveCell(lastEditedCell.iRow, lastEditedCell.iCol) if lastEditedCell

  grid


