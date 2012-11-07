define ['jqgrid', 'util', 'require', 'i18n!nls/transmng'], ($, util, require, i18n)->
  transDetailGrid = $("#transDetailGridList").jqGrid {
  url: 'json/transdetailgrid.json'
  mtype: 'POST', postData: {}, editurl: "", datatype: 'json'
  width: 'auto', height: 200, shrinkToFit: false
  rownumbers: true, loadonce: false # for reload the colModel
  pager: '#transDetailsPager', rowNum: 60, rowList: [10, 20, 30, 60, 120]
  sortname: 'key', sortorder: 'asc', viewrecords: true, gridview: true, multiselect: true,
  cellEdit: true, cellurl: 'trans/update-status'
  colNames: ['Label', 'Max Length', 'Context', 'Reference language', 'Translation', 'Status']
  colModel: [
    {name: 'key', index: 'key', width: 100, editable: false, stype: 'select', align: 'left', frozen: true}
    {name: 'maxlen', index: 'maxLength', width: 90, editable: true, align: 'right', frozen: true, search: false}
    {name: 'context', index: 'context.name', width: 80, align: 'left', frozen: true, search: false}
    {name: 'reflang', index: 'reference', width: 150, align: 'left', frozen: true, search: false}
    {name: 'trans', index: 'ct.translation', width: 150, align: 'left', search: false}
    {name: 'transStatus', index: 'ct.status', width: 150, align: 'left', editable: true, edittype: 'select',
    editoptions: {value: "0:#{i18n.trans.nottranslated};1:#{i18n.trans.inprogress};2:#{i18n.trans.translated}"}, formatter: 'select'
    }
  ]
  beforeSubmitCell: (rowid, cellname, value, iRow, iCol)->{type: 'trans'}
  afterSubmitCell: (serverresponse, rowid, cellname, value, iRow, iCol)->
    jsonFromServer = eval('(' + serverresponse.responseText + ')')
    [0 == jsonFromServer.status, jsonFromServer.message]

  afterCreate: (grid)->
    grid.navGrid '#transDetailsPager', {edit: false, add: false, del: false, search: false, view: false}
  }
  transDetailGrid.getGridParam('afterCreate') transDetailGrid

  ($("#translationDetailDialog [id^=detailTrans]").button().click ()->
    detailGrid = $("#transDetailGridList")
    selectedRowIds = detailGrid.getGridParam('selarrrow').join(',')
    $.post '/trans/update-status', {type: 'trans', transStatus: @value, id: selectedRowIds}, (json)->
      (alert json.message; return) if json.status != 0
      detailGrid.trigger 'reloadGrid'
  ).parent().buttonset()


  languageChanged: (param)->
    transDetailGrid = $("#transDetailGridList")
    url = "/rest/labels"
    prop = "key,maxLength,context.name,reference,ct.translation,ct.status"
    transDetailGrid.setGridParam({url: url, datatype: "json", postData: {dict: param.dict.id, language: param.language.id, format: 'grid', prop: prop, idprop: 'ct.id'}}).trigger("reloadGrid")





