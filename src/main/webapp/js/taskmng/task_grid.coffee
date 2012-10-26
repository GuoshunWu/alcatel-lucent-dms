define ['jqgrid', 'util', 'require', 'taskmng/dialogs', 'i18n!nls/taskmng', 'i18n!nls/common', 'blockui', 'jqmsgbox'], ($, util, require, dialogs, i18n, c18n)->
  handlers =
    'Download': (param)->
      console.log param
      filename = "#{$('#appDispAppName').text()}_#{$('#selAppVersion option:selected').text()}_#{new Date().format 'yyyyMMdd_hhmmss'}.zip"
      console.log filename
      param.name
      param.id
#      $.blockUI css: {backgroundColor: '#fff'}, overlayCSS: {opacity: 0.2}
#      $.post '/task/generate-task-files', {dicts: dicts.join(','), filename: filename}, (json)->
#        $.unblockUI()
#        ($.msgBox json.message, null, {title: c18n.error};return) if json.status != 0


    #      downloadForm = $('#downloadDict')
    #      $('#fileLoc', downloadForm).val json.fileLoc
    #      downloadForm.submit()


    'History…': (param)->
      alert 'History…'
      console.log param
    'Close': (param)->
      return if param.status == '1'
      $.blockUI css: {backgroundColor: '#fff'}, overlayCSS: {opacity: 0.2}
      $.post '/task/close-task', {id: param.id}, (json)->
        $.unblockUI()
        if json.status != 0
          $.msgBox json.message, null, {title: c18n.error}
          return
        $("#taskGrid").trigger 'reloadGrid'

  taskGrid = $("#taskGrid").jqGrid {
  url: 'json/taskgrid.json'
  mtype: 'POST', postData: {}, editurl: "", datatype: 'json'
  width: $(window).width() * 0.95, height: 400, shrinkToFit: false
  rownumbers: true, loadonce: false # for reload the colModel
  pager: '#taskPager', rowNum: 60, rowList: [10, 20, 30, 60, 120]
  sortname: 'name', sortorder: 'asc', viewrecords: true, gridview: true, multiselect: true,
  cellEdit: true, cellurl: 'http://127.0.0.1:2000'
  colNames: ['Task', 'Create time', 'Last upload time', 'Status' , 'Actions']
  colModel: [
    {name: 'name', index: 'name', width: 250, editable: false, stype: 'select', align: 'left'}
    {name: 'createTime', index: 'createTime', width: 150, editable: false, align: 'right' }
    {name: 'lastUpdateTime', index: 'lastUpdateTime', width: 150, align: 'left'}
    {name: 'status', index: 'status', width: 80, align: 'left', editable: false, edittype: 'select',
    editoptions: {value: "0:#{i18n.task.open};1:#{i18n.task.closed}"}, formatter: 'select'}
    {name: 'actions', index: 'actions', width: 240, align: 'center'}
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
      @cell[actIndex] = $(actions).map(
        (index)->
          "<A id='action_#{@}_#{rowData.id}_#{actIndex}' style='color:blue'title='#{@}' href=#  >#{@}</A>"
      ).get().join('&nbsp;&nbsp;&nbsp;&nbsp;')
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

  productVersionChanged: (product)->
    taskGrid = $("#taskGrid")
    prop = "name,createTime,lastUpdateTime,status"
    taskGrid.setGridParam(url: '/rest/tasks', postData: {prod: product.release.id, format: 'grid', prop: prop}).trigger "reloadGrid"










