define [
  'jqgrid'
  'dms-urls'
  'i18n!nls/admin'
], ($, urls, i18n)->

  afterSubmit = (response, postdata)->
    jsonFromServer = $.parseJSON response.responseText
    [jsonFromServer.status == 0, jsonFromServer.message]

  gridId = 'charsetGrid'
  hGridId = '#' + gridId
  pagerId = gridId + '_' + 'Pager'
  hPagerId = '#' + pagerId

  grid = $(hGridId).after("<div id='#{pagerId}'>").jqGrid(
    url: urls.charsets
    postData: {prop: 'name', format: 'grid'}, datatype: 'json'
    pager: hPagerId, mtype: 'post', multiselect: true, rowNum: 15, rowList: [15, 30, 60]
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
  ).jqGrid('navGrid', hPagerId, {search: false, edit: false}, {
    #      paramAdd
    mtype: 'post', afterSubmit: afterSubmit, ajaxEditOptions: {dataType: 'json'}, closeAfterAdd: true
    beforeShowForm: (form)-> }, {
    #      paramDel
    mtype: 'post', afterSubmit: afterSubmit, ajaxDelOptions: {dataType: 'json'}
    beforeShowForm: (form)->})