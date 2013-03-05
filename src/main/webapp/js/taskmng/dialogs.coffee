define ['jqueryui', 'taskmng/taskreport_grid', 'taskmng/transdetail_grid', 'jqmsgbox', 'i18n!nls/common', 'i18n!nls/taskmng', 'util', 'require'], ($, reportgrid, detailgrid, msgbox, c18n, i18n, util, require)->
  languageChooserDialog = $("<div title='Study' id='languageChooser'>").dialog {
  autoOpen: false, position: [23, 126], height: 'auto', width: 900, modal: true
  show: { effect: 'slide', direction: "up" }
  create: ->$.getJSON 'rest/languages?prop=id,name', {}, (languages)=>$(@).append(util.generateLanguageTable languages)
  buttons: [
    { text: c18n.ok, click: ()->
      $(@).dialog "close"
    }
    {text: c18n.cancel, click: ()->$(@).dialog "close"}
  ]
  }

  transReport = $('#translationReportDialog').dialog {
  autoOpen: false, modal: true
  width: $(window).width() * 0.8, height: 'auto'
  open: ()->
    param = $(@).data 'param'
    buttons = [
      {text: c18n.close, click: ()-> $(@).dialog "close"}
    ]

    buttons.unshift {text: c18n.import, click: ()->
      param = $(@).data 'param'
      $.blockUI
      $.post 'task/apply-task', {id: param.id}, (json)->
        $.unblockUI()
        ($.msgBox json.message, null, {title: c18n.error};return) if json.status != 0
        $.msgBox i18n.task.confirmmsg, ((keyPressed)->
          if c18n.no == keyPressed
            $.blockUI
            $.post 'task/close-task', {id: param.id}, (json)->
              $.unblockUI()
              ($.msgBox json.message, null, {title: c18n.error}; return)if json.status != 0
              $("#taskGrid").trigger 'reloadGrid'
        ), {title: c18n.confirm}, [c18n.yes, c18n.no]
      $(@).dialog "close"
    } unless param.viewReport
    $(@).dialog 'option', 'buttons', buttons
    $.ajax 'rest/languages', async: false, dataType: 'json', data: {task: param.id, prop: 'id,name'}, success: (languages)->
      reportgrid.regenerateGrid {id: param.id, languages: languages}
  #  resize: (event, ui)->$("#reportGrid").setGridWidth(ui.size.width - 40, true).setGridHeight(ui.size.height - 190, true)
  }


  $('#langChooser').button({}).click ()->
    languageChooserDialog.dialog 'open'

  viewDetail = $('#translationDetailDialog').dialog {
  autoOpen: false, modal: true
  width: 850, height: 'auto'
  resize: (event, ui)->$("#viewDetailGrid", @).setGridWidth(ui.size.width - 35, true).setGridHeight(ui.size.height - 145, true)
  open: ->
    param = $(@).data 'param'
    console.log param
    postData = $.extend param, {format: 'grid', prop: 'labelKey,maxLength,text.context.name,text.reference,newTranslation'}
    detailgrid.setGridParam(url: 'rest/task/details', postData: postData).trigger 'reloadGrid'
  }

  transReport: transReport
  viewDetail: viewDetail



