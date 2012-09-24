define ['jqgrid', 'require'], ($, require)->
  localIds = {
  dic_grid: '#dictionaryGridList'
  }
  dicGrid = $(localIds.dic_grid).jqGrid ({
  url: ''
  datatype: 'json'
  width: 1000
  height: 350
  pager: '#dictPager'
  editurl: "app/create-or-add-application"
  rowNum: 10
  rowList: [10, 20, 30]
  sortname: 'base.name'
  sortorder: 'asc'
  viewrecords: true
  gridview: true
  caption: 'Dictionary for Application'
  colNames: ['ID', 'Dictionary', 'Version', 'Format', 'Encoding', 'Labels', 'Action']
  colModel: [
    {name: 'id', index: 'id', width: 55, align: 'center', hidden: true}
    {name: 'name', index: 'base.name', width: 200, editable: true, align: 'left'}
    {name: 'version', index: 'version', width: 25, editable: true, align: 'center'}
    {name: 'format', index: 'base.format', width: 20, editable: true, align: 'center'}
    {name: 'encoding', index: 'base.encoding', width: 40, editable: true, align: 'center'}
    {name: 'labelNum', index: 'labelNum', width: 20, align: 'center'}
    {name: 'action', index: 'action', width: 90, editable: true, align: 'center'}
  ]
  })
  dicGrid.jqGrid('navGrid', '#dictPager', {edit: false, add: true, del: false, search: false, view: false})

  appChanged:(app)->
    url = "rest/dict?app=#{app.id}&format=grid&prop=id,base.name,version,base.format,base.encoding,labelNum"
    dicGrid.setGridParam({url: url, datatype: "json"}).trigger("reloadGrid")
    appBase = require('appmng/apptree').getSelected()
    dicGrid.setCaption "Dictionary for Application #{appBase.text} version #{app.version}"