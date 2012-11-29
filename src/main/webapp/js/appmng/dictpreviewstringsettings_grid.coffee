define ['jqgrid', 'require'], ($, require)->
  lastEditedCell = null

  dicGrid = $('#dictPreviewStringSettingsGrid').jqGrid {
  url: 'json/dummy.json', mtype: 'post', datatype: 'json'
  width: 700, height: 300
  pager: '#dictPreviewStringSettingsPager'
  editurl: "", cellurl: 'app/deliver-update-label', cellEdit: true
  rowNum: 10, rowList: [10, 20, 30]
  sortname: 'name'
  sortorder: 'asc'
  viewrecords: true
  gridview: true
  colNames: ['Label', 'Reference Language', 'Max Length', 'Context', 'Description']
  colModel: [
    {name: 'key', index: 'key', width: 50, editable: false, align: 'left'}
    {name: 'reference', index: 'reference', width: 160, editable: false, align: 'left'}
    {name: 'maxLength', index: 'maxLength', width: 35, editable: true, classes: 'editable-column', align: 'right'
    editrules:
      {custom: true, custom_func: (value, colname)->
        return [false, 'Invalid max length format.'] if !/^\d+(\s*,?\s*\d+\s*)*$/.test(value)
        [true, '']
      }}
    {name: 'context', index: 'context.name', width: 25, classes: 'editable-column', editable: true, align: 'left'}
    {name: 'description', index: 'description', width: 40, classes: 'editable-column', editable: true, align: 'left'}
  ]

  afterEditCell: (rowid, cellname, val, iRow, iCol)->lastEditedCell = {iRow: iRow, iCol: iCol, name: name, val: val}
  beforeSubmitCell: (rowid, cellname, value, iRow, iCol)->
    postData = $(@).getGridParam 'postData'
    handler: postData.handler, dict: postData.dict

  afterSubmitCell: (serverresponse, rowid, cellname, value, iRow, iCol)->
    jsonFromServer = eval "(#{serverresponse.responseText})"
    success = 0 == jsonFromServer.status
    $('#dictListPreviewGrid').trigger 'reloadGrid' if success
    [success, jsonFromServer.message]
  }
  dicGrid.jqGrid 'navGrid', '#dictPreviewStringSettingsPager', {edit: false, add: false, del: false, search: false, view: false}

  saveLastEditedCell: ()->dicGrid.saveCell(lastEditedCell.iRow, lastEditedCell.iCol) if lastEditedCell

