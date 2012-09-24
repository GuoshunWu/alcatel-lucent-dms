define ['jqgrid', 'require'], ($, require)->
  localIds = {
  dic_grid: '#dictionaryGridList'
  }
  dicGrid = $(localIds.dic_grid).jqGrid ({
  url: ''
  datatype: 'json'
  width: 700
  height: 350
  pager: '#dictPager'
  editurl: "app/create-or-add-application"
  rowNum: 10
  rowList: [10, 20, 30]
  sortname: 'name'
  sortorder: 'asc'
  viewrecords: true
  gridview: true
  caption: 'Dictionary for Application'
  colNames: ['ID', 'Dictionary', 'Version', 'Format', 'Encoding', 'Labels', 'Action']
  colModel: [
    {name: 'id', index: 'id', width: 55, align: 'center', hidden: true}
    {name: 'name', index: 'name', width: 100, editable: true, align: 'center'}
    {name: 'version', index: 'version', width: 90, editable: true, align: 'center'}
    {name: 'format', index: 'format', width: 90, editable: true, align: 'center'}
    {name: 'encoding', index: 'encoding', width: 90, editable: true, align: 'center'}
    {name: 'labelNum', index: 'labelNum', width: 80, align: 'center'}
    {name: 'action', index: 'action', width: 90, editable: true, align: 'center'}
  ]
  })
  dicGrid.jqGrid('navGrid', '#dictPager', {edit: false, add: true, del: false, search: false, view: false})