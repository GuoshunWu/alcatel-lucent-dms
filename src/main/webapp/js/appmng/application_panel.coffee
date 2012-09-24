define ['jqueryui', 'appmng/dictionary_grid', 'jsfileuploader/jquery.iframe-transport', 'jsfileuploader/jquery.fileupload'], ($, grid)->
  $("#selAppVersion").change ->
    grid.appChanged {version: $(@).find("option:selected").text(), id: $(@).val()}

  # create progress bar
  $("#progressDialog").dialog {
  autoOpen: false, width: '600', height: '100'
  }

  $("#progressbar").progressbar()


  $("#dctFileUpload").css 'visibility', 'hidden'

  $('#uploadBrower').button().click ()->
    $("#dctFileUpload").trigger "click"
    false

  $("#dctFileUpload").fileupload {
  type: 'POST'
  url: 'app/deliver-app-dict'
  add: (e, data)->
    $.each data.files, (index, file) ->
      $('#uploadStatus').html "Uploading file: #{file.name}"
    data.submit()
    $("#progressDialog").dialog("open")

  done: (e, data)->
    $("#progressDialog").dialog "close"
    $.each data.files, (index, file) ->$('#uploadStatus').html "#{file.name} upload finished."


  progressall: (e, data) ->
    progress = data.loaded / data.total * 100
    $('#progressbar').progressbar "value", progress
    $("#progressbar").children('.ui-progressbar-value')
    .html(progress.toPrecision(3) + '%')
    .css({"display":"block","textAlign":"left"})
  }

  refresh: (info)->
    $('#appDispProductName').html info.parent.text
    $('#appDispAppName').html info.text

    $.getJSON "rest/applications/apps/#{info.id}", {}, (json)->
      $("#selAppVersion").empty().append ($(json).map ()-> new Option @version, @id)
      $("#selAppVersion").trigger "change"
