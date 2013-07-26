define [
  'jqgrid'
  'dms-util'
  'dms-urls'
  'i18n!nls/admin'
], ($, util, urls, i18n)->

  afterSubmit = (response, postdata)->
    jsonFromServer = $.parseJSON response.responseText
    [jsonFromServer.status == 0, jsonFromServer.message]

  grid = $('#charsetGrid').jqGrid(
    url: urls.charsets, postData: {prop: 'name', format: 'grid'}, datatype: 'json'
    pager: '#charsetPager', mtype: 'post', multiselect: true, rowNum: 15, rowList: [15, 30, 60]
    loadtext: 'Loading, please wait...', caption: i18n.charsetgrid.caption
    autowidth: true
    height: '100%'
    cellurl: urls.charset.update, cellEdit: true
    afterSubmitCell: (serverresponse, rowid, cellname, value, iRow, iCol)->
      jsonFromServer = $.parseJSON serverresponse.responseText
      [jsonFromServer.status == 0, jsonFromServer.message]

    editurl: urls.charset.update
    colNames: ['Name']
    colModel: [
      {name: 'name', index: 'name', editable: true, classes: 'editable-column', align: 'left',
      #      formoptions:{elmprefix:"<span style='color:red'>*</span>"}
      editrules: {required: true}}
    ]
  ).jqGrid('navGrid', '#charsetPager', {search: false, edit: false}, {
    #      paramAdd
    mtype: 'post', afterSubmit: afterSubmit, ajaxEditOptions: {dataType: 'json'}, closeAfterAdd: true
    beforeShowForm: (form)-> }, {
    #      paramDel
    mtype: 'post', afterSubmit: afterSubmit, ajaxDelOptions: {dataType: 'json'}
    beforeShowForm: (form)->})