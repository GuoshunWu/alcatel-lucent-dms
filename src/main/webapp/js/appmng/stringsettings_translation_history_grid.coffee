define ['jqgrid', 'dms-util', 'i18n!nls/common'], ($, util, c18n)->
  grid = $('#stringSettingsTranslationHistoryGrid').jqGrid(
    url: 'json/dummy.json', mtype: 'post', datatype: 'local'
    width: 800, height: 300
    pager: '#stringSettingsTranslationHistoryGridPager'
    editurl: ""
    rowNum: 20, rowList: [20, 50, 100]
    sortorder: 'asc'
    viewrecords: true
    multiselect: false, cellEdit: false
    colNames: ['Operation Time', 'Operation Type',  'Operator', 'Translation', 'Status', 'Memo']
    colModel: [
      {name: 'operationTime', index: 'operationTime', width: 60, editable: false, align: 'left'}
      {name: 'operationType', index: 'operationType', width: 40, editable: false, align: 'left'
      formatter: 'select', editoptions:{value: c18n.transoptype}
      }
      {name: 'operator.name', index: 'operator.name', width: 50, align: 'left'}
      {name: 'translation', index: 'translation', width: 100, align: 'left'}
      {name: 'status', index: 'status', width: 50, formatter: 'select', editoptions:{value: c18n.translation.values}, align: 'left'}
      {name: 'memo', index: 'memo', width: 70, align: 'left'}
    ]
    gridComplete: ->
  ).setGridParam(datatype: 'json')
  grid


