#define ['jqgrid','jqmsgbox','taskmng/dialogs', 'i18n!nls/taskmng', 'i18n!nls/common', 'blockui', 'require'], ($,jqmsgbox,dialogs, i18n, c18n, blockui, require)->
define (require)->
  $ = require 'jqgrid'
  util = require 'util'
  dialogs = require 'taskmng/dialogs'
  c18n = require 'i18n!nls/common'
  i18n = require 'i18n!nls/taskmng'

  require 'blockui'
  require 'jqmsgbox'
  require 'jqupload'
  require  'iframetransport'

  handlers =
    'Download': (param)->
      filename = "#{$('#productBase option:selected').text()}_#{$('#productRelease option:selected').text()}_translation"
      filename += "_#{new Date().format 'yyyyMMdd_hhmmss'}.zip"

      $.blockUI()
      $.post 'task/generate-task-files', {id: param.id, filename: filename}, (json)->
        $.unblockUI()
        ($.msgBox json.message, null, {title: c18n.error};return) if json.status != 0

        downloadForm = $('#downloadTaskFiles')
        $('#fileLoc', downloadForm).val json.fileLoc
        downloadForm.submit()
    'View…': (param)->
      dialogs.transReport.data 'param', {id: param.id}
      dialogs.transReport.dialog 'open'
    'Close': (param)->
      return if param.status == '1'
      $.blockUI
      $.post 'task/close-task', {id: param.id}, (json)->
        $.unblockUI()
        if json.status != 0
          $.msgBox json.message, null, {title: c18n.error}
          return
        $("#taskGrid").trigger 'reloadGrid'
    'Upload': ((param)->)



  productId = window.location.search.split('?')[1].split('&')[1].split('=')[1] if window.location.search

#  url = if productId then 'rest/tasks'else 'json/taskgrid.json'
  url = if productId then 'rest/tasks'else ''
  prop = "name,createTime,lastUpdateTime,status"
#  console.log "Task grid url: #{url}."

  taskGrid = $("#taskGrid").jqGrid {
  url: url
  mtype: 'POST', postData: {prod: productId, format: 'grid', prop: prop},
  editurl: "", datatype: 'json'
  width: $(window).width() * 0.95, height: 400, shrinkToFit: false
  rownumbers: true, loadonce: false # for reload the colModel
  pager: '#taskPager', rowNum: 60, rowList: [10, 20, 30, 60, 120]
  sortname: 'createTime', sortorder: 'desc', viewrecords: true, gridview: true, multiselect: false,
  cellEdit: true, cellurl: 'http://127.0.0.1:2000'
  colNames: ['Task', 'Create time', 'Last upload time', 'Status' , 'Actions']
  colModel: [
    {name: 'name', index: 'name', width: 250, editable: false, stype: 'select', align: 'left'}
    {name: 'createTime', index: 'createTime', width: 150, editable: false, align: 'right' }
    {name: 'lastUpdateTime', index: 'lastUpdateTime', width: 150, align: 'left'}
    {name: 'status', index: 'status', width: 80, align: 'left', editable: false, edittype: 'select',
    editoptions: {value: "0:#{i18n.task.open};1:#{i18n.task.closed}"}, formatter: 'select'}
    {name: 'actions', index: 'actions', width: 280, align: 'center'}
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
        (index, action)->
          return "<span id='upload_#{@}_#{rowData.id}_#{actIndex}'></span>" if action == 'Upload'
          "<a id='action_#{@}_#{rowData.id}_#{actIndex}' style='color:blue'title='#{@}' href=#  >#{@}</A>"
      ).get().join('&nbsp;&nbsp;&nbsp;&nbsp;')
  gridComplete: ->
    grid = $(@)

    $('a[id^=action_]', @).click ()->
      [a, action, rowid, col]=@id.split('_')
      rowData = grid.getRowData(rowid)
      delete rowData.actions
      rowData.id = rowid

      handlers[action] rowData

    ($("#progressbar").draggable({grid: [50, 20], opacity: 0.35}).css({
    'z-index': 100, width: 600, textAlign: 'center'
    'position': 'absolute', 'top': '45%', 'left': '30%'}).progressbar {
    change: (e, ui)->
      value = ($(@).progressbar "value").toPrecision(4) + '%'
      $('#barvalue', @).html(value).css {"display": "block", "textAlign": "center"}
    }).hide()


    $('span[id^=upload_]', @).button(label: 'Upload',
      create: (e, ui)->
        [_, _, rowid]=@id.split('_')

        fileInput = $("<input type='file' id='#{@id}_fileInput' name='upload' title='Upload task file'accept='application/zip' multiple/>").css({
        position: 'absolute', top: -3, right: -3, border: '1px solid', borderWidth: '1px 1px 10px 0px',
        opacity: 0, filter: 'alpha(opacity=0)', cursor: 'pointer'}).appendTo @

        fileInput.fileupload {
        type: 'POST', dataType: 'json'
        url: "task/receive-task-files"
        formData: [
          {name: 'id', value: rowid}
        ]
        acceptFileTypes: /zip$/i
        add: (e, data)->
          data.submit()
          $("#progressbar").show() if !$.browser.msie
        progressall: (e, data)->
          progress = data.loaded / data.total * 100
          $('#progressbar').progressbar "value", progress
        done: (e, data)->
          $("#progressbar").hide() if !$.browser.msie
          #    request handler
          jsonFromServer = data.result

          if(0 != jsonFromServer.status)
            $.msgBox jsonFromServer.message, null, {title: c18n.error}
            return
          dialogs.transReport.data 'param', {id: rowid}
          dialogs.transReport.dialog 'open'
        }
    ).css(overflow: 'hidden')


  afterCreate: (grid)->
    grid.navGrid '#taskPager', {edit: false, add: false, del: false, search: false, view: false}
  }
  taskGrid.getGridParam('afterCreate') taskGrid



  productVersionChanged: (product)->
    taskGrid = $("#taskGrid")
    prop = "name,createTime,lastUpdateTime,status"
    taskGrid.setGridParam(url: 'rest/tasks', postData: {prod: product.release.id, format: 'grid', prop: prop}).trigger "reloadGrid"










