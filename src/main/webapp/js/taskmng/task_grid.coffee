define [
  'jqgrid'
  'blockui'
  'jqmsgbox'
  'jqupload'
  'iframetransport'

  'i18n!nls/common'
  'i18n!nls/taskmng'
  'dms-util'
  'dms-urls'

  'taskmng/dialogs'
], ($, blockui, msgbox, upload, iframetrans, c18n, i18n, util, urls, dialogs)->

  handlers =
    'Download':
      title: 'Download'
      url: 'task/generate-task-files'
      handler: (param)->
        filename = $('#versionTypeLabel',"div[id='taskmng']").text() + '_'
        filename += $('#selVersion option:selected',"div[id='taskmng']").text() + '_translation'
        filename += "_#{new Date().format 'yyyyMMdd_hhmmss'}.zip"

        $.blockUI()
        $.post 'task/generate-task-files', {id: param.id, filename: filename}, (json)->
          $.unblockUI()
          ($.msgBox json.message, null, {title: c18n.error};return) if json.status != 0

          downloadForm = $('#downloadTaskFiles')
          $('#fileLoc', downloadForm).val json.fileLoc
          downloadForm.submit()
    'View…':
      title: 'View…'
      url: ''
      handler: (param)->
      #      dialogs.transReport.data 'param', {id: param.id, showImport: Boolean(param.lastUpdateTime)}
        dialogs.transReport.data 'param', {id: param.id, viewReport: true}
        dialogs.transReport.dialog 'open'
    'Close':
      title: 'Close'
      url: 'task/close-task'
      handler: (param)->
        return if param.status == '1'
        $.blockUI
        $.post 'task/close-task', {id: param.id}, (json)->
          $.unblockUI()
          if json.status != 0
            $.msgBox json.message, null, {title: c18n.error}
            return
          $("#taskGrid").trigger 'reloadGrid'
    'Upload':
      title: 'Upload'
      url: 'task/receive-task-files'
      handler: ((param)->)


  prop = "name,createTime,lastUpdateTime,status"

  taskGrid = $("#taskGrid").jqGrid(
    mtype: 'POST'
    editurl: "", datatype: 'local'
    width: 'auto', height: 400, shrinkToFit: false
    cellactionhandlers: handlers
    rownumbers: true, loadonce: false # for reload the colModel
    pager: '#taskPager', rowNum: 60, rowList: [10, 20, 30, 60, 120]
    sortname: 'createTime', sortorder: 'desc', viewrecords: true, gridview: true, multiselect: false,
    cellEdit: true, cellurl: ''
    colNames: ['Task', 'Creator', 'Create time', 'Last upload time', 'Status' , 'Actions']
    colModel: [
      {name: 'name', index: 'name', width: 250, editable: false, stype: 'select', align: 'left'}
      {name: 'creator.name', index: 'creator.name', width: 100, editable: false, stype: 'select', align: 'left'}
      {name: 'createTime', index: 'createTime', width: 150, editable: false, align: 'right' }
      {name: 'lastUpdateTime', index: 'lastUpdateTime', width: 150, align: 'left'}
      {name: 'status', index: 'status', width: 80, align: 'left', editable: false, edittype: 'select',
      editoptions: {value: "0:#{i18n.task.open};1:#{i18n.task.closed}"}, formatter: 'select'}
      {name: 'actions', index: 'actions', width: 260, align: 'center',
      formatter: (cellvalue, options, rowObject)->
        $.map(handlers,
        (value, index)->
          return if '1' == rowObject[4] and index in ['Upload', 'Close']
          return "<a id='upload_#{index}_#{options.rowId}'title='#{value.title}' ></a>" if index == 'Upload'
          "<A id='action_#{index}_#{options.rowId}' style='color:blue' title='#{value.title}'href=# >#{index}</A>"
        ).join('&nbsp;&nbsp;&nbsp;&nbsp;')
      }
    ]
    beforeProcessing: (data, status, xhr)->
    gridComplete: ->
      grid = $(@)
      handlers = grid.getGridParam 'cellactionhandlers'

      $('a[id^=action_]', @).click ()->
        [a, action, rowid, col]=@id.split('_')
        rowData = grid.getRowData(rowid)
        delete rowData.actions
        rowData.id = rowid

        handlers[action].handler rowData

      $("#progressbar").draggable(grid: [50, 20], opacity: 0.35).progressbar(
        create: (e, ui) ->
          @label = $('.progressbar-label', @)
          $(@).position(my: 'center', at: 'center', of: window)
        change: (e, ui)->
          @label.html ($(this).progressbar("value").toPrecision(4)) + "%"
      ).hide()


      $('a[id^=upload_]', @).button(label: 'Upload',
        create: (e, ui)->
          [_, _, rowid]=@id.split('_')

          # modify the css of this button

          fileInput = $("<input type='file' id='#{@id}_fileInput' name='upload' accept='application/zip' multiple/>").css({
          position: 'absolute', top: 0, right: 0, border: '1px solid', borderWidth: '10px 180px 40px 20px',
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
      ).removeClass().addClass('ui-button').css(overflow: 'hidden')
      $('a[id^=upload_] .ui-button-text').css({textDecoration: 'underline', color: 'blue'})

    afterCreate: (grid)->
      grid.setGridParam 'datatype': 'json'
      grid.navGrid '#taskPager', {edit: false, add: false, del: false, search: false, view: false}
  )
  taskGrid.getGridParam('afterCreate') taskGrid

  versionChanged: (param)->
    taskGrid = $("#taskGrid", '#taskmng')
    prop = "name,creator.name,createTime,lastUpdateTime,status"
    postData = format: 'grid', prop: prop
    postData[param.type] = param.release.id

    console?.log postData

    # clear origianl grid post
#    taskGrid.getGridParam('postData')
    taskGrid.setGridParam(url: urls.tasks, postData: postData).trigger "reloadGrid"

