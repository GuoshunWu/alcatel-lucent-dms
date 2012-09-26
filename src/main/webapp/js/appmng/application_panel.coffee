define ['jqueryui', 'appmng/dictionary_grid', 'appmng/langsetting_grid', 'i18n!nls/appmng','appmng/stringsettings_grid', 'jsfileuploader/jquery.iframe-transport', 'jsfileuploader/jquery.fileupload'], ($, grid, langGrid, i18n)->
  $("#selAppVersion").change ->
    grid.appChanged {version: $("option:selected",@).text(), id: @value}

  ($("#progressbar").draggable({grid: [50, 20], opacity: 0.35}).css({
  'z-index': 100, width: 600, textAlign: 'center'
  'position': 'absolute', 'top': '45%', 'left': '30%'}).progressbar {
  change: (e, ui)->
    value = ($(@).progressbar "value").toPrecision(4) + '%'
    $('#barvalue', @).html(value).css {"display": "block", "textAlign": "center"}
  }).hide()

  dctFileUpload='dctFileUpload'
  #  create upload filebutton
  $('#uploadBrower').button({label: i18n.browse}).css({overflow: 'hidden'}).append $(
    "<input type='file' id='#{dctFileUpload}' name='upload' title='#{i18n.choosefile}' multiple/>").css {
  position: 'absolute', top: 0, right: 0, margin: 0,
  border: '1px transparent', borderWidth: '0 0 40px 0px',
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


  done: (e, data)->
    $.each data.files, (index, file) ->$('#uploadStatus').html "#{file.name} #{i18n.uploadfinished}"
    $("#progressbar").hide() if !$.browser.msie

  #  always: (e, data)->
  #    console.log "I am in always."

  progressall: (e, data) ->
    progress = data.loaded / data.total * 100
    $('#progressbar').progressbar "value", progress
  }

  refresh: (info)->
    $('#appDispProductName').html info.parent.text
    $('#appDispAppName').html info.text

    $.getJSON "rest/applications/apps/#{info.id}", {}, (json)->
      $("#selAppVersion").empty().append ($(json).map ()-> new Option @version, @id)
      $("#selAppVersion").trigger "change"
