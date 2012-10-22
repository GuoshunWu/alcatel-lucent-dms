define ['jqgrid', 'require'], ($, require)->
  dicGrid = $('#dictPreviewStringSettingsGrid').jqGrid {
  url: '',mtype:'post', datatype: 'json'
  width: 700, height: 300
  pager: '#dictPreviewStringSettingsPager'
  editurl: "",cellurl:'/app/deliver-update-label',cellEdit: true
  rowNum: 10, rowList: [10, 20, 30]
  sortname: 'name'
  sortorder: 'asc'
  viewrecords: true
  gridview: true
  colNames: ['Label', 'Reference Language','Max Length','Context','Description']
  colModel: [
    {name: 'key', index: 'key', width: 50, editable: false, align: 'left'}
    {name: 'reference', index: 'reference', width: 40, editable: false, align: 'center'}
    {name: 'maxLength', index: 'maxLength', width: 40, editable: true, align: 'center'}
    {name: 'context', index: 'context.name', width: 50, editable: true, align: 'left'}
    {name: 'description', index: 'description', width: 40, editable: true, align: 'center'}
  ]
  beforeSubmitCell: (rowid, cellname, value, iRow, iCol)->
    postData=$(@).getGridParam 'postData'
    handler:postData.handler,dict:postData.dict

  afterSubmitCell: (serverresponse, rowid, cellname, value, iRow, iCol)->
    jsonFromServer = eval "(#{serverresponse.responseText})"
    [0 == jsonFromServer.status, jsonFromServer.message]
  }
  dicGrid.jqGrid 'navGrid', '#dictPreviewStringSettingsPager', {edit: false, add: false, del: false, search: false, view: false}

