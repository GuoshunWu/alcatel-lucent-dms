define (require)->
  $ = require 'jqueryui'
  require 'appmng/langsetting_grid'
  require 'appmng/stringsettings_grid'

  require 'jqupload'
  require 'iframetransport'
  dialogs = require 'appmng/dialogs'

  grid = require 'appmng/dictionary_grid'
  i18n = require 'i18n!nls/appmng'
  c18n = require 'i18n!nls/appmng'
  util = require 'util'

  appInfo = {}

  $("#newAppVersion").button({text: false, label: '&nbsp;', icons: {primary: "ui-icon-plus"}}).click (e) =>
    dialogs.newAppVersion.dialog("open")

  $("#removeAppVersion").button({text: false, label: '&nbsp;', icons: {primary: "ui-icon-minus"}}).click (e) =>
    id = $("#selAppVersion").val()
    return if !id
    $.post 'app/remove-application', {id: id, permanent: 'true'}, (json)->
      if json.status != 0
        $.msgBox json.message, null, {title: c18n.error}
        return
      $("#selAppVersion option:selected").remove()
      $("#selAppVersion").trigger 'change'

  $("#selAppVersion").change (e)->
    appInfo.app = {version: $("option:selected", @).text(), id: if @value then @value else -1}
    grid.appChanged appInfo

  ($("#progressbar").draggable({grid: [50, 20], opacity: 0.35}).css({
  'z-index': 100, width: 600, textAlign: 'center'
  'position': 'absolute', 'top': '45%', 'left': '30%'}).progressbar {
  change: (e, ui)->
    value = ($(@).progressbar "value").toPrecision(4) + '%'
    $('#barvalue', @).html(value).css {"display": "block", "textAlign": "center"}
  }).hide()

  dctFileUpload = 'dctFileUpload'
  #  create upload filebutton
  $('#uploadBrower').button({label: i18n.browse}).css({overflow: 'hidden'}).append $(
    "<input type='file' id='#{dctFileUpload}' name='upload' title='#{i18n.choosefile}' accept='application/zip' multiple/>").css {
  position: 'absolute', top: -3, right: -3, border: '1px solid', borderWidth: '1px 1px 10px 0px',
  opacity: 0, filter: 'alpha(opacity=0)', cursor: 'pointer'}

  $("##{dctFileUpload}").fileupload {
  type: 'POST', dataType: 'json'
  url: "app/deliver-app-dict"

  #  forceIframeTransport:true

  add: (e, data)->
    $.each data.files, (index, file) ->
    #      $('#uploadStatus').html "#{i18n.uploadingfile}#{file.name}"
    appId = $("#selAppVersion").val()
    return if !appId
    $(@).fileupload 'option', 'formData', [
      {name: 'appId', value: $("#selAppVersion").val()}
    ]
    data.submit()
    $("#progressbar").show() if !$.browser.msie
  progressall: (e, data) ->
    progress = data.loaded / data.total * 100
    $('#progressbar').progressbar "value", progress
  done: (e, data)->
    $.each data.files, (index, file) ->$('#uploadStatus').html "#{file.name} #{i18n.uploadfinished}"
    $("#progressbar").hide() if !$.browser.msie
    #    request handler
    jsonFromServer = data.result

    if(0 != jsonFromServer.status)
      $.msgBox jsonFromServer.message, null, {title: c18n.error}
      return

    $('#dictListPreviewDialog').data 'param', {handler: jsonFromServer.filename, appId: $("#selAppVersion").val()}
    $('#dictListPreviewDialog').dialog 'open'
  }

  getApplicationSelectOptions: ()->$('#selAppVersion').children('option').clone(true)
  addNewApplication: (app) ->
    $('#selAppVersion').append("<option value='#{app.id}' selected>#{app.version}</option>").trigger 'change'
  refresh: (info)->
    $('#appDispProductName').html info.parent.text
    $('#appDispAppName').html info.text

    appInfo.base = {text: info.text, id: info.id}

    $.getJSON "rest/applications/apps/#{info.id}", {}, (json)->$("#selAppVersion").empty().append(util.json2Options json).trigger "change"
