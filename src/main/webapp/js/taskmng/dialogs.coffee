define [ 'jqueryui', 'require', 'taskmng/taskreport_grid', 'taskmng/transdetail_grid'], ($, require, reportgrid, detailgrid)->
  c18n = require 'i18n!nls/common'
  util = require 'util'
  #  require 'blockui'
  #  require 'jqmsgbox'
  #  require 'taskmng/taskreport_grid'

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
    $.ajax '/rest/languages', async: false, dataType: 'json', data: {task: param.id, prop: 'id,name'}, success: (languages)->
      reportgrid.regenerateGrid {id: param.id, languages: languages}

  buttons: [
    {text: 'Import', click: ()->
      alert 'Hi'
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
  }

  transReport: transReport
  viewDetail: viewDetail



