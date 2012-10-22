define ['jqgrid', 'util', 'require', 'taskmng/dialogs'], ($, util, require, dialogs)->
  handlers =
    'Download': (param)->
      alert 'download'
      console.log param
    'History…': (param)->
      alert 'History…'
      console.log param
    'End': (param)->
      alert 'End'
      console.log param
    'X': (param)->
      alert 'X'
      console.log param

  taskGrid = $("#taskGrid").jqGrid {
  url: 'json/taskgrid.json'
  mtype: 'POST', postData: {}, editurl: "", datatype: 'json'
  width: $(window).width() * 0.95, height: 400, shrinkToFit: false
  rownumbers: true, loadonce: false # for reload the colModel
  pager: '#taskPager', rowNum: 60, rowList: [10, 20, 30, 60, 120]
  sortname: 'key', sortorder: 'asc', viewrecords: true, gridview: true, multiselect: true,
  cellEdit: true, cellurl: 'http://127.0.0.1:2000'
  colNames: ['Task', 'Create time', 'Last upload time', 'Actions']
  colModel: [
    {name: 'task', index: 'task', width: 250, editable: false, stype: 'select', align: 'left', frozen: true}
    {name: 'createtime', index: 'createtime', width: 90, editable: false, align: 'right', frozen: true, search: false}
    {name: 'lastuploadtime', index: 'lastuploadtime', width: 80, align: 'left', frozen: true, search: false}
    {name: 'actions', index: 'reflang', width: 240, align: 'center', frozen: true, search: false}
  ]
  beforeProcessing: (data, status, xhr)->
  #   add actions
    actIndex = $(@).getGridParam('colNames').indexOf('Actions')
    --actIndex
    --actIndex if $(@).getGridParam('multiselect')

    actions = []
    actions.push k for k,v of handlers

    $(data.rows).each (index)->
      rowData = @
      @cell[actIndex] = $(actions).map((index)->
        "<A id='action_#{@}_#{rowData.id}_#{actIndex}' style='color:blue'title='#{@}' href=#  >#{@}</A>"
      ).get().join( '&nbsp;&nbsp;&nbsp;&nbsp;')
  gridComplete: ->
    grid = $(@)

    $('a[id^=action_]', @).click ()->
      [a, action, rowid, col]=@id.split('_')
      rowData = grid.getRowData(rowid)
      delete rowData.actions
      rowData.id = rowid

      handlers[action] rowData


  afterCreate: (grid)->
    grid.navGrid '#taskPager', {edit: false, add: false, del: false, search: false, view: false}
  }
  taskGrid.getGridParam('afterCreate') taskGrid

  #    test button
  $('#transReport').button({}).click ()->dialogs.transReport.dialog 'open'
  $('#viewDetail').button({}).click ()->dialogs.viewDetail.dialog 'open'






