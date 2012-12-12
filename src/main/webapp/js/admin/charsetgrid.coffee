define (require)->
  $ = require 'jqgrid'
  util = require 'util'
  afterSubmit = (response, postdata)->
    jsonFromServer = $.parseJSON response.responseText
    [jsonFromServer.status == 0, jsonFromServer.message]

  grid = $('#charsetGrid').jqGrid(
    url: 'rest/charsets', postData: {prop: 'name', format: 'grid'}, datatype: 'json'
    pager: '#charsetPager', mtype: 'post', multiselect: true, rowNum: 15, rowList: [15, 30, 60]
    loadtext: 'Loading, please wait...', caption: 'Place holder'
    width: $(window).innerWidth() * 0.95, height: $(window).innerHeight() * 0.6
    cellurl: 'admin/charset', cellEdit: true, afterSubmitCell: (serverresponse, rowid, cellname, value, iRow, iCol)->
      jsonFromServer = $.parseJSON serverresponse.responseText
      [jsonFromServer.status == 0, jsonFromServer.message]

    editurl: 'admin/charset'
    colNames: ['Name']
    colModel: [
      {name: 'name', index: 'name', editable: true, classes: 'editable-column', align: 'left',
      #      formoptions:{elmprefix:"<span style='color:red'>*</span>"}
      editrules: {required: true}}
    ]
  ).jqGrid('navGrid', '#charsetPager', {}, {
    #      paramAdd
    mtype: 'post', afterSubmit: afterSubmit, ajaxEditOptions: {dataType: 'json'}, closeAfterAdd: true
    beforeShowForm: (form)-> }, {
    #      paramDel
    mtype: 'post', afterSubmit: afterSubmit, ajaxDelOptions: {dataType: 'json'}
    beforeShowForm: (formid)->})