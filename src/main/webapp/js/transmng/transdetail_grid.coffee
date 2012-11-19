define ['jqgrid', 'util', 'require', 'i18n!nls/transmng', 'i18n!nls/common'], ($, util, require, i18n, c18n)->
  lastEditedCell = null

  transDetailGrid = $("#transDetailGridList").jqGrid {
  url: 'json/transdetailgrid.json'
  mtype: 'POST', postData: {}, editurl: "", datatype: 'json'
  width: 'auto', height: 200, shrinkToFit: false
  rownumbers: true, loadonce: false # for reload the colModel
  pager: '#transDetailsPager', rowNum: 60, rowList: [10, 20, 30, 60, 120]
  sortname: 'key', sortorder: 'asc', viewrecords: true, gridview: true, multiselect: true
  cellEdit: true, cellurl: 'trans/update-status', ajaxCellOptions: {async: false}
  colNames: ['Label', 'Max Length', 'Context', 'Reference language', 'Translation', 'Status']
  colModel: [
    {name: 'key', index: 'key', width: 100, editable: false, stype: 'select', align: 'left', frozen: true}
    {name: 'maxlen', index: 'maxLength', width: 90, editable: false, align: 'right', frozen: true, search: false}
    {name: 'context', index: 'context.name', width: 80, align: 'left', frozen: true, search: false}
    {name: 'reflang', index: 'reference', width: 150, align: 'left', frozen: true, search: false}
    {name: 'trans', index: 'ct.translation', width: 150, align: 'left', search: false}
    {name: 'transStatus', index: 'ct.status', width: 150, align: 'left', editable: true, classes:'editable-column', search: true,
    edittype: 'select', editoptions: {value: "0:#{i18n.trans.nottranslated};1:#{i18n.trans.inprogress};2:#{i18n.trans.translated}"},
    formatter: 'select',
    stype: 'select', searchoptions: {value: ":#{c18n.all};0:#{i18n.trans.nottranslated};1:#{i18n.trans.inprogress};2:#{i18n.trans.translated}"}
    }
  ]
  afterEditCell: (rowid, cellname, val, iRow, iCol)->lastEditedCell = {iRow: iRow, iCol: iCol, name: name, val: val}
  beforeSubmitCell: (rowid, cellname, value, iRow, iCol)->{type: 'trans'}
  afterSubmitCell: (serverresponse, rowid, cellname, value, iRow, iCol)->
    jsonFromServer = eval('(' + serverresponse.responseText + ')')
    [0 == jsonFromServer.status, jsonFromServer.message]

  afterCreate: (grid)->
    grid.navGrid '#transDetailsPager', {edit: false, add: false, del: false, search: false, view: false}
    grid.filterToolbar {stringResult: true, searchOnEnter: false}
  }
  transDetailGrid.getGridParam('afterCreate') transDetailGrid

  ($("#translationDetailDialog [id^=detailTrans]").button().click ()->
    detailGrid = $("#transDetailGridList")
    selectedRowIds = detailGrid.getGridParam('selarrrow').join(',')
    $.post 'trans/update-status', {type: 'trans', transStatus: @value, id: selectedRowIds}, (json)->
      (alert json.message; return) if json.status != 0
      detailGrid.trigger 'reloadGrid'
  ).parent().buttonset()


  languageChanged: (param)->
    transDetailGrid = $("#transDetailGridList")
    url = "rest/labels"
    prop = "key,maxLength,context.name,reference,ct.translation,ct.status"
    transDetailGrid.setGridParam({url: url, datatype: "json", postData: {dict: param.dict.id, language: param.language.id, format: 'grid', prop: prop, idprop: 'ct.id'}})
#   set status toolbar search to selected column
    map = 'N': '0', 'I': '1', 'T': '2'
    status = param.language.name.split('.')[1]
#    $('#gs_transStatus').val(map[status]).trigger 'change'
#    transDetailGrid.setColProp 'transStatus', {value: "1:#{i18n.trans.inprogress};2:#{i18n.trans.translated}"}
    transDetailGrid.trigger("reloadGrid")

  saveLastEditedCell: ()->
    transDetailGrid.saveCell(lastEditedCell.iRow, lastEditedCell.iCol) if lastEditedCell
    $("#transGrid").trigger 'reloadGrid' if transDetailGrid.getChangedCells('dirty').length > 0
#    or  $("#transGrid").setCell(rowid,'changed column name','new value','new class')




