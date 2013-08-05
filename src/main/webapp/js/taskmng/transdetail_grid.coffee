define ['jqgrid'], ($)->
  transDetailGrid = $("#viewDetailGrid").jqGrid(
    url: 'json/transdetailgrid.json'
    mtype: 'POST', postData: {}, editurl: "", datatype: 'local'
    width: $(window).width() * 0.6, height: 200, shrinkToFit: false
    rownumbers: true, loadonce: false # for reload the colModel
    pager: '#ViewDetailPager', rowNum: 100, rowList: [20,50,100,200,500]
    sortname: 'labelKey', sortorder: 'asc', viewrecords: true, gridview: true, multiselect: false,
    cellEdit: true
    colNames: ['Label', 'Max len', 'Context', 'Reference language', 'Translation']
    colModel: [
      {name: 'label', index: 'labelKey', width: 100, editable: false, stype: 'select', align: 'left', frozen: true}
      {name: 'maxlen', index: 'maxLength', width: 90, editable: false, align: 'right', frozen: true, search: false}
      {name: 'context', index: 'text.context.name', width: 80, align: 'left', frozen: true, search: false}
      {name: 'reflang', index: 'text.reference', width: 150, align: 'left', frozen: true, search: false}
      {name: 'trans', index: 'newTranslation', width: 250, align: 'left', frozen: true, search: false}
    ]
    afterCreate: (grid)->
      grid.navGrid '#ViewDetailPager', {edit: false, add: false, del: false, search: false, view: false}
  ).setGridParam(datatype:'json')
  afterCreate = transDetailGrid.getGridParam('afterCreate')
  afterCreate transDetailGrid if afterCreate





