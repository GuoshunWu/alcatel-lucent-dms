define [ 'jqueryui', 'require', 'taskmng/taskreport_grid', 'taskmng/transdetail_grid', 'jqmsgbox'], ($, require, reportgrid, detailgrid)->
  c18n = require 'i18n!nls/common'
  i18n = require 'i18n!nls/taskmng'
  util = require 'util'

  #  require 'jqmsgbox'
  c18n = require 'i18n!nls/common'

  #  require 'blockui'


  languageChooserDialog = $("<div title='Study' id='languageChooser'>").dialog {
  autoOpen: false, position: [23, 126], height: 'auto', width: 'auto'
  show: { effect: 'slide', direction: "up" }
  create: ->$.getJSON 'rest/languages?prop=id,name', {}, (languages)=>$(@).append(util.generateLanguageTable languages)
  buttons: [
    { text: c18n.ok, click: ()->
      console.log ($(":checkbox[name='languages']", @).map () -> {id: @id, name: @value} if @checked).get()
      $(@).dialog "close"
    }
    {text: c18n.cancel, click: ()->$(@).dialog "close"}
  ]
  }

  transReport = $('#translationReportDialog').dialog {
  autoOpen: false
  width: 'auto', height: 'auto'
  open: ()->
    param = $(@).data 'param'
    $.ajax 'rest/languages', async: false, dataType: 'json', data: {task: param.id, prop: 'id,name'}, success: (languages)->
      reportgrid.regenerateGrid {id: param.id, languages: languages}

  buttons: [
    {text: 'Import', click: ()->
      param = $(@).data 'param'
      $.post 'task/apply-task', {id: param.id}, (json)->
        if json.status != 0
          $.msgBox json.message, null, {title: c18n.error}
          return
        $.msgBox i18n.task.confirmmsg, ((keyPressed)->
          if c18n.ok == keyPressed
            $.blockUI
            $.post 'task/close-task', {id: param.id}, (json)->
              $.unblockUI()
              if json.status != 0
                $.msgBox json.message, null, {title: c18n.error}
                return
              $("#taskGrid").trigger 'reloadGrid'
        ), {title: c18n.confirm}, [c18n.ok, c18n.cancel]


      $(@).dialog "close"
    }
    {text: 'Cancel', click: ()-> $(@).dialog "close"}
  ]
  }
  $('#langChooser').button({}).click ()->
    languageChooserDialog.dialog 'open'

  viewDetail = $('#translationDetailDialog').dialog {
  autoOpen: false
  width: 'auto', height: 'auto'
  open: ->
    param = $(@).data 'param'
    postData = $.extend param, {format: 'grid', prop: 'labelKey,maxLength,text.context.name,text.reference,newTranslation'}
    detailgrid.setGridParam(url: 'rest/task/details', postData: postData).trigger 'reloadGrid'
  }

  transReport: transReport
  viewDetail: viewDetail



