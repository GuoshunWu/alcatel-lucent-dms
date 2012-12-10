define (require)->
  $ = require 'jqgrid'
  util = require 'util'

  grid = $('#languageGrid').jqGrid(
    url: 'rest/languages', postData: {prop: 'name,defaultCharset', format: 'grid'}, datatype: 'json', mtype: 'post'
    pager: '#languagePager'
    multiselect: true
    loadtext: 'Loading, please wait...', caption: 'Place holder'
    width: $(window).innerWidth() * 0.95, height: $(window).innerHeight() * 0.6
    colNames: ['Name', 'Default Charset']
    colModel: [
      {name: 'name', index: 'base.name', width: 100, classes: 'editable-column', editable: false, align: 'left'}
      {name: 'defaultCharset', index: 'defaultCharset', width: 100, editable: true, classes: 'editable-column', edittype: 'select', editoptions: {value: {}}, align: 'left'}
    ]
  ).jqGrid('navGrid', '#languagePager', {})
