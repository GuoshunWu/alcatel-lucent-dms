define (require)->
  $ = require 'jqgrid'
  util = require 'util'

  grid = $('#charsetGrid').jqGrid(
    url: 'rest/charsets', postData: {prop: 'name', format: 'grid'}, datatype: 'json'
    pager: '#charsetPager', mtype: 'post', multiselect: true
    loadtext: 'Loading, please wait...', caption: 'Place holder'
    width: $(window).innerWidth() * 0.95, height: $(window).innerHeight() * 0.6

    colNames: ['Name']
    colModel: [
      {name: 'name', index: 'name', classes: 'editable-column',align: 'left'}
    ]
  ).jqGrid('navGrid', '#charsetPager', {})