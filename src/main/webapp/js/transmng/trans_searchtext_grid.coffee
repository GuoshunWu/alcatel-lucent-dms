define [
  'jqgrid'
  'jqmsgbox'
  'i18n!nls/transmng'
  'i18n!nls/common'
  'dms-util'
  'dms-urls'
], ($, msgbox, i18n, c18n, util, urls)->


  grid = $("#transSearchTextGrid").jqGrid(
    mtype: 'POST', datatype: 'local'
    width: 'auto', height: 300
    rownumbers: true
    pager: '#transSearchTextGridPager', rowNum: 60, rowList: [10, 20, 30, 60, 120]
    viewrecords: true, gridview: true, multiselect: false
    caption: 'result'
    colNames: ['Application','Dictionary','Label', 'Max Length', 'Context', 'Reference language', 'Translation', 'Status','TransId']
    colModel: [
      {name: 'app', index: 'app.name', width: 50, editable: false, stype: 'select', align: 'left', frozen: true}
      {name: 'dict', index: 'dictionary.base.name', width: 150, editable: false, align: 'left', frozen: true, search: false}
      {name: 'key', index: 'key', width: 150, editable: false, stype: 'select', align: 'left', frozen: true}
      {name: 'maxlen', index: 'maxLength', width: 70, editable: false, align: 'right', frozen: true, search: false}
      {name: 'context', index: 'context.name', width: 80, align: 'left', frozen: true, search: false}
      {name: 'reference', index: 'reference', width: 160, align: 'left', frozen: true, search: false}
      {name: 'translation', index: 'ct.translation', width: 160, align: 'left', edittype:'textarea', search: false}
      {name: 'transStatus', index: 'ct.status', width: 100, align: 'left', editable: false,search: true,
      edittype: 'select', editoptions: {value: "0:#{i18n.trans.nottranslated};1:#{i18n.trans.inprogress};2:#{i18n.trans.translated}"},
      formatter: 'select',
      stype: 'select', searchoptions: {value: ":#{c18n.all};0:#{i18n.trans.nottranslated};1:#{i18n.trans.inprogress};2:#{i18n.trans.translated}"}
      }
      {name: 'transId', index: 'ct.id', width: 150, align: 'left', hidden:true, search: false}
    ]
  )
  .setGridParam('datatype':'json')
  .navGrid('#transSearchTextGridPager', {edit: false, add: false, del: false, search: false, view: false})