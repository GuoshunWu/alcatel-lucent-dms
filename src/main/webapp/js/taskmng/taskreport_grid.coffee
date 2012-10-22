define ['jqgrid', 'util', 'require'], ($, util, require)->
  languages = [
    {id: 2, name: 'Arabic'},
    {id: 6, name: 'Chinese (China)'}
    {id: 7, name: 'Catalan (Spain)'}
    {id: 8, name: 'German (Germany)'}
    {id: 9, name: 'French (France)'}
    {id: 10, name: 'Spanish (Spain)'}
    {id: 11, name: 'Italian (Italy)'}
    {id: 12, name: 'Chinese (Taiwan)'}
  ]
  cols = ['T', 'N']
  groupHeaders = []

  colNames = ['Dictionary', 'Total']
  colModel = [
    {name: 'name', index: 'dict', width: 100, editable: false, stype: 'select', align: 'left', frozen: true}
    {name: 'total', index: 'total', width: 90, editable: true, align: 'right', frozen: true}
  ]

  $(languages).each (index, language)->
    $.merge colNames, cols
    $.merge colModel, $(cols).map(
      (index)->name: "#{language.name}.#{@}", sortable: false, index: "s(#{language.id})[#{index}]", width: 40, editable: false, align: 'center').get()
    groupHeaders.push {startColumnName: "#{language.name}.T", numberOfColumns: cols.length, titleText: "<bold>#{language.name}</bold>"}

  transReportGrid = $("#reportGrid").jqGrid {
  url: 'json/transreportgrid.json'
  mtype: 'POST', postData: {}, editurl: "", datatype: 'json'
  width: $(window).width() * 0.6, height: 200, shrinkToFit: false
  pager: '#reportPager', rowNum: 60, rowList: [30, 60, 120]
  sortname: 'key', sortorder: 'asc', viewrecords: true, gridview: true, multiselect: true,
  cellEdit: true, cellurl: ''
  colNames: colNames, colModel: colModel
  }


  transReportGrid.setGroupHeaders {useColSpanStyle: true, groupHeaders: groupHeaders}
  transReportGrid.navGrid '#reportPager', {edit: false, add: false, del: false, search: false, view: false}
  transReportGrid.jqGrid 'setFrozenColumns'








