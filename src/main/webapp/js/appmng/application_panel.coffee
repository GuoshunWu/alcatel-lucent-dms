define ['require','jqueryui', 'iframetransport','iframetransport','jqupload', 'i18n!nls/appmng', 'i18n!nls/common', 'util', 'appmng/dialogs', 'appmng/langsetting_grid','appmng/stringsettings_grid','appmng/history_grid','appmng/dictionary_grid' ],(require, $, ifrm, iftrans, jqupload, i18n, c18n, util, dialogs, lgrid, sgrid, hgrid, grid)->
  appInfo = {}

  $("#newAppVersion").button({text: false, label: '&nbsp;', icons: {primary: "ui-icon-plus"}}).
  attr('privilegeName', util.urlname2Action 'app/create-application').
  click (e) =>dialogs.newAppVersion.dialog("open")

  $("#removeAppVersion").button({text: false, label: '&nbsp;', icons: {primary: "ui-icon-minus"}}).
  attr('privilegeName', util.urlname2Action 'app/remove-application').
  click (e) =>
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

  $("#progressbar").draggable(grid: [50, 20], opacity: 0.35).progressbar(
    create: (e, ui) ->
      @label = $('.progressbar-label', @)
    change: (e, ui)->
      @label.html ($(this).progressbar("value").toPrecision(4)) + "%"
    complete: (e, ui) ->
      $(@).progressbar("value", 0).hide()
  ).hide()

  dctFileUpload = 'dctFileUpload'
  #  create upload filebutton
  $('#uploadBrower').button(label: i18n.browse).attr('privilegeName', util.urlname2Action('app/deliver-app-dict')).css({overflow: 'hidden'}).append $(
    "<input type='file' id='#{dctFileUpload}' name='upload' title='#{i18n.choosefile}' accept='application/zip' multiple/>").css {
  position: 'absolute', top: -3, right: -3, border: '1px solid', borderWidth: '10px 180px 40px 20px',
  opacity: 0, filter: 'alpha(opacity=0)',
  cursor: 'pointer'
  }

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
    $("#progressbar").show() if !$.browser.msie || parseInt($.browser.version.split('\.')[0]) >= 10
    $('#uploadBrower').button 'disable'
  progressall: (e, data) ->
    progress = data.loaded / data.total * 100
    $('#progressbar').progressbar "value", progress
  done: (e, data)->
    $('#uploadBrower').button 'enable'

    $.each data.files, (index, file) ->$('#uploadStatus').html "#{file.name} #{i18n.uploadfinished}"
    $("#progressbar").hide() if !$.browser.msie || parseInt($.browser.version.split('\.')[0]) >= 10
    #    request handler
    jsonFromServer = data.result

    if(0 != jsonFromServer.status)
      $.msgBox jsonFromServer.message, null, {title: c18n.error, height: 600, width: 800}
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
