define ['jqueryui', 'appmng/dictionary_grid', 'appmng/langsetting_grid', 'jsfileuploader/jquery.iframe-transport', 'jsfileuploader/jquery.fileupload'], ($, grid, langGrid)->
  $("#selAppVersion").change ->
    grid.appChanged {version: $(@).find("option:selected").text(), id: $(@).val()}

  ($("#progressbar").draggable({grid: [50, 20], opacity: 0.35}).css({'z-index': 1000, width: 600, textAlign: 'center'
  'position': 'absolute', 'top': '45%', 'left': '30%'}).progressbar {
  change: (e, ui)->
    value = ($(@).progressbar "value").toPrecision(4) + '%'
    #    $("#progressbar .ui-progressbar-value").html(value).css({"display": "block", "textAlign": "right"})
    $('#barvalue', @).html(value).css {"display": "block", "textAlign": "center"}
  }).hide()

  #  $("#dctFileUpload").css 'visibility', 'hidden'
  $('#uploadBrower').button().click ()-> #$("#dctFileUpload").trigger "click"; false

  $("#dctFileUpload").fileupload {
  type: 'POST'
  url: 'app/deliver-app-dict'
  #  forceIframeTransport:true

  add: (e, data)->
    $.each data.files, (index, file) ->
      $('#uploadStatus').html "Uploading file: #{file.name}"
    data.submit()
    $("#progressbar").show() if !$.browser.msie


  done: (e, data)->
    $.each data.files, (index, file) ->$('#uploadStatus').html "#{file.name} upload finished."
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
