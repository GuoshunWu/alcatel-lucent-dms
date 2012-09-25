define ['jqgrid', 'util', 'require', 'i18n!nls/transmng'], ($, util, require, i18n)->
  transDetailGrid = $("#transDetailGridList").jqGrid {
  url: 'json/transdetailgrid.json'
  mtype: 'POST', postData: {}, editurl: "", datatype: 'json'
  width: 'auto', height: 200, shrinkToFit: false
  rownumbers: true, loadonce: false # for reload the colModel
  pager: '#transDetailsPager', rowNum: 60, rowList: [10, 20, 30, 60, 120]
  sortname: 'key', sortorder: 'asc', viewrecords: true, gridview: true, multiselect: true,
  cellEdit: true, cellurl: 'http://127.0.0.1:2000'
  colNames: ['Label', 'Max Length', 'Context', 'Reference language', 'Translation', 'Status']
  colModel: [
    #    {name: 'id', index: 'ct.id', width: 55, align: 'center', hidden: true, frozen: true}
    {name: 'key', index: 'key', width: 100, editable: false, stype: 'select', align: 'left', frozen: true}
    {name: 'maxlen', index: 'maxlen', width: 90, editable: true, align: 'right', frozen: true, search: false}
    {name: 'context', index: 'context', width: 80, align: 'left', frozen: true, search: false}
    {name: 'reflang', index: 'reflang', width: 150, align: 'left', frozen: true, search: false}
    {name: 'trans', index: 'trans', width: 150, align: 'left', search: false}
    {name: 'status', index: 'status', width: 150, align: 'left', editable: true, edittype: 'select',
    editoptions: {value: "0:#{i18n.trans.nottranslated};1:#{i18n.trans.inprogress};2:#{i18n.trans.translated}"}, formatter: 'select'
    }
    #    {name: 'transid', index: 'transid', width: 55, align: 'center', hidden: true}
  ]
  beforeSubmitCell: (rowid, cellname, value, iRow, iCol)->
  #    dict=$('#translationDetailDialog').data "dict"
  #    language={id:$('#detailLanguageSwitcher').val(), name:$('#detailLanguageSwitcher').find("option:selected").text()}
  #    temp={transId: @getCell rowid, iCol+1}
  afterSubmitCell: (serverresponse, rowid, cellname, value, iRow, iCol)->
    jsonFromServer = eval('(' + serverresponse.responseText + ')')
    [0 == jsonFromServer.status, jsonFromServer.message]

  afterCreate: (grid)->
    grid.navGrid '#transDetailsPager', {edit: false, add: false, del: false, search: false, view: false}
  }
  transDetailGrid.getGridParam('afterCreate') transDetailGrid

  languageChanged: (param)->
    transDetailGrid = $("#transDetailGridList")
    url = "/rest/labels"
    prop = "key,maxLength,context.name,reference,ct.translation,ct.status"
    transDetailGrid.setGridParam({url: url, datatype: "json", postData: {dict: param.dict.id, language: param.language.id, format: 'grid', prop: prop, idprop: 'ct.id'}}).trigger("reloadGrid")





