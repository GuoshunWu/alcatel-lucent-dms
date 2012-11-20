define ['jqgrid', 'require'], ($, require)->
  lastEditedCell = null

  dicGrid = $('#stringSettingsGrid').jqGrid {
  url: '', mtype: 'post', datatype: 'json'
  width: 700, height: 300
  pager: '#stringSettingsPager'
  editurl: ""
  rowNum: 10, rowList: [10, 20, 30]
  sortname: 'key'
  sortorder: 'asc'
  viewrecords: true
  gridview: true, multiselect: false, cellEdit: true, cellurl: 'app/update-label'
  colNames: ['Label', 'Reference Language', 'Max Length', 'Context', 'Description']
  colModel: [
    {name: 'key', index: 'key', width: 100, editable: false, align: 'left'}
    {name: 'reference', index: 'reference', width: 100, editable: false, align: 'left'}
    {name: 'maxLength', index: 'maxLength', width: 40, editable: true, classes: 'editable-column', align: 'right'}
    {name: 'context', index: 'context.name', width: 80, editable: true, classes: 'editable-column', align: 'left'}
    {name: 'description', index: 'description', width: 60, editable: true, classes: 'editable-column', align: 'left'}
  ]
  afterEditCell: (rowid, cellname, val, iRow, iCol)->lastEditedCell = {iRow: iRow, iCol: iCol, name: name, val: val}
  }
  dicGrid.jqGrid 'navGrid', '#stringSettingsPager', {edit: false, add: false, del: false, search: false, view: false}, {}, {}, {}

  saveLastEditedCell: ()->dicGrid.saveCell(lastEditedCell.iRow, lastEditedCell.iCol) if lastEditedCell


