define ['jquery', 'appmng/dictionary_grid', 'jsfileuploader/jquery.iframe-transport', 'jsfileuploader/jquery.fileupload'], ($, grid)->
  $("#selAppVersion").change ->
    grid.appChanged {version: $(@).find("option:selected").text(), id: $(@).val()}

  $("#progressbar").progressbar()

  $("#dctFileUpload").fileupload {
  type: 'POST'
  url: 'app/deliver-app-dict'
  add: (e, data)->
    $.each data.files, (index, file) ->
      $('#uploadStatus').html "Uploading file: #{file.name}"
    data.submit()

  done: (e, data)->
    $.each data.files, (index, file) ->$('#uploadStatus').html "#{file.name} upload finished."

  #    $.each data.result, (index, file)->console.log file.name
  progressall: (e, data) ->
    console.log "I am in progress."
    $('#progressbar').progressbar "value", parseInt(data.loaded / data.total * 100, 10)
  }

  refresh: (info)->
    $('#appDispProductName').html info.parent.text
    $('#appDispAppName').html info.text

    $.getJSON "rest/applications/apps/#{info.id}", {}, (json)->
      $("#selAppVersion").empty().append ($(json).map ()-> new Option @version, @id)
      $("#selAppVersion").trigger "change"
