define ['jqgrid', 'require'], ($, require)->
  dicGrid = $('#dictPreviewGrid').jqGrid {
  url: '',mtype:'post', datatype: 'json'
  width: 300, height: 'auto'
  pager: '#dictPreviewPager'
  editurl: ""
  rowNum: 10
  rowList: [10, 20, 30]
  sortname: 'name'
  sortorder: 'asc'
  viewrecords: true
  gridview: true
  colNames: ['Label', 'Reference Language','Max Length','Context','Description', 'Status']
  colModel: [
    {name: 'key', index: 'key', width: 50, editable: true, align: 'left'}
    {name: 'reference', index: 'reference', width: 40, editable: true, align: 'center'}
    {name: 'maxLength', index: 'maxLength', width: 40, editable: true, align: 'center'}
    {name: 'context', index: 'context.name', width: 50, editable: true, align: 'left'}
    {name: 'description', index: 'description', width: 40, editable: true, align: 'center'}
    {name: 'status', index: 'status', width: 40, editable: true, align: 'center'}
  ]
  }
  dicGrid.jqGrid('navGrid', '#dictPreviewPager', {edit: false, add: true, del: false, search: false, view: false})


