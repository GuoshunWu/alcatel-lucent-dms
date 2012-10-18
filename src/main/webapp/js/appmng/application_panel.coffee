define (require)->
  $ = require 'jqueryui'
  require 'appmng/langsetting_grid'
  require 'appmng/stringsettings_grid'

  require 'jqupload'
  require 'iframetransport'

  grid = require 'appmng/dictionary_grid'
  i18n = require 'i18n!nls/appmng'
  c18n = require 'i18n!nls/appmng'


  $("#selAppVersion").change (e)->
    grid.appChanged {version: $("option:selected", @).text(), id: @value}

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
  type: 'POST'
  url: 'app/deliver-app-dict'
  #  forceIframeTransport:true

  add: (e, data)->
    $.each data.files, (index, file) ->
      $('#uploadStatus').html "#{i18n.uploadingfile}#{file.name}"
    data.submit()
    $("#progressbar").show() if !$.browser.msie
  progressall: (e, data) ->
    progress = data.loaded / data.total * 100
    $('#progressbar').progressbar "value", progress
  done: (e, data)->
    $.each data.files, (index, file) ->$('#uploadStatus').html "#{file.name} #{i18n.uploadfinished}"
    $("#progressbar").hide() if !$.browser.msie
#    request handler
    jsonFromServer=eval "(#{data.result})"

    if(0!=jsonFromServer.status)
      $.msgBox jsonFromServer.message, null, {title: c18n.error}
      return
    $('#dictListPreviewDialog').data 'param',{handler: jsonFromServer.filename}
    $('#dictListPreviewDialog').dialog 'open'
  }

  refresh: (info)->
    $('#appDispProductName').html info.parent.text
    $('#appDispAppName').html info.text

    $.getJSON "rest/applications/apps/#{info.id}", {}, (json)->
      $("#selAppVersion").empty().append ($(json).map ()-> new Option @version, @id)
      $("#selAppVersion").trigger "change"
