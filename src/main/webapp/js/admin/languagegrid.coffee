define (require)->
  $ = require 'jqgrid'
  util = require 'util'

  grid = $('#languageGrid').jqGrid(
    datatype: 'local', pager: '#languagePager', mtype: 'post', multiselect: true
    loadtext: 'Loading, please wait...', caption: 'Place holder'
    width: $(window).innerWidth() * 0.95, height: $(window).innerHeight() * 0.6
    colNames: ['LangRefCode', 'Dictionary', 'Version', 'Format', 'Encoding', 'Labels', 'Action']
    colModel: [
      {name: 'langrefcode', index: 'langrefcode', width: 55, align: 'left', hidden: true}
      {name: 'name', index: 'base.name', width: 200, editable: false, align: 'left'}
      {name: 'version', index: 'version', width: 25, editable: true, classes: 'editable-column', edittype: 'select', editoptions: {value: {}}, align: 'left'}
      {name: 'format', index: 'base.format', width: 60, editable: true, edittype: 'select',
      editoptions: {value: "DCT:DCT;Dictionary conf:Dictionary conf;Text properties:Text properties;XML labels:XML labels"},
      align: 'left'}
      {name: 'encoding', index: 'base.encoding', width: 40, editable: true, edittype: 'select',
      editoptions: {value: 'ISO-8859-1:ISO-8859-1;UTF-8:UTF-8;UTF-16LE:UTF-16LE;UTF-16BE:UTF-16BE'}, align: 'left'}
      {name: 'labelNum', index: 'labelNum', width: 20, align: 'right'}
      {name: 'action', index: 'action', width: 80, editable: false, align: 'center'}
    ]
  ).setGridParam('datatype', 'json').jqGrid('navGrid', '#languagePager', {})
