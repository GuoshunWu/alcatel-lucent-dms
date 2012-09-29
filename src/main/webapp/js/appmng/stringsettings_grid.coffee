define ['jqgrid', 'require'], ($, require)->
  dicGrid = $('#stringSettingsGrid').jqGrid {
  url: '',mtype:'post', datatype: 'json'
  width: 700, height: 300
  pager: '#stringSettingsPager'
  editurl: ""
  rowNum: 10
  rowList: [10, 20, 30]
  sortname: 'key'
  sortorder: 'asc'
  viewrecords: true
  gridview: true
  colNames: ['Label', 'Reference Language','Max Length','Context','Description']
  colModel: [
    {name: 'key', index: 'key', width: 100, editable: true, align: 'left'}
    {name: 'reference', index: 'reference', width: 100, editable: true, align: 'left'}
    {name: 'maxLength', index: 'maxLength', width: 40, editable: true, align: 'left'}
    {name: 'context', index: 'context.name', width: 80, editable: true, align: 'left'}
    {name: 'description', index: 'description', width: 60, editable: true, align: 'left'}
  ]
  }
  dicGrid.jqGrid('navGrid', '#stringSettingsPager', {edit: false, add: true, del: false, search: false, view: false})


