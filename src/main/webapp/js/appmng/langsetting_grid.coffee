define ['jqgrid', 'require'], ($, require)->
  dicGrid = $('#languageSettingGrid').jqGrid {
  url: '',mtype:'post', datatype: 'json'
  width: 300, height: 'auto'
  pager: '#langSettingPager'
  editurl: ""
  rowNum: 10
  rowList: [10, 20, 30]
  sortname: 'name'
  sortorder: 'asc'
  viewrecords: true
  gridview: true
  colNames: ['Language', 'Code', 'Charset']
  colModel: [
    {name: 'name', index: 'name', width: 50, editable: true, align: 'left'}
    {name: 'code', index: 'code', width: 40, editable: true, align: 'center'}
    {name: 'charset', index: 'charset', width: 40, editable: true, align: 'center'}
  ]
  }
  dicGrid.jqGrid('navGrid', '#langSettingPager', {edit: false, add: true, del: false, search: false, view: false})


